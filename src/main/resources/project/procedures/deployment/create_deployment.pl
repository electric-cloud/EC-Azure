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
# create_deployment.pl
##########################
use warnings;
use strict;
use Encode;
use utf8;
use open IO => ':encoding(utf8)';

my $opts;

$opts->{connection_config}        = q{$[connection_config]};
$opts->{deployment_slot}          = q{$[deployment_slot]};
$opts->{service_name}             = q{$[service_name]};
$opts->{deployment_name}          = q{$[deployment_name]};
$opts->{label}                    = q{$[label]};
$opts->{package_url}              = q{$[package_url]};
$opts->{deployment_configuration} = q{$[deployment_configuration]};
$opts->{start_deployment}         = q{$[start_deployment]};
$opts->{treat_warnings_as_error}  = q{$[treat_warnings_as_error]};
$opts->{name}                     = q{$[name]};
$opts->{value}                    = q{$[value]};
$opts->{results_location_outpsp}  = q{$[results_location_outpsp]};
$opts->{tag_outpp}                = q{$[tag_outpp]};

$[/myProject/procedure_helpers/preamble_pl]

$azure->create_deployment();
exit($opts->{exitcode});
