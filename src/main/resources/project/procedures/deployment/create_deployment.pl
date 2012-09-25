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

$[/myProject/procedure_helpers/preamble]

$azure->create_deployment();
exit($opts->{exitcode});
