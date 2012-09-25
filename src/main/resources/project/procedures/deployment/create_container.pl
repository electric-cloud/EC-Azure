##########################
# create_container.pl
##########################
use warnings;
use strict;
use Encode;
use utf8;
use open IO => ':encoding(utf8)';

my $opts;

$opts->{connection_config}       = q{$[connection_config]};
$opts->{storage_account}         = q{$[storage_account]};
$opts->{container_name}          = q{$[container_name]};
$opts->{storage_account_key}     = q{$[storage_account_key]};
$opts->{results_location_outpsp} = q{$[results_location_outpsp]};
$opts->{tag_outpp}               = q{$[tag_outpp]};

$[/myProject/procedure_helpers/preamble]

$azure->create_container();
exit($opts->{exitcode});
