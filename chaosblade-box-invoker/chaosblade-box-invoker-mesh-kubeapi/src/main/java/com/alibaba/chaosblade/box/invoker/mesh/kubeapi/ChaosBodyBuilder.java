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

package com.alibaba.chaosblade.box.invoker.mesh.kubeapi;

import com.alibaba.chaosblade.box.common.utils.SceneCodeParseUtil;
import com.alibaba.chaosblade.box.invoker.RequestCommand;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.ChaosPodSelector;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.dnschaos.DNSChaos;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.dnschaos.DNSChaosSpec;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.iochaos.IOChaos;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.iochaos.IOChaosSpec;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.networkchaos.NetworkChaos;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.networkchaos.NetworkChaosSpec;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.podchaos.PodChaos;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.podchaos.PodChaosSpec;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.stresschaos.StressChaos;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.stresschaos.StressChaosSpec;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.timechaos.TimeChaos;
import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.timechaos.TimeChaosSpec;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author luo rongzhou
 */
public class ChaosBodyBuilder {

    public static Object buildPodChaosBody(RequestCommand requestCommand, V1ObjectMeta v1ObjectMeta) {
        containersStrToArray(requestCommand);
        Map<String, String> arguments = requestCommand.getArguments();
        arguments.put("action", SceneCodeParseUtil.getAction(requestCommand.getSceneCode()));
        arguments.put("mode", "all");

        requestCommand.setArguments(arguments);

        String specJsonStr = argumentsToJsonStr(requestCommand.getArguments());
        JSONObject specJson = JSONObject.parseObject(specJsonStr);

        PodChaosSpec podChaosSpec = JSON.toJavaObject(specJson, PodChaosSpec.class);
        ChaosPodSelector selector = buildChaosSelector(requestCommand);
        podChaosSpec.setSelector(selector);

        return PodChaos.builder()
                .apiVersion(Constants.API_VERSION)
                .kind("PodChaos")
                .metadata(v1ObjectMeta)
                .spec(podChaosSpec).build();
    }

    public static Object buildNetworkChaosBody(RequestCommand requestCommand, V1ObjectMeta v1ObjectMeta) {
        containersStrToArray(requestCommand);
        Map<String, String> arguments = requestCommand.getArguments();
        arguments.put("action", SceneCodeParseUtil.getAction(requestCommand.getSceneCode()));
        arguments.put("mode", "all");

        requestCommand.setArguments(arguments);

        String specJsonStr = argumentsToJsonStr(requestCommand.getArguments());
        JSONObject specJson = JSONObject.parseObject(specJsonStr);

        NetworkChaosSpec networkChaosSpec = JSON.toJavaObject(specJson, NetworkChaosSpec.class);
        ChaosPodSelector selector = buildChaosSelector(requestCommand);
        networkChaosSpec.setSelector(selector);

        return NetworkChaos.builder()
                .apiVersion(Constants.API_VERSION)
                .kind("NetworkChaos")
                .metadata(v1ObjectMeta)
                .spec(networkChaosSpec).build();
    }

    public static Object buildStressChaosBody(RequestCommand requestCommand, V1ObjectMeta v1ObjectMeta) {
        containersStrToArray(requestCommand);
        Map<String, String> arguments = requestCommand.getArguments();
        arguments.put("mode", "all");

        requestCommand.setArguments(arguments);

        String specJsonStr = argumentsToJsonStr(requestCommand.getArguments());
        JSONObject specJson = JSONObject.parseObject(specJsonStr);

        StressChaosSpec stressChaosSpec = JSON.toJavaObject(specJson, StressChaosSpec.class);
        ChaosPodSelector selector = buildChaosSelector(requestCommand);
        stressChaosSpec.setSelector(selector);

        return StressChaos.builder()
                .apiVersion(Constants.API_VERSION)
                .kind("StressChaos")
                .metadata(v1ObjectMeta)
                .spec(stressChaosSpec).build();
    }

