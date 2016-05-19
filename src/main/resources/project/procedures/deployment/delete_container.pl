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
# delete_container.pl
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

$[/myProject/procedure_helpers/preamble_pl]

$azure->delete_container();
exit($opts->{exitcode});
