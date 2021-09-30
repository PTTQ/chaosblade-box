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

import com.alibaba.chaosblade.box.common.constants.ChaosConstant;
import com.alibaba.chaosblade.box.common.enums.ChaosTools;
import com.alibaba.chaosblade.box.common.enums.DeviceType;
import com.alibaba.chaosblade.box.common.utils.SceneCodeParseUtil;
import com.alibaba.chaosblade.box.invoker.ChaosInvokerStrategy;
import com.alibaba.chaosblade.box.invoker.RequestCommand;
import com.alibaba.chaosblade.box.invoker.ResponseCommand;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author luo rongzhou
 */
@Slf4j
@ChaosInvokerStrategy(value = ChaosTools.CHAOS_MESH,
        deviceType = {
                DeviceType.NODE, DeviceType.POD
        },
        phase = ChaosConstant.PHASE_RECOVER)
@Component
public class ChaosMeshRecoverChaosInvoker extends AbstractChaosMeshChaosInvoker {
    @Override
    public CompletableFuture<ResponseCommand> invoke(RequestCommand requestCommand) {
        CompletableFuture<ResponseCommand> completableFuture = new CompletableFuture<>();

        try {
            CustomObjectsApi apiInstance = new CustomObjectsApi(getClient(requestCommand));

            String target = SceneCodeParseUtil.getTarget(requestCommand.getSceneCode());
            String experimentType = target.split("-")[1];

            String plural = "";

            switch (experimentType) {
                case "pod":
                    plural = "podchaos";
                    break;
                case "network":
                    plural = "networkchaos";
                    break;
                case "stress":
                    plural = "stresschaos";
                    break;
                case "file":
                    plural = "iochaos";
                    break;
                case "dns":
                    plural = "dnschaos";
                    break;
                case "time":
                    plural = "timechaos";
                    break;
            }

            apiInstance.deleteNamespacedCustomObjectAsync(
                    Constants.GROUP,
                    Constants.VERSION,
                    Constants.NAMESPACE,
                    plural,
                    requestCommand.getName(),
                    10,
                    false,
                    null,
                    null,
                    null,
                    new ApiCallback() {
                        @Override
                        public void onFailure(ApiException e, int statusCode, Map responseHeaders) {
                            ResponseCommand responseCommand;
                            if (statusCode == 404) {
                                responseCommand = ResponseCommand.builder().success(true)
                                        .result(requestCommand.getName()).build();
                            } else {
                                responseCommand = ResponseCommand.builder()
                                        .success(false)
                                        .code(String.valueOf(statusCode))
                                        .result(e.getMessage())
                                        .error(e.getResponseBody())
                                        .build();
                            }
                            completableFuture.complete(responseCommand);
                        }

                        @Override
                        public void onSuccess(Object result, int statusCode, Map responseHeaders) {
                            ResponseCommand responseCommand = ResponseCommand.builder()
                                    .code(String.valueOf(statusCode))
                                    .success(true)
                                    .result(requestCommand.getName())
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
        } catch (IOException e) {
            completableFuture.completeExceptionally(e);
        }
        return completableFuture;
    }
}
