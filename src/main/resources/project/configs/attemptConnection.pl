##########################
# attemptConnection.pl
##########################


use ElectricCommander;
use ElectricCommander::PropDB;
use LWP::UserAgent;
use MIME::Base64;

use Carp qw( carp croak );

use constant {
               SUCCESS => 0,
               ERROR   => 1,
             };

if ( $^O ne "MSWin32" ) {
    print "This plugin is Windows Only!\n";
        exit ERROR;
}                    
             
## get an EC object
my $ec = new ElectricCommander();
$ec->abortOnError(0);

my $credName = "$[/myJob/config_name]";

my $xpath  = $ec->getFullCredential("credential");
my $errors = $ec->checkAllErrors($xpath);
my $subscription_id   = $xpath->findvalue("//userName");
my $thumbprint   = $xpath->findvalue("//password");

my $projName = "$[/myProject/projectName]";
print "Attempting connection with server\n";

my $path_to_rest = $ENV{COMMANDER_PLUGINS} ."/$projName/console/AzureRequest.exe";
my @cmd;

push(@cmd, qq{"$path_to_rest"}); # Executable
push(@cmd, qq{list-hosted-services}); # Operation
push(@cmd, qq{$subscription_id}); # Subscription_id
push(@cmd, qq{"$thumbprint"}); #thumbprint
push(@cmd, qq{GET}); # Method
push(@cmd, qq{services/hostedservices}); #Url
push(@cmd, qq{2012-03-01}); #version

# list-hosted-services ffb31b9e-d5e5-447f-8e50-1d1951ccd45a B86D7C6253FA151A5518E7BF3F558C96709F444C GET "services/hostedservices" "2011-10-01"

my $command = join(" ", @cmd);
print $command . "\n";

my $hostedservices = qx{$command};
print $hostedservices . "\n";

#-----------------------------
# Check if successful login
#-----------------------------
if(($hostedservices =~ m/.*(Error|Exception).*/ixmsg) | ($hostedservices eq ''))
{

    my $errMsg = "\nTest connection failed.\n";
    $ec->setProperty("/myJob/configError", $errMsg);
     print $errMsg;

   $ec->deleteProperty("/projects/$projName/azure_cfgs/$credName");
   $ec->deleteCredential($projName, $credName);
   exit ERROR;

}

exit SUCCESS;


