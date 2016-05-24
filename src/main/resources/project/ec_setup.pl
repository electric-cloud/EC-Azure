#
#  Copyright 2015 Electric Cloud, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


# The plugin is being promoted, create a property reference in the server's property sheet
# Data that drives the create step picker registration for this plugin.


# Resource Management

#Deployment

my %create_vm = (
    label       => "Windows Azure - Create Virtual Machine",
    procedure   => "Create VM",
    description => "Create Virtual Machine",
    category    => "Deployment"
);

my %delete_vm = (
    label       => "Windows Azure - Delete Virtual Machine",
    procedure   => "Delete VM",
    description => "Delete Virtual Machine",
    category    => "Deployment"
);

my %start_vm = (
    label       => "Windows Azure - Start Virtual Machine",
    procedure   => "Start VM",
    description => "Start Virtual Machine",
    category    => "Deployment"
);

my %stop_vm = (
    label       => "Windows Azure - Stop Virtual Machine",
    procedure   => "Stop VM",
    description => "Stop Virtual Machine",
    category    => "Deployment"
);

my %restart_vm = (
    label       => "Windows Azure - Restart Virtual Machine",
    procedure   => "Restart VM",
    description => "Restart Virtual Machine",
    category    => "Deployment"
);

my %teardown = (
    label       => "Windows Azure - TearDown Virtual Machines",
    procedure   => "TearDown",
    description => "Delete Virtual Machine (Commander Resource/ ResourcePool)",
    category    => "Deployment"
);

my %create_update_database_server = (
    label       => "Windows Azure - Create or Update Database Server",
    procedure   => "Create or Update Database Server",
    description => "Creates or updates a database server",
    category    => "Deployment"
);

my %delete_database_server = (
    label       => "Windows Azure - Delete Database server",
    procedure   => "Delete Database Server",
    description => "Deletes a database server",
    category    => "Deployment"
);

my %create_update_database = (
    label       => "Windows Azure - Create or Update database",
    procedure   => "Create Or Update Database",
    description => "Creates or updates a database",
    category    => "Deployment"
);

my %delete_database = (
    label       => "Windows Azure - Delete existing database",
    procedure   => "DeleteDatabase",
    description => "Delete existing database",
    category    => "Deployment"
);

my %create_vnet = (
    label       => "Windows Azure - Create or Update Virtual Network",
    procedure   => "Create or Update Vnet",
    description => "Create or Update Virual Network",
    category    => "Deployment"
);

my %delete_vnet = (
    label       => "Windows Azure - Delete Virtual Network",
    procedure   => "Delete Vnet",
    description => "Delete Virual Network",
    category    => "Deployment"
);

my %create_update_subnet = (
    label       => "Windows Azure - Create or Update Subnet",
    procedure   => "Create or Update Subnet",
    description => "Creates a subnet or updates an existing one",
    category    => "Deployment"
);

my %delete_subnet = (
    label       => "Windows Azure - Delete existing subnet",
    procedure   => "Delete Subnet",
    description => "Delete existing subnet",
    category    => "Deployment"
);

my %create_update_security_group = (
    label       => "Windows Azure - Create or Update Network Security Group",
    procedure   => "Create or Update NetworkSecurityGroup",
    description => "Creates a Network Security Group or updates an existing one",
    category    => "Deployment"
);
my %delete_security_group = (
    label       => "Windows Azure - Delete existing Network Security Group",
    procedure   => "Delete NetworkSecurityGroup",
    description => "Delete existing Network Security Group",
    category    => "Deployment"
);

my %create_update_security_rule = (
    label       => "Windows Azure - Create or Update Network Security Rule",
    procedure   => "Create or Update NetworkSecurityRule",
    description => "Creates a Network Security Rule or updates an existing one",
    category    => "Deployment"
);

my %delete_security_rule = (
    label       => "Windows Azure - Delete existing Network Security Rule",
    procedure   => "Delete NetworkSecurityRule",
    description => "Delete existing Network Security Rule",
    category    => "Deployment"
);

my %nosql_operations = (
    label       => "Windows Azure - NoSQL Operations",
    procedure   => "NoSQL Operations",
    description => "Perform NoSQL Operations",
    category    => "Deployment"
);

my %sql_operations = (
    label       => "Windows Azure - SQL Operations",
    procedure   => "SQL Operations",
    description => "Perform SQL Operations",
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
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create VM");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete VM");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Start VM");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Stop VM");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Restart VM");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - TearDown");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create or Update Database Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete Database Server");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create Or Update Database");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - DeleteDatabase");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create Virtual Network");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create or Update Subnet");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete Subnet");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create or Update NetworkSecurityGroup");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete NetworkSecurityGroup");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Create or Update NetworkSecurityRule");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete NetworkSecurityRule");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - NoSQL Operations");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - SQL Operations");
$batch->deleteProperty("/server/ec_customEditors/pickerStep/Windows Azure - Delete Vnet");
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

@::createStepPickerSteps = (
    \%create_vm,
    \%delete_vm,
    \%start_vm,
    \%stop_vm,
    \%restart_vm,
    \%teardown,
    \%create_update_database_server,
    \%delete_database_server,
    \%create_update_database,
    \%delete_database,
    \%create_vnet,
    \%delete_vnet,
    \%create_update_subnet,
    \%delete_subnet,
    \%create_update_security_group,
    \%delete_security_group,
    \%create_update_security_rule,
    \%delete_security_rule,
    \%nosql_operations,
    \%sql_operations
);

my $pluginName = '@PLUGIN_NAME@';
my $pluginKey  = '@PLUGIN_KEY@';
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
        my @nodes = $nodes->findnodes("credential/credentialName");
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
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Create VM',
                    stepName      => 'Create VM'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Delete VM',
                    stepName      => 'Delete VM'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Start VM',
                    stepName      => 'Start VM'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Stop VM',
                    stepName      => 'Stop VM'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Restart VM',
                    stepName      => 'Restart VM'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'TearDown',
                    stepName      => 'tearDown'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Create or Update Database Server',
                    stepName      => 'createUpdateDatabaseServer'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Delete Database Server',
                    stepName      => 'deleteDatabaseServer'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Create Or Update Database',
                    stepName      => 'createUpdateDatabase'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'DeleteDatabase',
                    stepName      => 'deleteDatabase'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Create or Update Vnet',
                    stepName      => 'Create Vnet'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Delete Vnet',
                    stepName      => 'Delete Vnet'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Create or Update Subnet',
                    stepName      => 'createUpdateSubnet'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Delete Subnet',
                    stepName      => 'deleteSubnet'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Create or Update NetworkSecurityGroup',
                    stepName      => 'createUpdateNetworkSecurityGroup'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Delete NetworkSecurityGroup',
                    stepName      => 'deleteNetworkSecurityGroup'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Create or Update NetworkSecurityRule',
                    stepName      => 'createUpdateNetworkSecurityRule'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'Delete NetworkSecurityRule',
                    stepName      => 'deleteNetworkSecurityRule'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'NoSQL Operations',
                    stepName      => 'nosqlOperations'
                }
            );
            $batch->attachCredential(
                "\$[/plugins/$pluginName/project]",
                $cred,
                {
                    procedureName => 'SQL Operations',
                    stepName      => 'sqlOperations'
                }
            );
        }
    }
}
