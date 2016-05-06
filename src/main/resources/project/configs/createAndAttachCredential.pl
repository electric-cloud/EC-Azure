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

##########################
# createAndAttachCredential.pl
##########################

use ElectricCommander;

use constant {
    SUCCESS => 0,
    ERROR   => 1,
};

my $ec = new ElectricCommander();
$ec->abortOnError(0);

my $projName = "@PLUGIN_KEY@-@PLUGIN_VERSION@";

my $azureConfig       = "$[/myJob/config]";
my $azureCredential   = "$[/myJob/config]";
my $azureVMCredential = $azureCredential . "_vm_credential";

my %credentials = (
    $azureCredential   => "credential",
    $azureVMCredential => "vm_credential"
);

foreach my $credName ( keys %credentials ) {

    my $xpath    = $ec->getFullCredential( $credentials{$credName} );
    my $userName = $xpath->findvalue("//userName");
    my $password = $xpath->findvalue("//password");

    # Create credential

    $ec->deleteCredential( $projName, $credName );
    $xpath =
      $ec->createCredential( $projName, $credName, $userName, $password );
    my $errors = $ec->checkAllErrors($xpath);

    # Give config the credential's real name
    my $configPath = "/projects/$projName/azure_cfgs/$azureConfig";
    print "Setting property $configPath / + $credentials{$credName}";
    print " .. with value $credName";

    $xpath =
      $ec->setProperty( $configPath . "/" . $credentials{$credName},
        $credName );
    $errors .= $ec->checkAllErrors($xpath);

    # Give job launcher full permissions on the credential
    my $user = "$[/myJob/launchedByUser]";
    $xpath = $ec->createAclEntry(
        "user", $user,
        {
            projectName                => $projName,
            credentialName             => $credName,
            readPrivilege              => allow,
            modifyPrivilege            => allow,
            executePrivilege           => allow,
            changePermissionsPrivilege => allow
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    # Attach credential to steps that will need it
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Create Container',
            stepName      => 'Create Container'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Delete Container',
            stepName      => 'Delete Container'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Create Deployment',
            stepName      => 'Create Deployment'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Create Hosted Service',
            stepName      => 'Create Hosted Service'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Delete Hosted Service',
            stepName      => 'Delete Hosted Service'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Create Storage Account',
            stepName      => 'Create Storage Account'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Delete Storage Account',
            stepName      => 'Delete Storage Account'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Put Blob',
            stepName      => 'Put Blob'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Delete Blob',
            stepName      => 'Delete Blob'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);
    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Get Storage Account Keys',
            stepName      => 'Get Storage Account Keys'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,
        {
            procedureName => 'Create VM',
            stepName      => 'Create VM'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Delete VM',
            stepName      => 'Delete VM'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Start VM',
            stepName      => 'Start VM'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Stop VM',
            stepName      => 'Stop VM'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Restart VM',
            stepName      => 'Restart VM'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'TearDown',
            stepName      => 'tearDown'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Create or Update Database Server',
            stepName      => 'createUpdateDatabaseServer'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Delete Database Server',
            stepName      => 'deleteDatabaseServer'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Create Or Update Database',
            stepName      => 'createUpdateDatabase'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'DeleteDatabase',
            stepName      => 'deleteDatabase'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Create or Update Vnet',
            stepName      => 'Create Vnet'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Delete Vnet',
            stepName      => 'Delete Vnet'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Create or Update Subnet',
            stepName      => 'createUpdateSubnet'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Delete Subnet',
            stepName      => 'deleteSubnet'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Create or Update NetworkSecurityGroup',
            stepName      => 'createUpdateNetworkSecurityGroup'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Delete NetworkSecurityGroup',
            stepName      => 'deleteNetworkSecurityGroup'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Create or Update NetworkSecurityRule',
            stepName      => 'createUpdateNetworkSecurityRule'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'Delete NetworkSecurityRule',
            stepName      => 'deleteNetworkSecurityRule'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'NoSQL Operations',
            stepName      => 'nosqlOperations'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    $xpath = $ec->attachCredential(
        $projName,
        $credName,

        {
            procedureName => 'SQL Operations',
            stepName      => 'sqlOperations'
        }
    );
    $errors .= $ec->checkAllErrors($xpath);

    if ( "$errors" ne "" ) {

        # Cleanup the partially created configuration we just created
        $ec->deleteProperty($configPath);
        $ec->deleteCredential( $projName, $azureCredential );
        $ec->deleteCredential( $projName, $azureVMCredential );
        my $errMsg = "Error creating configuration credential: " . $errors;
        $ec->setProperty( "/myJob/configError", $errMsg );
        print $errMsg;
        exit ERROR;
    }

}
