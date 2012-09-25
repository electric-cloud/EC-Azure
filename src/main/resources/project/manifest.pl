@files = (

    #Configuration files
    ['//procedure[procedureName="CreateConfiguration"]/propertySheet/property[propertyName="ec_parameterForm"]/value', 'forms/CreateConfigurationForm.xml'],
    ['//procedure[procedureName="DeleteConfiguration"]/propertySheet/property[propertyName="ec_parameterForm"]/value', 'forms/DeleteConfigurationForm.xml'],
    ['//property[propertyName="forms"]/propertySheet/property[propertyName="CreateConfigForm"]/value',                 'forms/CreateConfigurationForm.xml'],
    ['//property[propertyName="forms"]/propertySheet/property[propertyName="EditConfigForm"]/value',                   'forms/EditConfigurationForm.xml'],
    ['//procedure[procedureName="CreateConfiguration"]/step[stepName="CreateConfiguration"]/command',                  'configs/createcfg.pl'],
    ['//procedure[procedureName="CreateConfiguration"]/step[stepName="CreateAndAttachCredential"]/command',            'configs/createAndAttachCredential.pl'],
    ['//procedure[procedureName="CreateConfiguration"]/step[stepName="AttemptConnection"]/command',                    'configs/attemptConnection.pl'],
    ['//procedure[procedureName="DeleteConfiguration"]/step[stepName="DeleteConfiguration"]/command',                  'configs/deletecfg.pl'],

    #Procedures-Deployment
    ['//step[stepName="Create Hosted Service"]/command',                                            'procedures/deployment/create_hosted.pl'],
    ['//procedure[procedureName="Create Hosted Service"]/step[stepName="SetTimelimit"]/command',    'procedures/setTimelimit.pl'],
    ['//step[stepName="Create Storage Account"]/command',                                           'procedures/deployment/create_storage.pl'],
    ['//procedure[procedureName="Create Storage Account"]/step[stepName="SetTimelimit"]/command',   'procedures/setTimelimit.pl'],
    ['//step[stepName="Get Storage Account Keys"]/command',                                         'procedures/deployment/get_keys.pl'],
    ['//procedure[procedureName="Get Storage Account Keys"]/step[stepName="SetTimelimit"]/command', 'procedures/setTimelimit.pl'],
    ['//step[stepName="Create Container"]/command',                                                 'procedures/deployment/create_container.pl'],
    ['//procedure[procedureName="Create Container"]/step[stepName="SetTimelimit"]/command',         'procedures/setTimelimit.pl'],
    ['//step[stepName="Put Blob"]/command',                                                         'procedures/deployment/put_blob.pl'],
    ['//procedure[procedureName="Put Blob"]/step[stepName="SetTimelimit"]/command',                 'procedures/setTimelimit.pl'],
    ['//step[stepName="Create Deployment"]/command',                                                'procedures/deployment/create_deployment.pl'],
    ['//procedure[procedureName="Create Deployment"]/step[stepName="SetTimelimit"]/command',        'procedures/setTimelimit.pl'],
    ['//step[stepName="Delete Container"]/command',                                                 'procedures/deployment/delete_container.pl'],
    ['//procedure[procedureName="Delete Container"]/step[stepName="SetTimelimit"]/command',         'procedures/setTimelimit.pl'],
    ['//step[stepName="Delete Blob"]/command',                                                      'procedures/deployment/delete_blob.pl'],
    ['//procedure[procedureName="Delete Blob"]/step[stepName="SetTimelimit"]/command',              'procedures/setTimelimit.pl'],
    ['//step[stepName="Delete Storage Account"]/command',                                           'procedures/deployment/delete_storage.pl'],
    ['//procedure[procedureName="Delete Storage Account"]/step[stepName="SetTimelimit"]/command',   'procedures/setTimelimit.pl'],
    ['//step[stepName="Delete Hosted Service"]/command',                                            'procedures/deployment/delete_hosted.pl'],
    ['//procedure[procedureName="Delete Hosted Service"]/step[stepName="SetTimelimit"]/command',    'procedures/setTimelimit.pl'],
    ['//step[stepName="Get Operation Status"]/command',                                             'procedures/deployment/get_status.pl'],
    ['//procedure[procedureName="Get Operation Status"]/step[stepName="SetTimelimit"]/command',     'procedures/setTimelimit.pl'],

    #Forms-Deployment
    ['//procedure[procedureName="Get Storage Account Keys"]/propertySheet/property[propertyName="ec_parameterForm"]/value', 'forms/deployment/GetStorageAccountKeysForm.xml'],
    ['//procedure[procedureName="Create Container"]/propertySheet/property[propertyName="ec_parameterForm"]/value',         'forms/deployment/CreateContainerForm.xml'],
    ['//procedure[procedureName="Delete Container"]/propertySheet/property[propertyName="ec_parameterForm"]/value',         'forms/deployment/DeleteContainerForm.xml'],
    ['//procedure[procedureName="Delete Blob"]/propertySheet/property[propertyName="ec_parameterForm"]/value',              'forms/deployment/DeleteBlobForm.xml'],
    ['//procedure[procedureName="Delete Storage Account"]/propertySheet/property[propertyName="ec_parameterForm"]/value',   'forms/deployment/DeleteStorageAccountForm.xml'],
    ['//procedure[procedureName="Delete Hosted Service"]/propertySheet/property[propertyName="ec_parameterForm"]/value',    'forms/deployment/DeleteHostedServiceForm.xml'],
    ['//procedure[procedureName="Get Operation Status"]/propertySheet/property[propertyName="ec_parameterForm"]/value',     'forms/deployment/GetOperationStatusForm.xml'],

    #Main files
    ['//property[propertyName="preamble"]/value',       'procedures/preamble.pl'],
    ['//property[propertyName="Azure"]/value',          'azure_driver/Azure.pm'],
    ['//property[propertyName="ec_setup"]/value',       'ec_setup.pl'],
    ['//property[propertyName="postp_matchers"]/value', 'postp_matchers.pl'],

    #Extra Modules
    ['//property[propertyName="Readonly"]/value', 'lib/Readonly.pm'],

);

