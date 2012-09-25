##########################
# delete_hosted.pl
##########################
use warnings;
use strict;
use Encode;
use utf8;
use open IO => ':encoding(utf8)';

my $opts;

$opts->{connection_config}       = q{$[connection_config]};
$opts->{service_name}            = q{$[service_name]};

$[/myProject/procedure_helpers/preamble]

$azure->delete_hosted_service();
exit($opts->{exitcode});
