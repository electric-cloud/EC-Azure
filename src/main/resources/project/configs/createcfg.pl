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

#########################
## createcfg.pl
#########################

use ElectricCommander;
use ElectricCommander::PropDB;

use constant {
               SUCCESS => 0,
               ERROR   => 1,
             };

my $opts;

my $PLUGIN_NAME = "EC-Azure";

if (!defined $PLUGIN_NAME) {
    print "PLUGIN_NAME must be defined\n";
    exit ERROR;
}

## get an EC object
my $ec = new ElectricCommander();
$ec->abortOnError(0);

## load option list from procedure parameters
my $x       = $ec->getJobDetails($ENV{COMMANDER_JOBID});
my $nodeset = $x->find("//actualParameter");
foreach my $node ($nodeset->get_nodelist) {
    my $parm = $node->findvalue("actualParameterName");
    my $val  = $node->findvalue("value");
    $opts->{$parm} = "$val";
}

if (!defined $opts->{config_name} || "$opts->{config_name}" eq "") {
    print "config_name parameter must exist and be non-blank\n";
    exit ERROR;
}

# check to see if a config with this name already exists before we do anything else
my $xpath    = $ec->getProperty("/myProject/azure_cfgs/$opts->{config_name}");
my $property = $xpath->findvalue("//response/property/propertyName");

if (defined $property && "$property" ne "") {
    my $errMsg = "A configuration named '$opts->{config_name}' already exists.";
    $ec->setProperty("/myJob/configError", $errMsg);
    print $errMsg;
    exit ERROR;
}

my $cfg = new ElectricCommander::PropDB($ec, "/myProject/azure_cfgs");

# set configuration description
$cfg->setRow("$opts->{config_name}", "$opts->{description}");

# add all the options as properties
foreach my $key (keys %{$opts}) {
    
    if ("$key" eq "config_name") {
        next;
    }

    if ("$key" eq "description") {
        next;
    }	
	
    $cfg->setCol("$opts->{config_name}", "$key", "$opts->{$key}");
}
exit SUCCESS;