    public static Object buildIOChaosBody(RequestCommand requestCommand, V1ObjectMeta v1ObjectMeta) {
        containersStrToArray(requestCommand);
        Map<String, String> arguments = requestCommand.getArguments();
        arguments.put("action", SceneCodeParseUtil.getAction(requestCommand.getSceneCode()));
        arguments.put("mode", "all");

        requestCommand.setArguments(arguments);

        String specJsonStr = argumentsToJsonStr(requestCommand.getArguments());
        JSONObject specJson = JSONObject.parseObject(specJsonStr);

        IOChaosSpec ioChaosSpec = JSON.toJavaObject(specJson, IOChaosSpec.class);
        ChaosPodSelector selector = buildChaosSelector(requestCommand);
        ioChaosSpec.setSelector(selector);

        return IOChaos.builder()
                .apiVersion(Constants.API_VERSION)
                .kind("IOChaos")
                .metadata(v1ObjectMeta)
                .spec(ioChaosSpec).build();
    }

    public static Object buildDNSChaosBody(RequestCommand requestCommand, V1ObjectMeta v1ObjectMeta) {
        containersStrToArray(requestCommand);
        Map<String, String> arguments = requestCommand.getArguments();
        arguments.put("action", SceneCodeParseUtil.getAction(requestCommand.getSceneCode()));
        arguments.put("mode", "all");

        requestCommand.setArguments(arguments);

        String specJsonStr = argumentsToJsonStr(requestCommand.getArguments());
        JSONObject specJson = JSONObject.parseObject(specJsonStr);

        DNSChaosSpec dnsChaosSpec = JSON.toJavaObject(specJson, DNSChaosSpec.class);
        ChaosPodSelector selector = buildChaosSelector(requestCommand);
        dnsChaosSpec.setSelector(selector);

        return DNSChaos.builder()
                .apiVersion(Constants.API_VERSION)
                .kind("DNSChaos")
                .metadata(v1ObjectMeta)
                .spec(dnsChaosSpec).build();
    }

    public static Object buildTimeChaosBody(RequestCommand requestCommand, V1ObjectMeta v1ObjectMeta) {
        containersStrToArray(requestCommand);
        Map<String, String> arguments = requestCommand.getArguments();
        arguments.put("mode", "all");

        requestCommand.setArguments(arguments);

        String specJsonStr = argumentsToJsonStr(requestCommand.getArguments());
        JSONObject specJson = JSONObject.parseObject(specJsonStr);

        TimeChaosSpec timeChaosSpec = JSON.toJavaObject(specJson, TimeChaosSpec.class);
        ChaosPodSelector selector = buildChaosSelector(requestCommand);
        timeChaosSpec.setSelector(selector);

        return TimeChaos.builder()
                .apiVersion(Constants.API_VERSION)
                .kind("TimeChaos")
                .metadata(v1ObjectMeta)
                .spec(timeChaosSpec).build();
    }

    private static String argumentsToJsonStr(Map<String, String> arguments) {
        Properties p = new Properties();
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            String val =  entry.getValue();
            val = val.replaceAll("\"", "'");
            p.setProperty(entry.getKey(), val);
        }
        Config config = ConfigFactory.parseProperties(p);
        String s = config.root().render(ConfigRenderOptions.concise());
        s = s.replaceAll("\"\\[", "[");
        s = s.replaceAll("]\"", "]");
        return s;
    }

    private static void containersStrToArray(RequestCommand requestCommand) {
        Map<String, String> arguments = requestCommand.getArguments();
        if (arguments.containsKey("Containers")) {
            String containerNamesJsonStr = "[";
            String[] containers = arguments.get("Containers").split(",");
            for (int i = 0; i < containers.length - 1; i++) {
                containerNamesJsonStr += "\"";
                containerNamesJsonStr += containers[i];
                containerNamesJsonStr += "\",";
            }
            containerNamesJsonStr += "\"";
            containerNamesJsonStr += containers[containers.length - 1];
            containerNamesJsonStr += "\"]";
            arguments.put("containerNames", containerNamesJsonStr);
        }
        requestCommand.setArguments(arguments);
    }

    private static ChaosPodSelector buildChaosSelector(RequestCommand requestCommand) {
        String[] namespaces = requestCommand.getArguments().get("Namespaces").split(",");
        String[] pods = requestCommand.getArguments().get("Pods").split(",");
        Map<String, String[]> selectorMap = new HashMap<>();
        for(String namespace : namespaces) {
            selectorMap.put(namespace, pods);
        }
        ChaosPodSelector selector = new ChaosPodSelector();
        selector.setPods(selectorMap);
        return selector;
    }

}
