
# The plugin is being promoted, create a property reference in the server's property sheet
# Data that drives the create step picker registration for this plugin.


# Resource Management
# my %provision = (
                 # label       => "Windows Azure - Provision",
                 # procedure   => "Provision",
                 # description => "Adds a Virtual Machine to an existing deployment, starts the new VM and create an Electric Commander resource.",
                 # category    => "Resource Management"
                # );

# my %cleanup = (
               # label       => "Windows Azure - Cleanup",
               # procedure   => "Cleanup",
               # description => "Deletes a virtual machine and deletes its resource from Electric Commander.",
               # category    => "Resource Management"
              # );

# my %call_azure = (
                  # label       => "Windows Azure - CallAzure",
                  # procedure   => "CallAzure",
                  # description => "Provides a generic way to call Windows Azure Service Management REST API.",
                  # category    => "Resource Management"
                 # );

# my %create_resource = (
                       # label       => "Windows Azure - Create Resource From VM",
                       # procedure   => "Create Resource From VM",
                       # description => "Gets Virtual machine information to create an Electric Commander resource for it.",
                       # category    => "Resource Management"
                      # );

# my %add_role = (
                # label       => "Windows Azure - Add Role",
                # procedure   => "Add Role",
                # description => "Adds a Virtual Machine to an existing deployment.",
                # category    => "Resource Management"
               # );

# my %capture_role = (
                    # label       => "Windows Azure - Capture Role",
                    # procedure   => "Capture Role",
                    # description => "Captures a virtual machine image to your image gallery.",
                    # category    => "Resource Management"
                   # );
# my %delete_role = (
                   # label       => "Windows Azure - Delete Role",
                   # procedure   => "Delete Role",
                   # description => "Deletes the specified virtual machine.",
                   # category    => "Resource Management"
                  # );
# my %get_role = (
                # label       => "Windows Azure - Get Role",
                # procedure   => "Get Role",
                # description => "Retrieves the specified virtual machine.",
                # category    => "Resource Management"
               # );

# my %restart_role = (
                    # label       => "Windows Azure - Restart Role",
                    # procedure   => "Restart Role",
                    # description => "Restarts the specified virtual machine.",
                    # category    => "Resource Management"
                   # );

# my %shutdown_role = (
                     # label       => "Windows Azure - Shutdown Role",
                     # procedure   => "Shutdown Role",
                     # description => "Shuts down the specified virtual machine.",
                     # category    => "Resource Management"
                    # );

# my %start_role = (
                  # label       => "Windows Azure - Start Role",
                  # procedure   => "Start Role",
                  # description => "Starts the specified virtual machine.",
                  # category    => "Resource Management"
                 # );

# my %create_vm_deployment = (
                            # label       => "Windows Azure - Create Virtual Machine Deployment",
                            # procedure   => "Create Virtual Machine Deployment",
                            # description => "Provisions a virtual machine based on the supplied configuration.",
                            # category    => "Resource Management"
                           # );

# my %download_rdp = (
                    # label       => "Windows Azure - Download RDP File",
                    # procedure   => "Download RDP File",
                    # description => "Retrieves the Remote Desktop Protocol configuration file from the specified virtual machine.",
                    # category    => "Resource Management"
                   # );

# my %list_objects = (
                    # label       => "Windows Azure - List Objects",
                    # procedure   => "List Objects",
                    # description => "Retrieves a list of the OS Images/Virtual Machine Disks/Virtual Network Sites from your subscription.",
                    # category    => "Resource Management"
                   # );
                   
                   
#Deployment
my %create_hosted_service = (
                 label       => "Windows Azure - Create Hosted Service",
                 procedure   => "Create Hosted Service",
                 description => "Creates a new hosted service in Windows Azure.",
                 category    => "Deployment"
                );

my %delete_hosted_service = (
                 label       => "Windows Azure - Delete Hosted Service",
                 procedure   => "Delete Hosted Service",
                 description => "Deletes the specified hosted service from Windows Azure.",
                 category    => "Deployment"
                );

my %create_storage_account = (
                 label       => "Windows Azure - Create Storage Account",
                 procedure   => "Create Storage Account",
                 description => "Creates a new storage account in Windows Azure.",
                 category    => "Deployment"
                );

my %delete_storage_account = (
                 label       => "Windows Azure - Delete Storage Account",
                 procedure   => "Delete Storage Account",
                 description => "Deletes the specified storage account from Windows Azure.",
                 category    => "Deployment"
                );

