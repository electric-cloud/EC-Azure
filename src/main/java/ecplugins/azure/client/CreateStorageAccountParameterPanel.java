// CreateStorageAccountParameterPanel.java --
//
// CreateStorageAccountParameterPanel.java is part of ElectricCommander.
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
import com.electriccloud.commander.gwt.client.ui.HasInitCallback;
import com.electriccloud.commander.gwt.client.ui.ParameterPanel;
import com.electriccloud.commander.gwt.client.ui.ParameterPanelProvider;

public class CreateStorageAccountParameterPanel extends ComponentBase implements
        ParameterPanel, ParameterPanelProvider, HasInitCallback {
    // ~ Static fields/initializers
    // ---------------------------------------------
    private static UiBinder<Widget, CreateStorageAccountParameterPanel> s_binder = GWT
            .create(Binder.class);

    // These are all the formalParameters on the Procedure

    static final String CONNECTION_CONFIG = "connection_config";
    static final String SERVICE_NAME = "service_name";
    static final String LABEL = "label";
    static final String DESCRIPTION = "description";
    static final String LOCATION = "location";
    static final String AFFINITY_GROUP = "affinity_group";
    static final String GEO_REPLICATION_ENABLED = "geo_replication_enabled";
    static final String NAME = "name";
    static final String VALUE = "value";
    static final String RESULTS_LOCATION_OUTPSP = "results_location_outpsp";
    static final String TAG_OUTPP = "tag_outpp";
    static final String JOB_STEP_TIMEOUT = "job_step_timeout";

    // ~ Instance fields
    // --------------------------------------------------------
    @UiField
    FormBuilder create_storage_account_ParameterForm;

    // ~ Methods
    // ----------------------------------------------------------------

    /**
     * This function is called by SDK infrastructure to initialize the UI parts
     * of this component.
     * 
     * @return A widget that the infrastructure should place in the UI; usually
     *         a panel.
     */
    @Override
    public Widget doInit() {
        Widget base = s_binder.createAndBindUi(this);

        final CustomValueCheckBox geo_replication = getUIFactory()
                .createCustomValueCheckBox("1", "0");

        final TextBox location_box = new TextBox();
        final TextBox affinity_box = new TextBox();
        final TextBox name_box = new TextBox();

        create_storage_account_ParameterForm
                .addRow(true,
                        "Configuration:",
                        "The name of the configuration which holds all the connection information for Windows Azure. ",
                        CONNECTION_CONFIG, "", new TextBox());
        create_storage_account_ParameterForm
                .addRow(true,
                        "Storage Account Name:",
                        "A name for the storage account that is unique within Windows Azure. This name is the DNS prefix name and can be used to access blobs, queues, and tables in the storage account. ",
                        SERVICE_NAME, "", new TextBox());
        create_storage_account_ParameterForm
                .addRow(true,
                        "Label:",
                        "A label for the storage account. The name can be used identify the storage account for your tracking purposes. ",
                        LABEL, "", new TextBox());
        create_storage_account_ParameterForm.addRow(false, "Description:",
                "A description for the storage account. ", DESCRIPTION, "",
                new TextBox());
        create_storage_account_ParameterForm
                .addRow(false,
                        "Location:",
                        "The location where the storage account is created. Required if 'Affinity Group' is not specified.",
                        LOCATION, "", location_box);
        create_storage_account_ParameterForm
                .addRow(false,
                        "Affinity Group:",
                        "The name of an existing affinity group in the specified subscription. Required if Location is not specified. ",
                        AFFINITY_GROUP, "", affinity_box);
        create_storage_account_ParameterForm
                .addRow(false,
                        "Enable Geo Replication:",
                        "Specifies whether the storage account is created with the geo-replication enabled.  ",
                        GEO_REPLICATION_ENABLED, "0", geo_replication);
        create_storage_account_ParameterForm
                .addRow(false,
                        "Name:",
                        "Represents the name of an extended storage account property. ",
                        NAME, "", name_box);
        create_storage_account_ParameterForm
                .addRow(false,
                        "Value:",
                        "Represents the value of an extended storage account property. ",
                        VALUE, "", new TextBox());
        create_storage_account_ParameterForm
                .addRow(true,
                        "Results location (output property sheet path):",
                        "The ElectricCommander location to store properties (default is /myJob/Azure/deployed). ",
                        RESULTS_LOCATION_OUTPSP, "/myJob/Azure/deployed",
                        new TextBox());
        create_storage_account_ParameterForm
                .addRow(true,
                        "Results tag (output property name):",
                        "The unique tag for this operation to keep it separate from others stored in the same results location. (default is $[jobStepId]). ",
                        TAG_OUTPP, "$[jobStepId]",
                        new TextBox());
        create_storage_account_ParameterForm
                .addRow(false,
                        "JobStep Timeout:",
                        "Timeout for the step execution in minutes. Blank means no timeout. ",
                        JOB_STEP_TIMEOUT, "", new TextBox());

        location_box.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                updateRowVisibility();
            }
        });
        affinity_box.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                updateRowVisibility();
            }
        });
        name_box.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                updateRowVisibility();
            }
        });

        updateRowVisibility();

        return base;
    }

    /**
     * Called when all the Commander Requests have returned, similar to
     * {@link ChainedCallback#onComplete()}.
     */
    @Override
    public void onInitComplete() {
    }

    /**
     * Performs validation of user supplied data before submitting the form.
     * 
     * <p>
     * This function is called after the user hits submit.
     * </p>
     * 
     * @return true if checks succeed, false otherwise
     */
    @Override
    public boolean validate() {
        boolean validationStatus = create_storage_account_ParameterForm
                .validate();

        // CONNECTION_CONFIG required.
        if (StringUtil.isEmpty(create_storage_account_ParameterForm.getValue(CONNECTION_CONFIG).trim())) {
            create_storage_account_ParameterForm
                    .setErrorMessage(CONNECTION_CONFIG,
                            "This Field is required.");
            validationStatus = false;
        }
        
        // LABEL required.
        if (StringUtil.isEmpty(create_storage_account_ParameterForm.getValue(LABEL).trim())) {
            create_storage_account_ParameterForm
                    .setErrorMessage(LABEL,
                            "This Field is required.");
            validationStatus = false;
        }
        
        // SERVICE_NAME required.
        if (StringUtil.isEmpty(create_storage_account_ParameterForm.getValue(SERVICE_NAME).trim())) {
            create_storage_account_ParameterForm
                    .setErrorMessage(SERVICE_NAME,
                            "This Field is required.");
            validationStatus = false;
        }
        
        // LOCATION required if AFFINITY_GROUP is not specified.
        if (StringUtil.isEmpty(create_storage_account_ParameterForm.getValue(
                LOCATION).trim())
                & StringUtil.isEmpty(create_storage_account_ParameterForm
                        .getValue(AFFINITY_GROUP).trim())) {
            create_storage_account_ParameterForm
                    .setErrorMessage(LOCATION,
                            "This Field is required if 'Affinity Group' is not specified.");
            validationStatus = false;
        }

        // AFFINITY_GROUP LOCATION is not specified.
        if (StringUtil.isEmpty(create_storage_account_ParameterForm.getValue(
                AFFINITY_GROUP).trim())
                & StringUtil.isEmpty(create_storage_account_ParameterForm
                        .getValue(LOCATION).trim())) {
            create_storage_account_ParameterForm.setErrorMessage(
                    AFFINITY_GROUP,
                    "This Field is required if 'Location' is not specified.");
            validationStatus = false;
        }

        // VALUE is required if NAME is specified.
        if (StringUtil.isEmpty(create_storage_account_ParameterForm.getValue(
                VALUE).trim())
                & !StringUtil.isEmpty(create_storage_account_ParameterForm
                        .getValue(NAME).trim())) {
            create_storage_account_ParameterForm.setErrorMessage(VALUE,
                    "This Field is required if 'Name' is provided.");
            validationStatus = false;
        }

        return validationStatus;
    }

    protected void updateRowVisibility() {

        create_storage_account_ParameterForm.setRowVisible(AFFINITY_GROUP,
                StringUtil.isEmpty(create_storage_account_ParameterForm
                        .getValue(LOCATION).trim()));
        create_storage_account_ParameterForm.setRowVisible(LOCATION, StringUtil
                .isEmpty(create_storage_account_ParameterForm.getValue(
                        AFFINITY_GROUP).trim()));
        create_storage_account_ParameterForm.setRowVisible(VALUE, !StringUtil
                .isEmpty(create_storage_account_ParameterForm.getValue(NAME)
                        .trim()));

    }

    /**
     * This method is used by UIBinder to embed FormBuilder's in the UI.
     * 
     * @return a new FormBuilder.
     */
    @UiFactory
    FormBuilder createFormBuilder() {
        return getUIFactory().createFormBuilder();
    }

    @Override
    public ParameterPanel getParameterPanel() {
        return this;
    }

    /**
     * Gets the values of the parameters that should map 1-to-1 to the formal
     * parameters on the object being called. Transform user input into a map of
     * parameter names and values.
     * 
     * <p>
     * This function is called after the user hits submit and validation has
     * succeeded.
     * </p>
     * 
     * @return The values of the parameters that should map 1-to-1 to the formal
     *         parameters on the object being called.
     */
    @Override
    public Map<String, String> getValues() {
        Map<String, String> actualParams = new HashMap<String, String>();
        Map<String, String> create_storage_account_FormValues = create_storage_account_ParameterForm
                .getValues();

        actualParams.put(CONNECTION_CONFIG,
                create_storage_account_FormValues.get(CONNECTION_CONFIG));
        actualParams.put(SERVICE_NAME,
                create_storage_account_FormValues.get(SERVICE_NAME));
        actualParams.put(LABEL, create_storage_account_FormValues.get(LABEL));
        actualParams.put(DESCRIPTION,
                create_storage_account_FormValues.get(DESCRIPTION));
        actualParams.put(LOCATION,
                create_storage_account_FormValues.get(LOCATION));
        actualParams.put(AFFINITY_GROUP,
                create_storage_account_FormValues.get(AFFINITY_GROUP));
        actualParams.put(GEO_REPLICATION_ENABLED,
                create_storage_account_FormValues.get(GEO_REPLICATION_ENABLED));
        actualParams.put(NAME, create_storage_account_FormValues.get(NAME));
        actualParams.put(VALUE, create_storage_account_FormValues.get(VALUE));
        actualParams.put(RESULTS_LOCATION_OUTPSP,
                create_storage_account_FormValues.get(RESULTS_LOCATION_OUTPSP));
        actualParams.put(TAG_OUTPP,
                create_storage_account_FormValues.get(TAG_OUTPP));
        actualParams.put(JOB_STEP_TIMEOUT,
                create_storage_account_FormValues.get(JOB_STEP_TIMEOUT));

        return actualParams;
    }

    /**
     * Push actual parameters into the panel implementation.
     * 
     * <p>
     * This is used when editing an existing object to show existing content.
     * </p>
     * 
     * @param actualParameters
     *            Actual parameters assigned to this list of parameters.
     */
    @Override
    public void setActualParameters(Collection<ActualParameter> actualParameters) {

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
        for (String key : new String[] { CONNECTION_CONFIG, SERVICE_NAME,
                LABEL, DESCRIPTION, LOCATION, AFFINITY_GROUP,
                GEO_REPLICATION_ENABLED, NAME, VALUE, RESULTS_LOCATION_OUTPSP, TAG_OUTPP,
                JOB_STEP_TIMEOUT }) {
            create_storage_account_ParameterForm.setValue(key,
                    StringUtil.nullToEmpty(params.get(key)));
        }

        String geo_replication = params.get("GEO_REPLICATION_ENABLED");

        if (geo_replication == null) {
            geo_replication = "0";
        }

        create_storage_account_ParameterForm.setValue(
                "GEO_REPLICATION_ENABLED", geo_replication);

        updateRowVisibility();
    }

    /**
     * Push form parameters into the panel implementation.
     * 
     * <p>
     * This is used when creating a new object and showing default values.
     * </p>
     * 
     * @param formalParameters
     *            Formal parameters on the target object.
     */
    @Override
    public void setFormalParameters(Collection<FormalParameter> formalParameters) {
    }

    // ~ Inner Interfaces
    // -------------------------------------------------------

    // ~ Inner Interfaces
    // -------------------------------------------------------
    interface Binder extends
            UiBinder<Widget, CreateStorageAccountParameterPanel> {
    }
}
