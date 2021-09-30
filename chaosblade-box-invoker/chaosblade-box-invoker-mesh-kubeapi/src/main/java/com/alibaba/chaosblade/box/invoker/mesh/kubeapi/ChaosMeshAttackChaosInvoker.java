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

import cn.hutool.core.util.IdUtil;
import com.alibaba.chaosblade.box.common.constants.ChaosConstant;
import com.alibaba.chaosblade.box.common.enums.ChaosTools;
import com.alibaba.chaosblade.box.common.enums.DeviceType;
import com.alibaba.chaosblade.box.common.utils.JsonUtils;
import com.alibaba.chaosblade.box.common.utils.SceneCodeParseUtil;
import com.alibaba.chaosblade.box.invoker.ChaosInvokerStrategy;
import com.alibaba.chaosblade.box.invoker.RequestCommand;
import com.alibaba.chaosblade.box.invoker.ResponseCommand;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Config;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author luo rongzhou
 */
@ChaosInvokerStrategy(value = ChaosTools.CHAOS_MESH,
        deviceType = {
                DeviceType.NODE, DeviceType.POD
        },
        phase = ChaosConstant.PHASE_ATTACK)
@Component
public class ChaosMeshAttackChaosInvoker extends AbstractChaosMeshChaosInvoker {

    @Override
    public CompletableFuture<ResponseCommand> invoke(RequestCommand requestCommand) {

        CustomObjectsApi apiInstance;
        final CompletableFuture<ResponseCommand> completableFuture = new CompletableFuture<>();

        try {
            apiInstance = new CustomObjectsApi(getClient(requestCommand));
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(IdUtil.fastSimpleUUID());

            String target = SceneCodeParseUtil.getTarget(requestCommand.getSceneCode());
            String experimentType = target.split("-")[1];

            Object body = new Object();
            String plural = "";

            switch (experimentType) {
                case "pod":
                    body = ChaosBodyBuilder.buildPodChaosBody(requestCommand, v1ObjectMeta);
                    plural = "podchaos";
                    break;
                case "network":
                    body = ChaosBodyBuilder.buildNetworkChaosBody(requestCommand, v1ObjectMeta);
                    plural = "networkchaos";
                    break;
                case "stress":
                    body = ChaosBodyBuilder.buildStressChaosBody(requestCommand, v1ObjectMeta);
                    plural = "stresschaos";
                    break;
                case "file":
                    body = ChaosBodyBuilder.buildIOChaosBody(requestCommand, v1ObjectMeta);
                    plural = "iochaos";
                    break;
                case "dns":
                    body = ChaosBodyBuilder.buildDNSChaosBody(requestCommand, v1ObjectMeta);
                    plural = "dnschaos";
                    break;
                case "time":
                    body = ChaosBodyBuilder.buildTimeChaosBody(requestCommand, v1ObjectMeta);
                    plural = "timechaos";
                    break;
            }

            apiInstance.createNamespacedCustomObjectAsync(
                    Constants.GROUP,
                    Constants.VERSION,
                    Constants.NAMESPACE,
                    plural,
                    body,
                    "true",
                    null,
                    null,
                    new ApiCallback() {
                        @Override
                        public void onFailure(ApiException e, int statusCode, Map responseHeaders) {
                            ResponseCommand responseCommand = ResponseCommand.builder()
                                    .success(false)
                                    .code(String.valueOf(statusCode))
                                    .result(e.getMessage())
                                    .error(e.getResponseBody())
                                    .build();
                            completableFuture.complete(responseCommand);
                        }

                        @Override
                        public void onSuccess(Object result, int statusCode, Map responseHeaders) {
                            ResponseCommand responseCommand = ResponseCommand.builder()
                                    .code(String.valueOf(statusCode))
                                    .success(true)
                                    .result(v1ObjectMeta.getName())
                                    .build();
                            completableFuture.complete(responseCommand);
                        }

                        @Override
                        public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {

                        }

                        @Override
                        public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {

                        }
                    }
            );

        } catch (ApiException e) {
            ResponseCommand responseCommand = ResponseCommand.builder()
                    .success(false)
                    .code(String.valueOf(e.getCode()))
                    .result(e.getMessage())
                    .error(e.getResponseBody())
                    .build();
            completableFuture.complete(responseCommand);
        } catch (Exception e) {
            completableFuture.completeExceptionally(e);
        }

        return completableFuture;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        client = Config.defaultClient();
    }
}
