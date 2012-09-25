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

my $credName = "$[/myJob/config_name]";
my $xpath    = $ec->getFullCredential("credential");
my $userName = $xpath->findvalue("//userName");
my $password = $xpath->findvalue("//password");

# Create credential
my $projName = "@PLUGIN_KEY@-@PLUGIN_VERSION@";

$ec->deleteCredential($projName, $credName);
$xpath = $ec->createCredential($projName, $credName, $userName, $password);
my $errors = $ec->checkAllErrors($xpath);

# Give config the credential's real name
my $configPath = "/projects/$projName/azure_cfgs/$credName";
$xpath = $ec->setProperty($configPath . "/credential", $credName);
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

if ("$errors" ne "") {

    # Cleanup the partially created configuration we just created
    $ec->deleteProperty($configPath);
    $ec->deleteCredential($projName, $credName);
    my $errMsg = "Error creating configuration credential: " . $errors;
    $ec->setProperty("/myJob/configError", $errMsg);
    print $errMsg;
    exit ERROR;
}
