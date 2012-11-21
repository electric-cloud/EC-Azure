
// CreateDeploymentParameterPanel.java --
//
// CreateDeploymentParameterPanel.java is part of ElectricCommander.
//
// Copyright (c) 2005-2012 Electric Cloud, Inc.
// All rights reserved.
//

package ecplugins.azure.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import com.electriccloud.commander.client.ChainedCallback;
import com.electriccloud.commander.client.domain.ActualParameter;
import com.electriccloud.commander.client.domain.FormalParameter;
import com.electriccloud.commander.client.util.StringUtil;
import com.electriccloud.commander.gwt.client.ComponentBase;
import com.electriccloud.commander.gwt.client.ui.CustomValueCheckBox;
import com.electriccloud.commander.gwt.client.ui.FormBuilder;
import com.electriccloud.commander.gwt.client.ui.ParameterPanel;
import com.electriccloud.commander.gwt.client.ui.ParameterPanelProvider;
import com.electriccloud.commander.gwt.client.ui.ValuedListBox;

public class CreateDeploymentParameterPanel
    extends ComponentBase
    implements ParameterPanel,
        ParameterPanelProvider
{

    //~ Static fields/initializers ---------------------------------------------

    // ~ Static fields/initializers
    // ---------------------------------------------
    private static UiBinder<Widget, CreateDeploymentParameterPanel> s_binder =
        GWT.create(Binder.class);

    // These are all the formalParameters on the Procedure
    static final String CONNECTION_CONFIG        = "connection_config";
    static final String DEPLOYMENT_SLOT          = "deployment_slot";
    static final String SERVICE_NAME             = "service_name";
    static final String LABEL                    = "label";
    static final String DEPLOYMENT_NAME          = "deployment_name";
    static final String PACKAGE_URL              = "package_url";
    static final String DEPLOYMENT_CONFIGURATION = "deployment_configuration";
    static final String START_DEPLOYMENT         = "start_deployment";
    static final String TREAT_WARNINGS_AS_ERROR  = "treat_warnings_as_error";
    static final String NAME                     = "name";
    static final String VALUE                    = "value";
    static final String RESULTS_LOCATION_OUTPSP  = "results_location_outpsp";
    static final String TAG_OUTPP                = "tag_outpp";
    static final String JOB_STEP_TIMEOUT         = "job_step_timeout";
    static final String PRODUCTION               = "production";
    static final String STAGING                  = "staging";

    //~ Instance fields --------------------------------------------------------

    // ~ Instance fields
    // --------------------------------------------------------
    @UiField FormBuilder create_deployment_ParameterForm;

    //~ Methods ----------------------------------------------------------------

    // ~ Methods
    // ----------------------------------------------------------------
    /**
     * This function is called by SDK infrastructure to initialize the UI parts
     * of this component.
     *
     * @return  A widget that the infrastructure should place in the UI; usually
     *          a panel.
     */
    @Override public Widget doInit()
    {
        Widget              base            = s_binder.createAndBindUi(this);
        final ValuedListBox deployment_slot = getUIFactory()
                .createValuedListBox();

        deployment_slot.addItem(STAGING, STAGING);
        deployment_slot.addItem(PRODUCTION, PRODUCTION);

        final CustomValueCheckBox start_deployment        = getUIFactory()
                .createCustomValueCheckBox("true", "false");
        final CustomValueCheckBox treat_warnings_as_error = getUIFactory()
                .createCustomValueCheckBox("true", "false");
        final TextBox             name_box                = new TextBox();

        create_deployment_ParameterForm.addRow(true, "Configuration:",
            "The name of the configuration which holds all the connection information for Windows Azure.",
            CONNECTION_CONFIG, "", new TextBox());
        create_deployment_ParameterForm.addRow(true, "Deployment Slot:",
            "Specifies the environment in which to deploy the virtual machine. Possible values are: Staging, Production",
            DEPLOYMENT_SLOT, STAGING, deployment_slot);
        create_deployment_ParameterForm.addRow(true, "Hosted Service:",
            "Specifies the unique DNS Prefix value in the Windows Azure Management Portal. ",
            SERVICE_NAME, "", new TextBox());
        create_deployment_ParameterForm.addRow(true, "Deployment Name:",
            "Specifies the name for the deployment. The deployment name must be unique among other deployments for the hosted service. ",
            DEPLOYMENT_NAME, "", new TextBox());
        create_deployment_ParameterForm.addRow(true, "Package URL:",
            "A URL that refers to the location of the service package in the Blob service. ",
            PACKAGE_URL, "", new TextBox());
        create_deployment_ParameterForm.addRow(true,
            "Deployment Configuration:",
            "Specifies the service configuration file for the deployment. ",
            DEPLOYMENT_CONFIGURATION, "", new TextBox());
        create_deployment_ParameterForm.addRow(true, "Label:",
            "A name for the hosted service. ", LABEL, "", new TextBox());
        create_deployment_ParameterForm.addRow(false, "Start Deployment?:",
            "Indicates whether to start the deployment immediately after it is created.",
            START_DEPLOYMENT, "false", start_deployment);
        create_deployment_ParameterForm.addRow(false,
            "Treat Warnings As Error?:",
            "Indicates whether to treat package validation warnings as errors. ",
            TREAT_WARNINGS_AS_ERROR, "false", treat_warnings_as_error);
        create_deployment_ParameterForm.addRow(false, "Name:",
            "Represents the name of an extended deployment property. ", NAME,
            "", name_box);
        create_deployment_ParameterForm.addRow(false, "Value:",
            "Represents the value of an extended hosted service property. ",
            VALUE, "", new TextBox());
        create_deployment_ParameterForm.addRow(true,
            "Results location (output property sheet path):",
            "The ElectricCommander location to store properties (default is /myJob/Azure/deployed). ",
            RESULTS_LOCATION_OUTPSP, "/myJob/Azure/deployed", new TextBox());
        create_deployment_ParameterForm.addRow(true,
            "Results tag (output property name):",
            "The unique tag for this operation to keep it separate from others stored in the same results location. (default is $[jobStepId]). ",
            TAG_OUTPP, "$[jobStepId]", new TextBox());
        create_deployment_ParameterForm.addRow(false, "JobStep Timeout:",
            "Timeout for the step execution in minutes. Blank means no timeout. ",
            JOB_STEP_TIMEOUT, "", new TextBox());
        name_box.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override public void onValueChange(
                        ValueChangeEvent<String> event)
                {
                    updateRowVisibility();
                }
            });
        updateRowVisibility();

        return base;
    }

    /**
     * Performs validation of user supplied data before submitting the form.
     *
     * <p>This function is called after the user hits submit.</p>
     *
     * @return  true if checks succeed, false otherwise
     */
    @Override public boolean validate()
    {
        boolean validationStatus = create_deployment_ParameterForm.validate();

        // CONNECTION_CONFIG required.
        if (StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(
                                                   CONNECTION_CONFIG)
                                               .trim())) {
            create_deployment_ParameterForm.setErrorMessage(CONNECTION_CONFIG,
                "This Field is required.");
            validationStatus = false;
        }

        // DEPLOYMENT_SLOT required.
        if (StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(
                                                   DEPLOYMENT_SLOT)
                                               .trim())) {
            create_deployment_ParameterForm.setErrorMessage(DEPLOYMENT_SLOT,
                "This Field is required.");
            validationStatus = false;
        }

        // SERVICE_NAME required.
        if (StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(SERVICE_NAME)
                                               .trim())) {
            create_deployment_ParameterForm.setErrorMessage(SERVICE_NAME,
                "This Field is required.");
            validationStatus = false;
        }

        // DEPLOYMENT_NAME required.
        if (StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(
                                                   DEPLOYMENT_NAME)
                                               .trim())) {
            create_deployment_ParameterForm.setErrorMessage(DEPLOYMENT_NAME,
                "This Field is required.");
            validationStatus = false;
        }

        // PACKAGE_URL required.
        if (StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(PACKAGE_URL)
                                               .trim())) {
            create_deployment_ParameterForm.setErrorMessage(PACKAGE_URL,
                "This Field is required.");
            validationStatus = false;
        }

        // DEPLOYMENT_CONFIGURATION required.
        if (StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(
                                                   DEPLOYMENT_CONFIGURATION)
                                               .trim())) {
            create_deployment_ParameterForm.setErrorMessage(
                DEPLOYMENT_CONFIGURATION, "This Field is required.");
            validationStatus = false;
        }

        // LABEL required.
        if (StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(LABEL)
                                               .trim())) {
            create_deployment_ParameterForm.setErrorMessage(LABEL,
                "This Field is required.");
            validationStatus = false;
        }

        // VALUE is required if NAME is specified.
        if (StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(VALUE)
                                               .trim())
                & !StringUtil.isEmpty(
                    create_deployment_ParameterForm.getValue(NAME)
                                               .trim())) {
            create_deployment_ParameterForm.setErrorMessage(VALUE,
                "This Field is required if 'Name' is provided.");
            validationStatus = false;
        }

        return validationStatus;
    }

    protected void updateRowVisibility()
    {
        create_deployment_ParameterForm.setRowVisible(VALUE,
            !StringUtil.isEmpty(
                create_deployment_ParameterForm.getValue(NAME)
                                               .trim()));
    }

    /**
     * This method is used by UIBinder to embed FormBuilder's in the UI.
     *
     * @return  a new FormBuilder.
     */
    @UiFactory FormBuilder createFormBuilder()
    {
        return getUIFactory().createFormBuilder();
    }

    @Override public ParameterPanel getParameterPanel()
    {
        return this;
    }

    /**
     * Gets the values of the parameters that should map 1-to-1 to the formal
     * parameters on the object being called. Transform user input into a map of
     * parameter names and values.
     *
     * <p>This function is called after the user hits submit and validation has
     * succeeded.</p>
     *
     * @return  The values of the parameters that should map 1-to-1 to the
     *          formal parameters on the object being called.
     */
    @Override public Map<String, String> getValues()
    {
        Map<String, String> actualParams                 = new HashMap<String,
                String>();
        Map<String, String> create_deployment_FormValues =
            create_deployment_ParameterForm.getValues();

        actualParams.put(CONNECTION_CONFIG,
            create_deployment_FormValues.get(CONNECTION_CONFIG));
        actualParams.put(DEPLOYMENT_SLOT,
            create_deployment_FormValues.get(DEPLOYMENT_SLOT));
        actualParams.put(SERVICE_NAME,
            create_deployment_FormValues.get(SERVICE_NAME));
        actualParams.put(DEPLOYMENT_NAME,
            create_deployment_FormValues.get(DEPLOYMENT_NAME));
        actualParams.put(PACKAGE_URL,
            create_deployment_FormValues.get(PACKAGE_URL));
        actualParams.put(DEPLOYMENT_CONFIGURATION,
            create_deployment_FormValues.get(DEPLOYMENT_CONFIGURATION));
        actualParams.put(LABEL, create_deployment_FormValues.get(LABEL));
        actualParams.put(START_DEPLOYMENT,
            create_deployment_FormValues.get(START_DEPLOYMENT));
        actualParams.put(TREAT_WARNINGS_AS_ERROR,
            create_deployment_FormValues.get(TREAT_WARNINGS_AS_ERROR));
        actualParams.put(NAME, create_deployment_FormValues.get(NAME));
        actualParams.put(VALUE, create_deployment_FormValues.get(VALUE));
        actualParams.put(RESULTS_LOCATION_OUTPSP,
            create_deployment_FormValues.get(RESULTS_LOCATION_OUTPSP));
        actualParams.put(TAG_OUTPP,
            create_deployment_FormValues.get(TAG_OUTPP));
        actualParams.put(JOB_STEP_TIMEOUT,
            create_deployment_FormValues.get(JOB_STEP_TIMEOUT));

        return actualParams;
    }

    /**
     * Push actual parameters into the panel implementation.
     *
     * <p>This is used when editing an existing object to show existing content.
     * </p>
     *
     * @param  actualParameters  Actual parameters assigned to this list of
     *                           parameters.
     */
    @Override public void setActualParameters(
            Collection<ActualParameter> actualParameters)
    {

        if (actualParameters == null) {
            return;
        }

        // First load the parameters into a map. Makes it easier to
        // update the form by querying for various params randomly.
        Map<String, String> params = new HashMap<String, String>();

        for (ActualParameter p : actualParameters) {
            params.put(p.getName(), p.getValue());
        }

        // Do the easy form elements first.
        for (String key : new String[] {
                    CONNECTION_CONFIG,
                    SERVICE_NAME,
                    DEPLOYMENT_SLOT,
                    DEPLOYMENT_NAME,
                    PACKAGE_URL,
                    DEPLOYMENT_CONFIGURATION,
                    LABEL,
                    START_DEPLOYMENT,
                    TREAT_WARNINGS_AS_ERROR,
                    NAME,
                    VALUE,
                    RESULTS_LOCATION_OUTPSP,
                    TAG_OUTPP,
                    JOB_STEP_TIMEOUT
                }) {
            create_deployment_ParameterForm.setValue(key,
                StringUtil.nullToEmpty(params.get(key)));
        }

        String start_deployment = params.get("START_DEPLOYMENT");

        if (start_deployment == null) {
            start_deployment = "0";
        }

        create_deployment_ParameterForm.setValue("START_DEPLOYMENT",
            start_deployment);

        String treat_warnings_as_error = params.get("TREAT_WARNINGS_AS_ERROR");

        if (treat_warnings_as_error == null) {
            treat_warnings_as_error = "0";
        }

        create_deployment_ParameterForm.setValue("TREAT_WARNINGS_AS_ERROR",
            treat_warnings_as_error);
        updateRowVisibility();
    }

    /**
     * Push form parameters into the panel implementation.
     *
     * <p>This is used when creating a new object and showing default values.
     * </p>
     *
     * @param  formalParameters  Formal parameters on the target object.
     */
    @Override public void setFormalParameters(
            Collection<FormalParameter> formalParameters) { }

    //~ Inner Interfaces -------------------------------------------------------

    // ~ Inner Interfaces
    // -------------------------------------------------------
    // ~ Inner Interfaces
    // -------------------------------------------------------
    interface Binder
        extends UiBinder<Widget, CreateDeploymentParameterPanel> { }
}
