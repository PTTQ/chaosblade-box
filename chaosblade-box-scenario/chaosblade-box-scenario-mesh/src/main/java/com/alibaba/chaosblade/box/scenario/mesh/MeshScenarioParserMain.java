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

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.chaosblade.box.common.enums.ChaosTools;
import com.alibaba.chaosblade.box.common.model.chaos.PluginSpecBean;
import com.alibaba.chaosblade.box.common.utils.SystemPropertiesUtils;
import com.alibaba.chaosblade.box.scenario.api.ScenarioFileNameParser;
import com.alibaba.chaosblade.box.scenario.api.ScenarioRequest;
import com.alibaba.chaosblade.box.scenario.api.model.ScenarioOriginal;
import com.alibaba.chaosblade.box.scenario.api.model.ToolsVersion;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author luo rongzhou
 */
public class MeshScenarioParserMain {

    public static void main(String[] args) {
        File[] ls = FileUtil.ls(ChaosTools.CHAOS_MESH.getName());
        if (ArrayUtil.isEmpty(ls)) {
            return;
        }
        Representer representer = new Representer();
        representer.setPropertyUtils(new PropertyUtils() {
            @Override
            public Property getProperty(Class<? extends Object> type, String name) {
                if (name.indexOf('-') > -1) {
                    name = name.replace('-', '_');
                }
                return super.getProperty(type, name);
            }
        });

        for (File file : ls) {
            if (file.isFile()) {
                continue;
            }

            ToolsVersion toolsVersion = new Yaml(representer).loadAs(
                    FileUtil.getInputStream(file.getPath() + "/version.yaml")
                    , ToolsVersion.class);

            for (String scenarioFile : toolsVersion.getScenarioFiles()) {
                PluginSpecBean bean = ScenarioFileNameParser.parse(scenarioFile);

                AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
                MeshScenarioParser parser = context.getBean(MeshScenarioParser.class);
                ArrayList<ScenarioOriginal> mesh = new ArrayList<>();

                mesh.add(ScenarioOriginal.builder()
                        .name(bean.getType())
                        .version(toolsVersion.getVersion())
                        .url(String.format("http://open-source-summer.cn-bj.ufileos.com/chaosmesh-%s-spec-%s.yaml", bean.getType(), bean.getVersion()))
                        .build()
                );

                parser.setMesh(mesh);
                List<PluginSpecBean> parse = parser.parse(ScenarioRequest.builder().build());

                parse.forEach(pluginSpecBean -> {

                    String path = pluginSpecBean.getKind() + File.separatorChar + pluginSpecBean.getVersion();

                    Yaml yaml = new Yaml();
                    String dump = yaml.dumpAs(pluginSpecBean, Tag.MAP, DumperOptions.FlowStyle.BLOCK);

                    FileUtil.writeString(dump, path + "/" + ScenarioFileNameParser.toFileName(pluginSpecBean), SystemPropertiesUtils.getPropertiesFileEncoding());
                });
            }
        }
    }

    @ComponentScan("com.alibaba.chaosblade.box.scenario.mesh")
    private static class Config {

    }
}
