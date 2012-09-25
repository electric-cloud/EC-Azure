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

public class ParameterPanelFactory extends ComponentBaseFactory {
    private static String s_componentName;

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