my %get_storage_account_keys = (
                 label       => "Windows Azure - Get Storage Account Keys",
                 procedure   => "Get Storage Account Keys",
                 description => "Returns the primary and secondary access keys for the specified storage account.",
                 category    => "Deployment"
                );

my %create_container = (
                 label       => "Windows Azure - Create Container",
                 procedure   => "Create Container",
                 description => "Creates a new container under the specified account. If the container with the same name already exists, the operation fails.",
                 category    => "Deployment"
                );

my %delete_container = (
                 label       => "Windows Azure - Delete Container",
                 procedure   => "Delete Container",
                 description => "Marks the specified container for deletion. The container and any blobs contained within it are later deleted during garbage collection.",
                 category    => "Deployment"
                );

my %put_blob = (
                 label       => "Windows Azure - Put Blob",
                 procedure   => "Put Blob",
                 description => "Creates a new block blob or page blob, or updates the content of an existing block blob.",
                 category    => "Deployment"
                );

my %delete_blob = (
                 label       => "Windows Azure - Delete Blob",
                 procedure   => "Delete Blob",
                 description => "Marks the specified blob or snapshot for deletion. The blob is later deleted during garbage collection.",
                 category    => "Deployment"
                );
                
my %create_deployment = (
                 label       => "Windows Azure - Create Deployment",
                 procedure   => "Create Deployment",
                 description => "Uploads a new service package and creates a new deployment on staging or production.",
                 category    => "Deployment"
                );
                
my %get_status = (
                 label       => "Windows Azure - Get Operation Status",
                 procedure   => "Get Operation Status",
                 description => "Return the actual Status of the operation.",
                 category    => "Deployment"
                );


                
#Resource Management           

$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Provision");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Cleanup");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - CallAzure");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create Resource From VM");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Add Role");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Capture Role");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete Role");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Get Role");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Restart Role");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Shutdown Role");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Start Role");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create Virtual Machine Deployment");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Download RDP File");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - List Objects");


#Deployment

$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create Hosted Service");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete Hosted Service");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create Storage Account");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete Storage Account");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Get Storage Account Keys");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create Container");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete Container");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Put Blob");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete Blob");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create Deployment");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Get Operation Status");





# @::createStepPickerSteps = (\%provision, \%cleanup, \%call_azure, \%create_resource, \%add_role, \%capture_role, \%delete_role, \%get_role, \%restart_role, \%shutdown_role, \%start_role, \%create_vm_deployment, \%download_rdp, \%list_objects);

@::createStepPickerSteps = (\%create_hosted_service, \%delete_hosted_service ,\%create_storage_account, \%delete_storage_account, \%get_storage_account_keys, \%create_container, \%delete_container, \%put_blob, \%delete_blob, \%get_status, \%create_deployment);

my $pluginName = "@PLUGIN_NAME@";
my $pluginKey  = "@PLUGIN_KEY@";
if ($promoteAction ne '') {
    my @objTypes = ('projects', 'resources', 'workspaces');
    my $query    = $commander->newBatch();
    my @reqs     = map { $query->getAclEntry('user', "project: $pluginName", { systemObjectName => $_ }) } @objTypes;
    push @reqs, $query->getProperty('/server/ec_hooks/promote');
    $query->submit();

    foreach my $type (@objTypes) {
        if ($query->findvalue(shift @reqs, 'code') ne 'NoSuchAclEntry') {
            $batch->deleteAclEntry('user', "project: $pluginName", { systemObjectName => $type });
        }
    }

    if ($promoteAction eq "promote") {
        foreach my $type (@objTypes) {
            $batch->createAclEntry(
                                   'user',
                                   "project: $pluginName",
                                   {
                                      systemObjectName           => $type,
                                      readPrivilege              => 'allow',
                                      modifyPrivilege            => 'allow',
                                      executePrivilege           => 'allow',
                                      changePermissionsPrivilege => 'allow'
                                   }
                                  );
        }
    }
}

