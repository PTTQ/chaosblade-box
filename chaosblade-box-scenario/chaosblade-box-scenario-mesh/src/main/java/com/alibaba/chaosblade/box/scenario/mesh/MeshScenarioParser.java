/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.chaosblade.box.scenario.mesh;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.NoResourceException;
import cn.hutool.core.io.resource.ResourceUtil;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;

import com.alibaba.chaosblade.box.common.enums.ChaosTools;
import com.alibaba.chaosblade.box.common.model.chaos.ActionSpecBean;

import com.alibaba.chaosblade.box.common.model.chaos.FlagSpecBean;
import com.alibaba.chaosblade.box.common.model.chaos.ModelSpecBean;
import com.alibaba.chaosblade.box.common.model.chaos.PluginSpecBean;
import com.alibaba.chaosblade.box.common.utils.SystemPropertiesUtils;
import com.alibaba.chaosblade.box.scenario.api.Original;
import com.alibaba.chaosblade.box.scenario.api.ScenarioParser;
import com.alibaba.chaosblade.box.scenario.api.ScenarioRequest;
import com.alibaba.chaosblade.box.scenario.api.model.ScenarioOriginal;
import com.alibaba.chaosblade.box.scenario.mesh.model.ParamSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author luo rongzhou
 */
@Slf4j
@Original(ChaosTools.CHAOS_MESH)
@Component
@ConfigurationProperties(prefix = "chaos.scene.originals")
public class MeshScenarioParser implements ScenarioParser, InitializingBean {

    private List<ScenarioOriginal> mesh;

    public void setMesh(List<ScenarioOriginal> mesh) {
        this.mesh = mesh;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollUtil.isNotEmpty(mesh)) {
            parse(ScenarioRequest.builder().build());
        }
    }

    @Override
    public List<PluginSpecBean> parse(ScenarioRequest scenarioRequest) {
        return mesh.stream().map(
                mesh -> {
                    log.info("parse scenario yaml, name: {}", mesh.getName());
                    String file = String.format("%s/%s-%s-%s.yaml",
                            ChaosTools.CHAOS_MESH.getName(),
                            ChaosTools.CHAOS_MESH.getName(),
                            mesh.getName(),
                            mesh.getVersion());

                    String s;
                    try {
                        s = IoUtil.read(ResourceUtil.getStream(file), Charset.defaultCharset());
                    } catch (NoResourceException e) {
                        log.warn("class path not found litmus yaml");
                        log.info("parse scenario yaml from url, name: {}, url: {}", mesh.getName(), mesh.getUrl());
                        HttpRequest request = HttpUtil.createGet(mesh.getUrl());
                        HttpResponse execute = request.execute();
                        s = new String(execute.bodyBytes());
                        FileUtil.writeString(s, file, SystemPropertiesUtils.getPropertiesFileEncoding());
                    }

                    Representer representer = new Representer();
                    representer.getPropertyUtils().setSkipMissingProperties(true);
                    Yaml yaml = new Yaml(representer);
                    Map<String, List<String>> categories = yaml.load(ResourceUtil.getStream(String.format("chaosmesh/%s/category.yaml", mesh.getVersion())));

                    List<ParamSet> paramSetList = Arrays.stream(StrUtil.split(s, "---"))
                            .filter(StrUtil::isNotBlank)
                            .map(paramSet -> {
                                paramSet = paramSet.trim();
                                ParamSet one = yaml.loadAs(paramSet, ParamSet.class);
                                return ParamSet.builder()
                                        .action(one.getAction())
                                        .flags(one.getFlags())
                                        .build();
                            }).collect(Collectors.toList());


                    // transform ParamSet (maybe has two categories: pod and container) to ModelSpecBean
                    List<ModelSpecBean> modelSpecBeans = new ArrayList<>();
                    for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
                        String[] split = entry.getKey().split("\\.");
                        String scope = split[1].split("-")[0];
                        String target = split[1].split("-")[1];
                        String action = split[2];
                        String simpleSceneCode = "chaosmesh." + target + "." + action;

                        for (ParamSet paramSet : paramSetList) {
                            if (paramSet.getAction().equals(simpleSceneCode)) {
                                List<FlagSpecBean> flags = paramSet.getFlags().stream().map(
                                        flag -> FlagSpecBean.builder()
                                                .name(flag.getName())
                                                .desc(flag.getDesc())
                                                .required(flag.isRequired())
                                                .build()
                                ).collect(Collectors.toList());

                                String[] categoriesArray = new String[entry.getValue().size()];
                                for (int i = 0; i < entry.getValue().size(); i++) {
                                    categoriesArray[i] = entry.getValue().get(i);
                                }

                                List<ActionSpecBean> actionSpecBeans = new ArrayList<>();
                                actionSpecBeans.add(ActionSpecBean.builder()
                                        .action(action)
                                        .flags(flags)
                                        .categories(categoriesArray)
                                        .build());
                                ModelSpecBean bean = ModelSpecBean.builder()
                                        .scope(scope)
                                        .target(target)
                                        .actions(actionSpecBeans)
                                        .build();
                                modelSpecBeans.add(bean);
                                break;
                            }
                        }

                    }

                    return PluginSpecBean.builder()
                            .kind(ChaosTools.CHAOS_MESH.getName())
                            .type(mesh.getName())
                            .version(mesh.getVersion())
                            .items(modelSpecBeans).build();
                }
        ).collect(Collectors.toList());
    }
}
