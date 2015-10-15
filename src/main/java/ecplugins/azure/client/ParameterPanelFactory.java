/**
 *  Copyright 2015 Electric Cloud, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
// ParameterPanelFactory.java --
//
// ParameterPanelFactory.java is part of ElectricCommander.
//
// Copyright (c) 2005-2011 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.azure.client;

import com.electriccloud.commander.gwt.client.Component;
import com.electriccloud.commander.gwt.client.ComponentBaseFactory;
import com.electriccloud.commander.gwt.client.ComponentContext;

import com.google.gwt.core.client.GWT;
import org.jetbrains.annotations.NotNull;

public class ParameterPanelFactory extends ComponentBaseFactory {
    private static String s_componentName;

    @NotNull
    @Override
    public Component createComponent(ComponentContext jso) {
        Component childComponent = null;

        s_componentName = GWT.getModuleName();
        s_componentName = s_componentName.substring(s_componentName
                .lastIndexOf('.') + 1);

        if ("CreateHostedServiceParameterPanel".equals(s_componentName)) {
            childComponent = new CreateHostedServiceParameterPanel();
        } else if ("CreateStorageAccountParameterPanel".equals(s_componentName)) {
            childComponent = new CreateStorageAccountParameterPanel();
        } else if ("CreateDeploymentParameterPanel".equals(s_componentName)) {
            childComponent = new CreateDeploymentParameterPanel();
        } else if ("PutBlobParameterPanel".equals(s_componentName)) {
            childComponent = new PutBlobParameterPanel();
        }         

        return childComponent;
    }
}
