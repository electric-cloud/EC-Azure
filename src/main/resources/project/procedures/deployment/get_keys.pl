##########################
# get_keys.pl
##########################
use warnings;
use strict;
use Encode;
use utf8;
use open IO => ':encoding(utf8)';

my $opts;

$opts->{connection_config}       = q{$[connection_config]};
$opts->{service_name}            = q{$[service_name]};
$opts->{results_location_outpsp} = q{$[results_location_outpsp]};
$opts->{tag_outpp}               = q{$[tag_outpp]};

$[/myProject/procedure_helpers/preamble]

$azure->get_storage_account_keys();
exit($opts->{exitcode});
