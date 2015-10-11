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
# deletecfg.pl
##########################

use ElectricCommander;
use ElectricCommander::PropDB;

use constant {
               SUCCESS => 0,
               ERROR   => 1,
             };

my $opts;

my $PLUGIN_NAME = "EC-Azure";
my $projName    = "@PLUGIN_KEY@-@PLUGIN_VERSION@";

if (!defined $PLUGIN_NAME) {
    print "PLUGIN_NAME must be defined\n";
    exit ERROR;
}

## get an EC object
my $ec = new ElectricCommander();
$ec->abortOnError(0);

my $opts;
$opts->{connection_config} = "$[connection_config]";

if (!defined $opts->{connection_config} || "$opts->{connection_config}" eq "") {
    print "connection_config parameter must exist and be non-blank\n";
    exit ERROR;
}

# check to see if a config with this name already exists before we do anything else
my $xpath    = $ec->getProperty("/myProject/azure_cfgs/$opts->{connection_config}");
my $property = $xpath->findvalue("//response/property/propertyName");

if (!defined $property || "$property" eq "") {
    my $errMsg = "Error: A configuration named '$opts->{connection_config}' does not exist.";
    $ec->setProperty("/myJob/configError", $errMsg);
    print $errMsg;
    exit ERROR;
}

$ec->deleteProperty("/myProject/azure_cfgs/$opts->{connection_config}");
$ec->deleteCredential($projName, $opts->{connection_config});
exit SUCCESS;
