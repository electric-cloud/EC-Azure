##########################
# put_blob.pl
##########################
use warnings;
use strict;
use Encode;
use utf8;
use open IO => ':encoding(utf8)';

my $opts;

$opts->{connection_config}   = q{$[connection_config]};
$opts->{storage_account}     = q{$[storage_account]};
$opts->{container_name}      = q{$[container_name]};
$opts->{storage_account_key} = q{$[storage_account_key]};

$opts->{upload_file}             = q{$[upload_file]};
$opts->{filepath}                = q{$[filepath]};
$opts->{blob_name}               = q{$[blob_name]};
$opts->{blob_type}               = q{$[blob_type]};
$opts->{blob_content_length}     = q{$[blob_content_length]};
$opts->{blob_sequence_number}    = q{$[blob_sequence_number]};
$opts->{blob_content}            = q{$[blob_content]};
$opts->{results_location_outpsp} = q{$[results_location_outpsp]};
$opts->{tag_outpp}               = q{$[tag_outpp]};

$[/myProject/procedure_helpers/preamble]

$azure->put_blob();
exit($opts->{exitcode});