if ($upgradeAction eq "upgrade") {
    my $query   = $commander->newBatch();
    my $newcfg  = $query->getProperty("/plugins/$pluginName/project/azure_cfgs");
    my $oldcfgs = $query->getProperty("/plugins/$otherPluginName/project/azure_cfgs");
    my $creds   = $query->getCredentials("\$[/plugins/$otherPluginName]");

    local $self->{abortOnError} = 0;
    $query->submit();

    # if new plugin does not already have cfgs
    if ($query->findvalue($newcfg, "code") eq "NoSuchProperty") {

        # if old cfg has some cfgs to copy
        if ($query->findvalue($oldcfgs, "code") ne "NoSuchProperty") {
            $batch->clone(
                          {
                            path      => "/plugins/$otherPluginName/project/azure_cfgs",
                            cloneName => "/plugins/$pluginName/project/azure_cfgs"
                          }
                         );
        }
    }

    # Copy configuration credentials and attach them to the appropriate steps
    my $nodes = $query->find($creds);
    if ($nodes) {
        my @nodes = $query->{xpath}->findnodes("credential/credentialName", $nodes);
        for (@nodes) {
            my $cred = $_->string_value;

            # Clone the credential
            $batch->clone(
                          {
                            path      => "/plugins/$otherPluginName/project/credentials/$cred",
                            cloneName => "/plugins/$pluginName/project/credentials/$cred"
                          }
                         );

            # Make sure the credential has an ACL entry for the new project principal
            my $xpath = $commander->getAclEntry(
                                                "user",
                                                "project: $pluginName",
                                                {
                                                   projectName    => $otherPluginName,
                                                   credentialName => $cred
                                                }
                                               );
            if ($xpath->findvalue("//code") eq "NoSuchAclEntry") {
                $batch->deleteAclEntry(
                                       "user",
                                       "project: $otherPluginName",
                                       {
                                          projectName    => $pluginName,
                                          credentialName => $cred
                                       }
                                      );
                $batch->createAclEntry(
                                       "user",
                                       "project: $pluginName",
                                       {
                                          projectName                => $pluginName,
                                          credentialName             => $cred,
                                          readPrivilege              => 'allow',
                                          modifyPrivilege            => 'allow',
                                          executePrivilege           => 'allow',
                                          changePermissionsPrivilege => 'allow'
                                       }
                                      );
            }

            # Attach the credential to the appropriate steps
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Provision',
                                        # stepName      => 'Provision'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Cleanup',
                                        # stepName      => 'Cleanup'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'CallAzure',
                                        # stepName      => 'CallAzure'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Create Resource From VM',
                                        # stepName      => 'Create Resource From VM'
                                     # }
                                    # );

            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Add Role',
                                        # stepName      => 'Add Role'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Capture Role',
                                        # stepName      => 'Capture Role'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Delete Role',
                                        # stepName      => 'Delete Role'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Get Role',
                                        # stepName      => 'Get Role'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Restart Role',
                                        # stepName      => 'Restart Role'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Shutdown Role',
                                        # stepName      => 'Shutdown Role'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Start Role',
                                        # stepName      => 'Start Role'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Create Virtual Machine Deployment',
                                        # stepName      => 'Create Virtual Machine Deployment'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'Download RDP File',
                                        # stepName      => 'Download RDP File'
                                     # }
                                    # );
            # $batch->attachCredential(
                                     # "\$[/plugins/$pluginName/project]",
                                     # $cred,
                                     # {
                                        # procedureName => 'List Objects',
                                        # stepName      => 'List Objects'
                                     # }
                                    # );
                                    
            $batch->attachCredential(
                                     "\$[/plugins/$pluginName/project]",
                                     $cred,
                                     {
                                        procedureName => 'Create Hosted Service',
                                        stepName      => 'Create Hosted Service'
                                     }
                                    );
            $batch->attachCredential(
                                     "\$[/plugins/$pluginName/project]",
                                     $cred,
                                     {
                                        procedureName => 'Delete Hosted Service',
                                        stepName      => 'Delete Hosted Service'
                                     }
                                    );
            $batch->attachCredential(
                                     "\$[/plugins/$pluginName/project]",
                                     $cred,
                                     {
                                        procedureName => 'Create Storage Account',
                                        stepName      => 'Create Storage Account'
                                     }
                                    );
            $batch->attachCredential(
                                     "\$[/plugins/$pluginName/project]",
                                     $cred,
                                     {
                                        procedureName => 'Delete Storage Account',
                                        stepName      => 'Delete Storage Account'
                                     }
                                    );
            $batch->attachCredential(
                                     "\$[/plugins/$pluginName/project]",
                                     $cred,
                                     {
                                        procedureName => 'Get Storage Account Keys',
                                        stepName      => 'Get Storage Account Keys'
                                     }
                                    );
            $batch->attachCredential(
                                     "\$[/plugins/$pluginName/project]",
                                     $cred,
                                     {
                                        procedureName => 'Create Deployment',
                                        stepName      => 'Create Deployment'
                                     }
                                    );
            $batch->attachCredential(
                                     "\$[/plugins/$pluginName/project]",
                                     $cred,
                                     {
                                        procedureName => 'Get Operation Status',
                                        stepName      => 'Get Operation Status'
                                     }
                                    );
        }
    }
}
