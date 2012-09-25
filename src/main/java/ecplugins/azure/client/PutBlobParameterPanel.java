// PutBlobParameterPanel.java --
//
// PutBlobParameterPanel.java is part of ElectricCommander.
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
import com.google.gwt.user.client.ui.TextArea;
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
import com.electriccloud.commander.gwt.client.ui.ValuedListBox;

public class PutBlobParameterPanel extends ComponentBase implements
        ParameterPanel, ParameterPanelProvider, HasInitCallback {
    // ~ Static fields/initializers
    // ---------------------------------------------
    private static UiBinder<Widget, PutBlobParameterPanel> s_binder = GWT
            .create(Binder.class);

    // These are all the formalParameters on the Procedure
    static final String CONNECTION_CONFIG = "connection_config";
    static final String STORAGE_ACCOUNT = "storage_account";
    static final String STORAGE_ACCOUNT_KEY = "storage_account_key";
    static final String CONTAINER_NAME = "container_name";
    static final String BLOB_TYPE = "blob_type";
    static final String UPLOAD_FILE = "upload_file";
    static final String FILEPATH = "filepath";
    static final String BLOB_NAME = "blob_name";
    static final String BLOB_CONTENT_LENGTH = "blob_content_length";
    static final String BLOB_SEQUENCE_NUMBER = "blob_sequence_number";
    static final String BLOB_CONTENT = "blob_content";
    static final String RESULTS_LOCATION_OUTPSP = "results_location_outpsp";
    static final String TAG_OUTPP = "tag_outpp";
    static final String JOB_STEP_TIMEOUT = "job_step_timeout";

    static final String BLOCK_BLOB = "BlockBlob";
    static final String PAGE_BLOB = "PageBlob";

    // ~ Instance fields
    // --------------------------------------------------------
    @UiField
    FormBuilder put_blob_ParameterForm;

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

        final ValuedListBox blob_type = getUIFactory().createValuedListBox();

        blob_type.addItem(BLOCK_BLOB, BLOCK_BLOB);
        blob_type.addItem(PAGE_BLOB, PAGE_BLOB);

        final CustomValueCheckBox upload_file = getUIFactory()
                .createCustomValueCheckBox("1", "0");

        final TextBox sequence_number = new TextBox();

        put_blob_ParameterForm
                .addRow(true,
                        "Configuration:",
                        "The name of the configuration which holds all the connection information for Windows Azure.",
                        CONNECTION_CONFIG, "", new TextBox());

        put_blob_ParameterForm
                .addRow(true,
                        "Storage Account:",
                        "Specifies the name of the storage account that is unique within Windows Azure. ",
                        STORAGE_ACCOUNT, "", new TextBox());

        put_blob_ParameterForm.addRow(true, "Storage Account Key:",
                "The primary access key for the storage account. ",
                STORAGE_ACCOUNT_KEY, "", new TextBox());

        put_blob_ParameterForm
                .addRow(true,
                        "Container Name:",
                        "Specifies the name of the container. Value can only include lower-case characters. ",
                        CONTAINER_NAME, "", new TextBox());

        put_blob_ParameterForm
                .addRow(true,
                        "Blob Type:",
                        "Specifies the type of blob to create: block blob or page blob. ",
                        BLOB_TYPE, BLOCK_BLOB, blob_type);

        put_blob_ParameterForm.addRow(false, "Upload File:",
                "Upload a file to the current storage account. ", UPLOAD_FILE,
                "0", upload_file);

        put_blob_ParameterForm.addRow(false, "Filepath:",
                "Path to the file to upload. ", FILEPATH, "", new TextBox());

        put_blob_ParameterForm
                .addRow(false,
                        "Blob Name:",
                        "Specifies the name for the blob. Value can only include lower-case characters. ",
                        BLOB_NAME, "", new TextBox());

        put_blob_ParameterForm
                .addRow(false,
                        "Blob content length:",
                        "Specifies the maximum size for the page blob, up to 1 TB. The page blob size must be aligned to a 512-byte boundary. ",
                        BLOB_CONTENT_LENGTH, "", new TextBox());

        put_blob_ParameterForm
                .addRow(false,
                        "Blob sequence number:",
                        "The sequence number is a user-controlled value that you can use to track requests. ",
                        BLOB_SEQUENCE_NUMBER, "", sequence_number);
        put_blob_ParameterForm.addRow(false, "Blob Content:",
                "The content of the blob.", BLOB_CONTENT, "", new TextArea());
        put_blob_ParameterForm
                .addRow(true,
                        "Results location (output property sheet path):",
                        "The ElectricCommander location to store properties (default is /myJob/Azure/deployed). ",
                        RESULTS_LOCATION_OUTPSP, "/myJob/Azure/deployed",
                        new TextBox());
        put_blob_ParameterForm
                .addRow(true,
                        "Results tag (output property name):",
                        "The unique tag for this operation to keep it separate from others stored in the same results location. (default is $[jobStepId]). ",
                        TAG_OUTPP, "$[jobStepId]",
                        new TextBox());
        put_blob_ParameterForm
                .addRow(false,
                        "JobStep Timeout:",
                        "Timeout for the step execution in minutes. Blank means no timeout. ",
                        JOB_STEP_TIMEOUT, "", new TextBox());

        blob_type.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                updateRowVisibility();
            }
        });

        upload_file.addValueChangeHandler(new ValueChangeHandler<String>() {
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
        boolean validationStatus = put_blob_ParameterForm.validate();
        String type = put_blob_ParameterForm.getValue(BLOB_TYPE);
        String file = put_blob_ParameterForm.getValue(UPLOAD_FILE);

        // CONNECTION_CONFIG required.
        if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(
                CONNECTION_CONFIG).trim())) {
            put_blob_ParameterForm.setErrorMessage(CONNECTION_CONFIG,
                    "This Field is required.");
            validationStatus = false;
        }

        // STORAGE_ACCOUNT required.
        if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(STORAGE_ACCOUNT)
                .trim())) {
            put_blob_ParameterForm.setErrorMessage(STORAGE_ACCOUNT,
                    "This Field is required.");
            validationStatus = false;
        }

        // STORAGE_ACCOUNT_KEY required.
        if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(
                STORAGE_ACCOUNT_KEY).trim())) {
            put_blob_ParameterForm.setErrorMessage(STORAGE_ACCOUNT,
                    "This Field is required.");
            validationStatus = false;
        }

        // CONTAINER_NAME required.
        if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(CONTAINER_NAME)
                .trim())) {
            put_blob_ParameterForm.setErrorMessage(CONTAINER_NAME,
                    "This Field is required.");
            validationStatus = false;
        }

        // BLOB_TYPE required.
        if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(BLOB_TYPE)
                .trim())) {
            put_blob_ParameterForm.setErrorMessage(BLOB_TYPE,
                    "This Field is required.");
            validationStatus = false;
        }

        if (PAGE_BLOB.equals(type)) {

            // BLOB_NAME required
            if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(BLOB_NAME)
                    .trim())) {
                put_blob_ParameterForm.setErrorMessage(BLOB_NAME,
                        "This Field is required.");
                validationStatus = false;
            }

            // BLOB_CONTENT_LENGTH required if Blob type is page.
            if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(
                    BLOB_CONTENT_LENGTH).trim())) {
                put_blob_ParameterForm.setErrorMessage(BLOB_CONTENT_LENGTH,
                        "This Field is required.");
                validationStatus = false;
            }

            // BLOB_SEQUENCE_NUMBER required
            if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(
                    BLOB_SEQUENCE_NUMBER).trim())) {
                put_blob_ParameterForm.setErrorMessage(BLOB_SEQUENCE_NUMBER,
                        "This Field is required.");
                validationStatus = false;
            }

            if (!put_blob_ParameterForm.getValue(BLOB_SEQUENCE_NUMBER).trim()
                    .matches("\\d+")) {
                put_blob_ParameterForm.setErrorMessage(BLOB_SEQUENCE_NUMBER,
                        "Only integers greater than \"0\" allowed.");
                validationStatus = false;

            }

        } else if (BLOCK_BLOB.equals(type) && file.equals("0")) {

            // BLOB_NAME is required if UPLOAD_FILE is 0.
            if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(BLOB_NAME)
                    .trim())) {
                put_blob_ParameterForm.setErrorMessage(BLOB_NAME,
                        "This Field is required.");
                validationStatus = false;
            }

            // BLOB_CONTENT is required if Blob type is block.
            if (StringUtil.isEmpty(put_blob_ParameterForm
                    .getValue(BLOB_CONTENT).trim())) {
                put_blob_ParameterForm.setErrorMessage(BLOB_CONTENT,
                        "This Field is required.");
                validationStatus = false;
            }
        } else if (BLOCK_BLOB.equals(type) && file.equals("1")) {

            // FILEPATH is required if UPLOAD_FILE is 1.
            if (StringUtil.isEmpty(put_blob_ParameterForm.getValue(FILEPATH)
                    .trim())) {
                put_blob_ParameterForm.setErrorMessage(FILEPATH,
                        "This Field is required.");
                validationStatus = false;
            }

        }

        return validationStatus;
    }

    protected void updateRowVisibility() {

        String type = put_blob_ParameterForm.getValue(BLOB_TYPE);
        String file = put_blob_ParameterForm.getValue(UPLOAD_FILE);

        put_blob_ParameterForm.setRowVisible(BLOB_CONTENT_LENGTH,
                PAGE_BLOB.equals(type));
        put_blob_ParameterForm.setRowVisible(BLOB_SEQUENCE_NUMBER,
                PAGE_BLOB.equals(type));

        put_blob_ParameterForm.setRowVisible(UPLOAD_FILE,
                BLOCK_BLOB.equals(type));

        put_blob_ParameterForm.setRowVisible(BLOB_NAME,
                (PAGE_BLOB.equals(type) || (BLOCK_BLOB.equals(type) && file
                        .equals("0"))));

        put_blob_ParameterForm.setRowVisible(BLOB_CONTENT,
                (BLOCK_BLOB.equals(type) && file.equals("0")));

        put_blob_ParameterForm.setRowVisible(FILEPATH,
                (BLOCK_BLOB.equals(type) && file.equals("1")));

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
        Map<String, String> put_blob_FormValues = put_blob_ParameterForm
                .getValues();

        actualParams.put(CONNECTION_CONFIG,
                put_blob_FormValues.get(CONNECTION_CONFIG));
        actualParams.put(STORAGE_ACCOUNT,
                put_blob_FormValues.get(STORAGE_ACCOUNT));
        actualParams.put(STORAGE_ACCOUNT_KEY,
                put_blob_FormValues.get(STORAGE_ACCOUNT_KEY));
        actualParams.put(CONTAINER_NAME,
                put_blob_FormValues.get(CONTAINER_NAME));
        actualParams.put(BLOB_TYPE, put_blob_FormValues.get(BLOB_TYPE));
        actualParams.put(UPLOAD_FILE, put_blob_FormValues.get(UPLOAD_FILE));
        actualParams.put(FILEPATH, put_blob_FormValues.get(FILEPATH));
        actualParams.put(BLOB_NAME, put_blob_FormValues.get(BLOB_NAME));
        actualParams.put(BLOB_CONTENT_LENGTH,
                put_blob_FormValues.get(BLOB_CONTENT_LENGTH));
        actualParams.put(BLOB_SEQUENCE_NUMBER,
                put_blob_FormValues.get(BLOB_SEQUENCE_NUMBER));
        actualParams.put(BLOB_CONTENT, put_blob_FormValues.get(BLOB_CONTENT));
        actualParams.put(RESULTS_LOCATION_OUTPSP,
                put_blob_FormValues.get(RESULTS_LOCATION_OUTPSP));
        actualParams.put(TAG_OUTPP,
                put_blob_FormValues.get(TAG_OUTPP));
        actualParams.put(JOB_STEP_TIMEOUT,
                put_blob_FormValues.get(JOB_STEP_TIMEOUT));

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
        for (String key : new String[] { CONNECTION_CONFIG, STORAGE_ACCOUNT,
                STORAGE_ACCOUNT_KEY, CONTAINER_NAME, BLOB_TYPE, UPLOAD_FILE,
                BLOB_NAME, FILEPATH, BLOB_CONTENT_LENGTH, BLOB_SEQUENCE_NUMBER,
                BLOB_CONTENT, RESULTS_LOCATION_OUTPSP, TAG_OUTPP, JOB_STEP_TIMEOUT }) {
            put_blob_ParameterForm.setValue(key,
                    StringUtil.nullToEmpty(params.get(key)));
        }

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
    interface Binder extends UiBinder<Widget, PutBlobParameterPanel> {
    }
}
