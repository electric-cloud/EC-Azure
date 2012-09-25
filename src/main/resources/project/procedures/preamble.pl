use ElectricCommander;
use File::Basename;
use ElectricCommander::PropDB;
use ElectricCommander::PropMod;
use Encode;
use utf8;

$| = 1;

use constant {
               SUCCESS => 0,
               ERROR   => 1,
             };

# Create ElectricCommander instance
my $ec = new ElectricCommander();
$ec->abortOnError(0);

my $pluginKey  = 'EC-Azure';
my $xpath      = $ec->getPlugin($pluginKey);
my $pluginName = $xpath->findvalue('//pluginVersion')->value;
print "Using plugin $pluginKey version $pluginName\n";
$opts->{pluginVer} = $pluginName;

if ($^O ne "MSWin32") {
    print "This plugin is Windows Only!\n";
    exit ERROR;
}

if (defined($opts->{connection_config}) && $opts->{connection_config} ne "") {
    my $cfgName = $opts->{connection_config};
    print "Loading config $cfgName\n";

    my $proj = "$[/myProject/projectName]";
    my $cfg = new ElectricCommander::PropDB($ec, "/projects/$proj/azure_cfgs");

    my %vals = $cfg->getRow($cfgName);

    # Check if configuration exists
    unless (keys(%vals)) {
        print "Configuration [$cfgName] does not exist\n";
        exit ERROR;
    }

    # Add all options from configuration
    foreach my $c (keys %vals) {
        print "Adding config $c = $vals{$c}\n";
        $opts->{$c} = $vals{$c};
    }

    # Check that credential item exists
    if (!defined $opts->{credential} || $opts->{credential} eq "") {
        print "Configuration [$cfgName] does not contain an Azure credential\n";
        exit ERROR;
    }

    # Get suscription id/certificate thumbprint out of credential named in $opts->{credential}
    my $xpath = $ec->getFullCredential("$opts->{credential}");
    $opts->{subscription_id} = $xpath->findvalue("//userName");
    $opts->{thumbprint}      = $xpath->findvalue("//password");

}

$opts->{JobStepId} = "$[/myJobStep/jobStepId]";

# Load the actual code into this process
if (!ElectricCommander::PropMod::loadPerlCodeFromProperty($ec, '/myProject/azure_driver/Azure')) {
    print 'Could not load Azure.pm\n';
    exit ERROR;
}

# Make an instance of the object, passing in options as a hash
my $azure = new Azure($ec, $opts);
