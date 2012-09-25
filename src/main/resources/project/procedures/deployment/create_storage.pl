##########################
# create_storage.pl
##########################
use warnings;
use strict;
use Encode;
use utf8;
use open IO => ':encoding(utf8)';

my $opts;

$opts->{connection_config}       = q{$[connection_config]};
$opts->{service_name}            = q{$[service_name]};
$opts->{label}                   = q{$[label]};
$opts->{description}             = q{$[description]};
$opts->{location}                = q{$[location]};
$opts->{affinity_group}          = q{$[affinity_group]};
$opts->{geo_replication_enabled} = q{$[geo_replication_enabled]};
$opts->{name}                    = q{$[name]};
$opts->{value}                   = q{$[value]};
$opts->{results_location_outpsp} = q{$[results_location_outpsp]};
$opts->{tag_outpp}               = q{$[tag_outpp]};

$[/myProject/procedure_helpers/preamble]

$azure->create_storage_account();
exit($opts->{exitcode});
