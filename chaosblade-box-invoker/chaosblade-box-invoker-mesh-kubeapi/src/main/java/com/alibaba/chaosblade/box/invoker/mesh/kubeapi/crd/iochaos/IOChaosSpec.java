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

package com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.iochaos;

import com.alibaba.chaosblade.box.invoker.mesh.kubeapi.crd.ChaosPodSelector;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * @author luo rongzhou
 */
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class IOChaosSpec {

    private String delay;

    private int errno;

    private String path;

    private int percent;

    private String volumePath;

    private String action;

    private String duration;

    private AttrOverrideSpec attr;

    private MistakeSpec mistake;

    private String[] methods;

    private String[] containerNames;

    private String mode;

    private ChaosPodSelector selector;

    @Tolerate
    public IOChaosSpec() {}

}
