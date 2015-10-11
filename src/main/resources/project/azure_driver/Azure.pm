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

package Azure;

# -------------------------------------------------------------------------
# Includes
# -------------------------------------------------------------------------
use warnings;
use ElectricCommander::PropDB;
use strict;
use LWP::UserAgent;
use MIME::Base64;
use Date::Format;
use URI::QueryParam;
use Digest::SHA qw(hmac_sha256_base64);
use Encode;
use utf8;

use lib "$ENV{COMMANDER_PLUGINS}/@PLUGIN_NAME@/agent/lib";
use Readonly;
use open IO => ':encoding(utf8)';

use XML::XPath;
use Data::Dumper;

# -------------------------------------------------------------------------
# Constants
# -------------------------------------------------------------------------

Readonly my $DEFAULT_DEBUG => 1;
Readonly my $ERROR         => 1;
Readonly my $SUCCESS       => 0;
Readonly my $EMPTY         => q{};
Readonly my %EMPTY_HASH    => ();

Readonly my $DEFAULT_API_VERSION  => '2012-03-01';
Readonly my $DEFAULT_LOCATION     => "/myJob/Azure/deployed";
Readonly my $DEFAULT_PING_TIMEOUT => 100;

Readonly my $ALIVE     => 1;
Readonly my $NOT_ALIVE => 0;

Readonly my $WAIT_SLEEP_TIME => 45;

Readonly my $PATH_TO_CLIENT => "$ENV{COMMANDER_PLUGINS}/@PLUGIN_NAME@/console/AzureRequest.exe";

# -------------------------------------------------------------------------
# Main functions
# -------------------------------------------------------------------------

###########################################################################

=head2 new
 
  Title    : new
  Usage    : new($ec, $opts);
  Function : Object constructor for Azure.
  Returns  : Azure instance
  Args     : named arguments:
           : -_cmdr => ElectricCommander instance
           : -_opts => hash of parameters from procedures
           :
=cut

###########################################################################
sub new {
    my $class = shift;
    my $self = {
                 _cmdr => shift,
                 _opts => shift,
               };
    bless $self, $class;
    return $self;
}

###########################################################################

=head2 create_hosted_service
 
  Title    : create_hosted_service
  Usage    : $self->create_hosted_service();
  Function : Create a new hosted service
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub create_hosted_service {
    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->initializePropPrefix();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $subscription_id = $self->opts->{subscription_id};
    my $thumbprint      = $self->opts->{thumbprint};
    my $url_text        = 'services/hostedservices';
    my $api_version     = '2012-03-01';  #$self->opts->{'x-ms-version'};

    $self->debug_msg(1, 'Creating request to create a new hosted service \'' . $self->opts->{service_name} . '\'...');

    #Create Hosted Service body
    my $encoded_label = encode_base64($self->opts->{label}, '');
    my $hostedservice_content = "<?xml version=\"\"1.0\"\" encoding=\"\"utf-8\"\"?><CreateHostedService xmlns=\"\"http://schemas.microsoft.com/windowsazure\"\"><ServiceName>" . $self->opts->{service_name} . "</ServiceName><Label>" . $encoded_label . "</Label><Description>" . $self->opts->{description} . "</Description>";
    ($self->opts->{location} ne $EMPTY) ? ($hostedservice_content .= "<Location>" . $self->opts->{location} . "</Location>") : ($hostedservice_content .= "<AffinityGroup>" . $self->opts->{affinity_group} . "</AffinityGroup>");

    if ($self->opts->{name} ne $EMPTY) {
        $hostedservice_content .= "<ExtendedProperties><ExtendedProperty><Name>" . $self->opts->{name} . "</Name><Value>" . $self->opts->{value} . "</Value></ExtendedProperty></ExtendedProperties>";
    }

    $hostedservice_content .= "</CreateHostedService>";

    $self->debug_msg(1, 'Sending request to \'https://management.core.windows.net\'...');
    my $result = $self->azure_request('create-hosted-service', $subscription_id, $thumbprint, 'POST', $url_text, $api_version, qq{"$hostedservice_content"});
    if ($self->opts->{exitcode}) { return; }

    #Store properties

    $self->debug_msg(1, 'Hosted Service \'' . $self->opts->{service_name} . '\' succesfully created!');

    $self->debug_msg(1, 'Storing information in property sheet \'' . $self->opts->{PropPrefix} . '\'...');

    my $setResult = $self->setProp("/HostedService", $self->opts->{service_name});
    $setResult = $self->setProp("/Description", $self->opts->{description});
    if ($self->opts->{location} ne $EMPTY) {
        $setResult = $self->setProp("/Location", $self->opts->{location});
    }
    else {
        $setResult = $self->setProp("/AffinityGroup", $self->opts->{affinity_group});
    }

    if ($self->opts->{name} ne $EMPTY) {
        $setResult = $self->setProp("/ExtendedProperties/Name",  $self->opts->{name});
        $setResult = $self->setProp("/ExtendedProperties/Value", $self->opts->{value});
    }

    if ($result =~ m/.*Request\sId:\s([A-Za-z0-9]+)/ixmsg) {
        my $request_id = $1;
        $self->debug_msg(1, 'RequestID: ' . $request_id);

        $setResult = $self->setProp("/RequestID", $request_id);
    }

    return;
}

###########################################################################

=head2 delete_hosted_service
 
  Title    : delete_hosted_service
  Usage    : $self->delete_hosted_service();
  Function : Delete an existing hosted service
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub delete_hosted_service {
    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $subscription_id = $self->opts->{subscription_id};
    my $thumbprint      = $self->opts->{thumbprint};
    my $url_text        = 'services/hostedservices/' . $self->opts->{service_name};
    my $api_version     = '2012-03-01';  #$self->opts->{'x-ms-version'};

    $self->debug_msg(1, 'Creating request to delete hosted service \'' . $self->opts->{service_name} . '\'...');

    $self->debug_msg(1, 'Sending request to \'https://management.core.windows.net\'...');
    my $result = $self->azure_request('delete-hosted-service', $subscription_id, $thumbprint, 'DELETE', $url_text, $api_version, $EMPTY);
    if ($self->opts->{exitcode}) { return; }

    $self->debug_msg(1, 'Hosted Service \'' . $self->opts->{service_name} . '\' succesfully deleted!');

    if ($result =~ m/.*Request\sId:\s([A-Za-z0-9]+)/ixmsg) {
        my $request_id = $1;
        $self->debug_msg(1, 'RequestID: ' . $request_id);
    }
    return;
}

###########################################################################

=head2 create_storage_account
 
  Title    : create_storage_account
  Usage    : $self->create_storage_account();
  Function : Create a new storage account
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub create_storage_account {
    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->initializePropPrefix();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $subscription_id = $self->opts->{subscription_id};
    my $thumbprint      = $self->opts->{thumbprint};
    my $url_text        = 'services/storageservices';
    my $api_version     = '2012-03-01'; #$self->opts->{'x-ms-version'};

    $self->debug_msg(1, 'Creating request to create a new storage account \'' . $self->opts->{service_name} . '\'...');

    #Create Storage Account body
    my $encoded_label = encode_base64($self->opts->{label}, '');
    my $storageaccount_content = "<?xml version=\"\"1.0\"\" encoding=\"\"utf-8\"\"?><CreateStorageServiceInput xmlns=\"\"http://schemas.microsoft.com/windowsazure\"\"><ServiceName>" . $self->opts->{service_name} . "</ServiceName><Label>" . $encoded_label . "</Label><Description>" . $self->opts->{description} . "</Description>";
    ($self->opts->{location} ne $EMPTY) ? ($storageaccount_content .= "<Location>" . $self->opts->{location} . "</Location>") : ($storageaccount_content .= "<AffinityGroup>" . $self->opts->{affinity_group} . "</AffinityGroup>");

    $storageaccount_content .= "<GeoReplicationEnabled>" . $self->opts->{geo_replication_enabled} . "</GeoReplicationEnabled>";

    if ($self->opts->{name} ne $EMPTY) {
        $storageaccount_content .= "<ExtendedProperties><ExtendedProperty><Name>" . $self->opts->{name} . "</Name><Value>" . $self->opts->{value} . "</Value></ExtendedProperty></ExtendedProperties>";
    }

    $storageaccount_content .= "</CreateStorageServiceInput>";

    $self->debug_msg(1, 'Sending request to \'https://management.core.windows.net\'...');
    my $result = $self->azure_request('create-storage-account', $subscription_id, $thumbprint, 'POST', $url_text, $api_version, qq{"$storageaccount_content"});
    if ($self->opts->{exitcode}) { return; }

    #Get Operation Status
    my $request_id;
    if ($result =~ m/.*Request\sId:\s([A-Za-z0-9]+)/ixmsg) {
        $request_id = $1;
    }
    $self->get_operation_status($request_id);
    if ($self->opts->{exitcode}) { return; }

    #Store properties
    $self->debug_msg(1, 'Storage Account \'' . $self->opts->{service_name} . '\' succesfully created!');
    $self->debug_msg(1, 'Storing information in property sheet \'' . $self->opts->{PropPrefix} . '\'...');

    my $setResult = $self->setProp("/StorageAccount", $self->opts->{service_name});
    $setResult = $self->setProp("/Description", $self->opts->{description});
    if ($self->opts->{location} ne $EMPTY) {
        $setResult = $self->setProp("/Location", $self->opts->{location});
    }
    else {
        $setResult = $self->setProp("/AffinityGroup", $self->opts->{affinity_group});
    }

    $setResult = $self->setProp("/GeoReplicationEnabled", $self->opts->{geo_replication_enabled});
    if ($self->opts->{name} ne $EMPTY) {
        $setResult = $self->setProp("/ExtendedProperties/Name",  $self->opts->{name});
        $setResult = $self->setProp("/ExtendedProperties/Value", $self->opts->{value});
    }

    if ($result =~ m/.*Request\sId:\s([A-Za-z0-9]+)/ixmsg) {
        my $request_id = $1;
        $self->debug_msg(1, 'RequestID: ' . $request_id);

        $setResult = $self->setProp("/RequestID", $request_id);
    }
    return;
}

###########################################################################

=head2 delete_storage_account
 
  Title    : delete_storage_account
  Usage    : $self->delete_storage_account();
  Function : Delete an existing storage account
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub delete_storage_account {
    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $subscription_id = $self->opts->{subscription_id};
    my $thumbprint      = $self->opts->{thumbprint};
    my $url_text        = 'services/storageservices/' . $self->opts->{service_name};
    my $api_version     = '2012-03-01';  #$self->opts->{'x-ms-version'};

    $self->debug_msg(1, 'Creating request to delete storage account \'' . $self->opts->{service_name} . '\'...');

    $self->debug_msg(1, 'Sending request to \'https://management.core.windows.net\'...');
    my $result = $self->azure_request('delete-storage-account', $subscription_id, $thumbprint, 'DELETE', $url_text, $api_version, $EMPTY);
    if ($self->opts->{exitcode}) { return; }

    $self->debug_msg(1, 'Storage Account \'' . $self->opts->{service_name} . '\' succesfully deleted!');

    if ($result =~ m/.*Request\sId:\s([A-Za-z0-9]+)/ixmsg) {
        my $request_id = $1;
        $self->debug_msg(1, 'RequestID: ' . $request_id);
    }

    return;
}

###########################################################################

=head2 get_storage_account_keys
 
  Title    : get_storage_account_keys
  Usage    : $self->get_storage_account_keys();
  Function : Gets the primary and scondary keys from a storage account
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub get_storage_account_keys {
    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->initializePropPrefix();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $subscription_id = $self->opts->{subscription_id};
    my $thumbprint      = $self->opts->{thumbprint};
    my $url_text        = 'services/storageservices/' . $self->opts->{service_name} . '/keys';
    my $api_version     = '2012-03-01';  #$self->opts->{'x-ms-version'};

    $self->debug_msg(1, 'Creating request to get storage account keys from \'' . $self->opts->{service_name} . '\'...');

    $self->debug_msg(1, 'Sending request to \'https://management.core.windows.net\'...');
    my $result = $self->azure_request('get-storage-account-keys', $subscription_id, $thumbprint, 'GET', $url_text, $api_version, $EMPTY);
    if ($self->opts->{exitcode}) { return; }

    my $url       = $EMPTY;
    my $primary   = $EMPTY;
    my $secondary = $EMPTY;
    my $xPath;

    if ($result =~ m/.*(\<StorageService\s.+\<\/StorageService\>).*/ixmsg) {
        $xPath = XML::XPath->new($1);

        $url       = $xPath->find('/StorageService/Url')->string_value;
        $primary   = $xPath->find('/StorageService/StorageServiceKeys/Primary')->string_value;
        $secondary = $xPath->find('/StorageService/StorageServiceKeys/Secondary')->string_value;
    }
    else {

        $self->debug_msg(1, 'Error: Unable to get storage account keys!');
        return $ERROR;
    }

    #Store properties
    $self->debug_msg(1, 'Storage Account keys from \'' . $self->opts->{service_name} . '\' succesfully got!');
    $self->debug_msg(1, 'Storing information in property sheet \'' . $self->opts->{PropPrefix} . '\'...');

    my $setResult = $self->setProp("/StorageServiceUrl", $url);
    $setResult = $self->setProp("/StorageServiceKeys/Primary",   $primary);
    $setResult = $self->setProp("/StorageServiceKeys/Secondary", $secondary);

    if ($result =~ m/.*Request\sId:\s([A-Za-z0-9]+)/ixmsg) {
        my $request_id = $1;
        $self->debug_msg(1, 'RequestID: ' . $request_id);

        $setResult = $self->setProp("/RequestID", $request_id);
    }
    return;
}
###########################################################################

=head2 get_status
 
  Title    : get_status
  Usage    : $self->get_status();
  Function : Gets the current status of an operation
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub get_status {
    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->initializePropPrefix();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $subscription_id = $self->opts->{subscription_id};
    my $thumbprint      = $self->opts->{thumbprint};
    my $url_text        = 'operations/' . $self->opts->{request_id};
    my $api_version     = '2012-03-01';  #$self->opts->{'x-ms-version'};

    $self->debug_msg(1, 'Creating request to get operation status for request id \'' . $self->opts->{request_id} . '\'...');

    $self->debug_msg(1, 'Sending request to \'https://management.core.windows.net\'...');
    my $result = $self->azure_request('get-operation-status', $subscription_id, $thumbprint, 'GET', $url_text, $api_version, $EMPTY);
    if ($self->opts->{exitcode}) { return; }

    if ($result =~ m/.*(\<Operation.+\<\/Operation\>).*/ixmsg) {
        my $xPath = XML::XPath->new($1);

        my $status = $xPath->find('/Operation/Status')->string_value;

        #Store properties
        $self->debug_msg(1, 'Storing information in property sheet \'' . $self->opts->{PropPrefix} . '\'...');

        $self->debug_msg(1, "\nStatus: $status");
        my $setResult = $self->setProp("/Status", $status);
        if ($status eq 'Failed') {

            my $error_code = $xPath->find('/Operation/Error/Code');
            my $error_msg  = $xPath->find('/Operation/Error/Message');
            $self->debug_msg(0, 'Code: ' . $error_code);
            $self->debug_msg(0, 'Message: ' . $error_msg);

            $setResult = $self->setProp("/Error_Code",    $error_code);
            $setResult = $self->setProp("/Error_Message", $error_msg);

        }

        my $request = $xPath->find('/Operation/ID');

        $setResult = $self->setProp("/RequestID", $request);

    }
    else {
        $self->opts->{exitcode} = $ERROR;
        $self->debug_msg(0, "\nRequest Failed!");
        return;

    }
    return;
}

###########################################################################

=head2 create_container
 
  Title    : create_container
  Usage    : $self->create_container();
  Function : Creates a new container in the storage account
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub create_container {

    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->initializePropPrefix();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $storage_account     = $self->opts->{storage_account};
    my $container_name      = $self->opts->{container_name};
    my $storage_account_key = $self->opts->{storage_account_key};
    my $api_version         = '2009-09-19';#$self->opts->{'x-ms-version'};
    my $url                 = 'http://' . $storage_account . '.blob.core.windows.net/' . $container_name . '?restype=container';

    $self->debug_msg(1, 'Creating request to create container \'' . $container_name . '\'...');

    $self->debug_msg(1, 'Sending request to \'' . $url . '\'...');
    my $result = $self->rest_request('PUT', $url, $api_version, $EMPTY, 'application/xml', $storage_account_key, \%EMPTY_HASH);
    if ($self->opts->{exitcode}) { return; }

    $self->debug_msg(1, 'Container \'' . $container_name . '\' succesfully created!');

    #my @response_headers = $result->header_field_names;

    my $etag       = $result->header('ETag');
    my $request_id = $result->header('x-ms-request-id');

    #Store properties
    $self->debug_msg(1, 'Storing information in property sheet \'' . $self->opts->{PropPrefix} . '\'...');

    my $setResult = $self->setProp("/Container", $container_name);
    $setResult = $self->setProp("/Url",       $url);
    $setResult = $self->setProp("/ETag",      $etag);
    $setResult = $self->setProp("/RequestID", $request_id);

    return;

}

###########################################################################

=head2 delete_container
 
  Title    : delete_container
  Usage    : $self->delete_container();
  Function : Deletes a container in the storage account
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub delete_container {

    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $storage_account     = $self->opts->{storage_account};
    my $container_name      = $self->opts->{container_name};
    my $storage_account_key = $self->opts->{storage_account_key};
    my $api_version         = '2009-09-19'; #$self->opts->{'x-ms-version'};
    my $url                 = 'http://' . $storage_account . '.blob.core.windows.net/' . $container_name . '?restype=container';

    $self->debug_msg(1, 'Creating request to delete container \'' . $container_name . '\'...');

    $self->debug_msg(1, 'Sending request to \'' . $url . '\'...');
    my $result = $self->rest_request('DELETE', $url, $api_version, $EMPTY, 'application/xml', $storage_account_key, \%EMPTY_HASH);
    if ($self->opts->{exitcode}) { return; }

    $self->debug_msg(1, 'Container \'' . $container_name . '\' succesfully deleted!');

    my $request_id = $result->header('x-ms-request-id');

    $self->debug_msg(1, 'RequestID: ' . $request_id);

    return;

}

###########################################################################

=head2 put_blob
 
  Title    : put_blob
  Usage    : $self->put_blob();
  Function : Creates a new blob in the storage account
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub put_blob {

    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->initializePropPrefix();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $storage_account     = $self->opts->{storage_account};
    my $container_name      = $self->opts->{container_name};
    my $storage_account_key = $self->opts->{storage_account_key};
    my $blob_type           = $self->opts->{blob_type};
    my $blob_name           = $self->opts->{blob_name};
    my $blob_content        = $EMPTY;
    my $upload_file         = $self->opts->{upload_file};
    my $content_type        = 'application/xml';
    my $temp_debug          = $self->opts->{debug_level};

    my %headers = ('x-ms-blob-type' => $blob_type);

    if ($blob_type eq 'PageBlob') {
        my $blob_content_length  = $self->opts->{blob_content_length};
        my $blob_sequence_number = $self->opts->{blob_sequence_number};

        $headers{'x-ms-blob-content-length'}  = $blob_content_length;
        $headers{'x-ms-blob-sequence-number'} = $blob_sequence_number;

    }
    elsif ($blob_type eq 'BlockBlob') {
        if ($upload_file eq "1") {

            my $filepath = $self->opts->{filepath};

            open FILE, $filepath or die $!;
            binmode FILE;
            $blob_content = join("", <FILE>);
            close FILE;

            $content_type = 'application/octet-stream';
            $filepath =~ m/([^\/|^\\]*)$/ixmsg;
            $blob_name = $1;

            $self->opts->{debug_level} = '1';

        }
        else {

            $blob_content = $self->opts->{blob_content};

        }
    }

    my $api_version = '2009-09-19'; #$self->opts->{'x-ms-version'};
    my $url         = 'http://' . $storage_account . '.blob.core.windows.net/' . $container_name . '/' . $blob_name;

    $self->debug_msg(1, 'Creating request to create blob \'' . $blob_name . '\'...');

    $self->debug_msg(1, 'Sending request to \'' . $url . '\'...');
    my $result = $self->rest_request('PUT', $url, $api_version, $blob_content, $content_type, $storage_account_key, \%headers);
    if ($self->opts->{exitcode}) { return; }
    $self->opts->{debug_level} = $temp_debug;

    $self->debug_msg(1, 'Blob \'' . $blob_name . '\' succesfully created!');

    my $etag       = $result->header('ETag');
    my $request_id = $result->header('x-ms-request-id');

    #Store properties
    $self->debug_msg(1, 'Storing information in property sheet \'' . $self->opts->{PropPrefix} . '\'...');

    my $setResult = $self->setProp("/Container", $container_name);
    $setResult = $self->setProp("/Blob",      $blob_name);
    $setResult = $self->setProp("/Url",       $url);
    $setResult = $self->setProp("/ETag",      $etag);
    $setResult = $self->setProp("/RequestID", $request_id);

    return;

}

###########################################################################

=head2 delete_blob
 
  Title    : delete_blob
  Usage    : $self->delete_blob();
  Function : Deletes a blob in the storage account
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub delete_blob {

    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $storage_account     = $self->opts->{storage_account};
    my $container_name      = $self->opts->{container_name};
    my $storage_account_key = $self->opts->{storage_account_key};
    my $blob_name           = $self->opts->{blob_name};
    my $api_version         = '2009-09-19';#$self->opts->{'x-ms-version'};
    my $url                 = 'http://' . $storage_account . '.blob.core.windows.net/' . $container_name . '/' . $blob_name;
    my %headers             = ('x-ms-delete-snapshots' => 'include');

    $self->debug_msg(1, 'Creating request to delete blob \'' . $blob_name . '\'...');

    $self->debug_msg(1, 'Sending request to \'' . $url . '\'...');
    my $result = $self->rest_request('DELETE', $url, $api_version, $EMPTY, 'application/xml', $storage_account_key, \%headers);
    if ($self->opts->{exitcode}) { return; }

    $self->debug_msg(1, 'Blob \'' . $blob_name . '\' succesfully deleted!');

    my $request_id = $result->header('x-ms-request-id');

    $self->debug_msg(1, 'RequestID: ' . $request_id);

    return;

}

###########################################################################

=head2 create_deployment
 
  Title    : create_deployment
  Usage    : $self->create_deployment();
  Function : Creates a new deployment
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub create_deployment {
    my $self = shift;

    $self->initialize();
    if ($self->opts->{exitcode}) { return; }
    $self->initializePropPrefix();
    if ($self->opts->{exitcode}) { return; }
    $self->debug_msg(1, '---------------------------------------------------------------------');

    my $subscription_id = $self->opts->{subscription_id};
    my $thumbprint      = $self->opts->{thumbprint};
    my $url_text        = 'services/hostedservices/' . $self->opts->{service_name} . '/deploymentslots/' . $self->opts->{deployment_slot};
    my $api_version     = '2012-03-01';                                                                                                      #$self->opts->{'x-ms-version'};

    $self->debug_msg(1, 'Creating request to create a new deployment \'' . $self->opts->{deployment_name} . '\'...');

    #Create Deployment body
    my $encoded_label = encode_base64($self->opts->{label}, '');

    my $filepath = $self->opts->{deployment_configuration};

    open FILE, $filepath or die $!;
    binmode FILE;
    my $config = join('', <FILE>);
    close FILE;

    #Clean beginning of file
    if ($config =~ /^(.*)</) {
        $config =~ s/$1//;
    }

    my $encoded_config = encode_base64($config, '');
    my $deployment_content = "<?xml version=\"\"1.0\"\" encoding=\"\"utf-8\"\"?><CreateDeployment xmlns=\"\"http://schemas.microsoft.com/windowsazure\"\"><Name>" . $self->opts->{deployment_name} . "</Name><PackageUrl>" . $self->opts->{package_url} . "</PackageUrl><Label>" . $encoded_label . "</Label><Configuration>" . $encoded_config . "</Configuration><StartDeployment>" . $self->opts->{start_deployment} . "</StartDeployment><TreatWarningsAsError>" . $self->opts->{treat_warnings_as_error} . "</TreatWarningsAsError>";

    if ($self->opts->{name} ne $EMPTY) {
        $deployment_content .= "<ExtendedProperties><ExtendedProperty><Name>" . $self->opts->{name} . "</Name><Value>" . $self->opts->{value} . "</Value></ExtendedProperty></ExtendedProperties>";
    }

    $deployment_content .= "</CreateDeployment>";

    $self->debug_msg(1, 'Sending request to \'https://management.core.windows.net\'...');
    my $result = $self->azure_request('create-deployment', $subscription_id, $thumbprint, 'POST', $url_text, $api_version, qq{"$deployment_content"});
    if ($self->opts->{exitcode}) { return; }

    #Get Operation Status
    my $request_id;
    if ($result =~ m/.*(Request\sId|x-ms-request-id):\s([A-Za-z0-9]+)/ixmsg) {
        $request_id = $2;
    }
    $self->get_operation_status($request_id);
    if ($self->opts->{exitcode}) { return; }

    #Store properties
    $self->debug_msg(1, 'Deployment \'' . $self->opts->{deployment_name} . '\' succesfully created!');
    $self->debug_msg(1, 'Storing information in property sheet \'' . $self->opts->{PropPrefix} . '\'...');

    my $setResult = $self->setProp("/StorageAccount", $self->opts->{service_name});
    $setResult = $self->setProp("/Deployment",     $self->opts->{deployment_name});
    $setResult = $self->setProp("/Label",          $self->opts->{label});
    $setResult = $self->setProp("/PackageUrl",     $self->opts->{package_url});
    $setResult = $self->setProp("/DeploymentSlot", $self->opts->{deployment_slot});

    if ($self->opts->{name} ne $EMPTY) {
        $setResult = $self->setProp("/ExtendedProperties/Name",  $self->opts->{name});
        $setResult = $self->setProp("/ExtendedProperties/Value", $self->opts->{value});
    }

    if ($result =~ m/.*Request\sId:\s([A-Za-z0-9]+)/ixmsg) {
        my $request_id = $1;
        $self->debug_msg(1, 'RequestID: ' . $request_id);

        $setResult = $self->setProp("/RequestID", $request_id);
    }
    return;
}

# -------------------------------------------------------------------------
# Helper functions
# -------------------------------------------------------------------------

###########################################################################

=head2 myCmdr
 
  Title    : myCmdr
  Usage    : $self->myCmdr();
  Function : Get ElectricCommander instance.
  Returns  : ElectricCommander instance asociated to Azure
  Args     : named arguments:
           : none
           :
=cut

###########################################################################
sub myCmdr {
    my ($self) = @_;
    return $self->{_cmdr};
}

###########################################################################

=head2 opts
 
  Title    : opts
  Usage    : $self->opts();
  Function : Get opts hash.
  Returns  : opts hash
  Args     : named arguments:
           : none
           :
=cut

###########################################################################
sub opts {
    my ($self) = @_;
    return $self->{_opts};
}

###########################################################################

=head2 initialize
 
  Title    : initialize
  Usage    : $self->initialize();
  Function : Set initial values.
  Returns  : none
  Args     : named arguments:
           : none
           :
=cut

###########################################################################
sub initialize {
    my ($self) = @_;

    binmode STDOUT, ':encoding(utf8)';
    binmode STDIN,  ':encoding(utf8)';
    binmode STDERR, ':encoding(utf8)';

    $self->{_props} = new ElectricCommander::PropDB($self->myCmdr(), $EMPTY);

    # Set defaults
    if (!defined($self->opts->{debug_level})) {
        $self->opts->{debug_level} = $DEFAULT_DEBUG;
    }

    $self->opts->{exitcode} = $SUCCESS;

    if (!defined($self->opts->{'x-ms-version'})) {
        $self->opts->{'x-ms-version'} = $DEFAULT_API_VERSION;
    }

    $self->opts->{JobId} = $ENV{COMMANDER_JOBID};

    return;
}

###########################################################################

=head2 myProp
 
  Title    : myProp
  Usage    : $self->myProp();
  Function : Get PropDB.
  Returns  : PropDB
  Args     : named arguments:
           : none
           :
=cut

###########################################################################
sub myProp {
    my ($self) = @_;
    return $self->{_props};
}

###########################################################################

=head2 setProp
 
  Title    : setProp
  Usage    : $self->setProp();
  Function : Use stored property prefix and PropDB to set a property.
  Returns  : setResult => result returned by PropDB->setProp
  Args     : named arguments:
           : -location => relative location to set the property
           : -value    => value of the property
           :
=cut

###########################################################################
sub setProp {
    my ($self, $location, $value) = @_;
    my $setResult = $self->myProp->setProp($self->opts->{PropPrefix} . $location, $value);
    return $setResult;
}

###########################################################################

=head2 getProp
 
  Title    : getProp
  Usage    : $self->getProp();
  Function : Use stored property prefix and PropDB to get a property.
  Returns  : getResult => property value
  Args     : named arguments:
           : -location => relative location to get the property
           :
=cut

###########################################################################
sub getProp {
    my ($self, $location) = @_;
    my $getResult = $self->myProp->getProp($self->opts->{PropPrefix} . $location);
    return $getResult;
}

###########################################################################

=head2 deleteProp
 
  Title    : deleteProp
  Usage    : $self->deleteProp();
  Function : Use stored property prefix and PropDB to delete a property.
  Returns  : delResult => result returned by PropDB->deleteProp
  Args     : named arguments:
           : -location => relative location of the property to delete
           :
=cut

###########################################################################
sub deleteProp {
    my ($self, $location) = @_;
    my $delResult = $self->myProp->deleteProp($self->opts->{PropPrefix} . $location);
    return $delResult;
}

###########################################################################

=head2 debug_msg
 
  Title    : debug_msg
  Usage    : $self->debug_msg();
  Function : Print a debug message.
  Returns  : none
  Args     : named arguments:
           : -errorlevel => number compared to $self->opts->{debug}
           : -msg        => string message
           :
=cut

###########################################################################
sub debug_msg {
    my ($self, $errlev, $msg) = @_;
    if ($self->opts->{debug_level} >= $errlev) { print "$msg\n"; }
    return;
}

###########################################################################

=head2 get_operation_status
 
  Title    : get_operation_status
  Usage    : $self->get_operation_status();
  Function : Gets the status of an operation and waits for it to complete
  Returns  : none
  Args     : -request_id => request id of the operation to track 
           :
=cut

###########################################################################
sub get_operation_status {
    my $self       = shift;
    my $request_id = shift;

    my $subscription_id = $self->opts->{subscription_id};
    my $thumbprint      = $self->opts->{thumbprint};
    my $url_text        = 'operations/' . $request_id;
    my $api_version     = '2012-03-01';  #$self->opts->{'x-ms-version'};

    my $xPath;
    my $status;
    my $error_code;
    my $error_msg;

    my $operation_status = 'InProgress';

    #-----------------------------
    # WAIT UNTIL OPERATION COMPLETES OR RETURNS ERROR
    #-----------------------------
    print 'Waiting for operation to complete...';

    my $temp_debug = $self->opts->{debug_level};

    $self->opts->{debug_level} = '1';

    while ($operation_status eq 'InProgress') {

        my $result = $self->azure_request('get-operation-status', $subscription_id, $thumbprint, 'GET', $url_text, $api_version, $EMPTY);
        if ($self->opts->{exitcode}) { return; }

        if ($result =~ m/.*(\<Operation.+\<\/Operation\>).*/ixmsg) {
            $xPath = XML::XPath->new($1);

            $status = $xPath->find('/Operation/Status')->string_value;

            if ($status eq 'Succeeded') {
                $self->debug_msg(1, "\nOperation succeded!");
                $self->opts->{debug_level} = $temp_debug;
                return;
            }
            elsif ($status eq 'Failed') {
                $self->opts->{exitcode} = $ERROR;

                $error_code = $xPath->find('/Operation/Error/Code');
                $error_msg  = $xPath->find('/Operation/Error/Message');
                $self->debug_msg(0, "\nOperation failed!");
                $self->debug_msg(0, 'Error Code: ' . $error_code);
                $self->debug_msg(0, 'Error Message: ' . $error_msg);
                $self->opts->{debug_level} = $temp_debug;
                return;
            }

            # elsif ($status eq 'InProgress') {
            # print '.';
            # }

        }
        else {
            $self->opts->{exitcode} = $ERROR;
            $self->debug_msg(0, "\nOperation failed!");
            $self->opts->{debug_level} = $temp_debug;
            return;

        }

        #-----------------------------
        # Still running
        #-----------------------------
        sleep($WAIT_SLEEP_TIME);

    }
    $self->opts->{debug_level} = $temp_debug;
    return;

}

###########################################################################

=head2 sign_request
 
  Title    : sign_request
  Usage    : $self->sign_request();
  Function : Signs the rest request with the storage account key
  Returns  : none
  Args     : -request           => HTTP::Request
           : -key               => key to sign the request
           : -api_version       => Windows Azure API version
           : -content_length    => Length of the content body
           : -headers           => Hash with extra headers
           : 
=cut

###########################################################################
sub sign_request {
    my $self           = shift;
    my $request        = shift;
    my $key            = shift;
    my $api_version    = shift;
    my $content_length = shift;
    my $headers        = shift;

    #Common headers
    $request->header('x-ms-version',   $api_version);
    $request->header('x-ms-date',      time2str("%a, %d %b %Y %T %Z", time, 'GMT'));
    $request->header('Content-Length', $content_length);

    #Extra headers
    foreach my $header (keys %{$headers}) {
        $request->header($header, $headers->{$header});
    }

    my $canonicalized_headers = join "", map { lc($_) . ':' . $request->header($_) . "\n" } sort grep { /^x-ms/ } keys %{ $request->headers };

    my $account = ($request->uri->authority =~ /^([^.]*)/ and $1);
    my $canonicalized_resource = "/$account@{[$request->uri->path]}";
    $canonicalized_resource .= join "", map { "\n" . lc($_) . ':' . join(',', sort $request->uri->query_param($_)) } sort $request->uri->query_param;

    chomp(my $string_to_sign = <<END);
@{[$request->method]}
@{[$request->header('Content-Encoding')]}
@{[$request->header('Content-Language')]}
@{[$request->header('Content-Length')]}
@{[$request->header('Content-MD5')]}
@{[$request->header('Content-Type')]}
@{[$request->header('Date')]}
@{[$request->header('If-Modified-Since')]}
@{[$request->header('If-Match')]}
@{[$request->header('If-None-Match')]}
@{[$request->header('If-Unmodified-Since')]}
@{[$request->header('Range')]}
$canonicalized_headers$canonicalized_resource
END
    my $signature = hmac_sha256_base64($string_to_sign, decode_base64($key));
    $signature .= '=' x (4 - (length($signature) % 4));

    $request->authorization("SharedKey $account:$signature");
    return;
}

###########################################################################

=head2 rest_request
 
  Title    : rest_request
  Usage    : $self->rest_request();
  Function : Send a rest request to Windows Azure using LWP
  Returns  : none
  Args     : -request_method    => HTTP request method
           : -url_text          => Suffix for the url to send the request
           : -api_version       => Windows Azure API version
           : -content           => Content body
           : -content_type      => Type of the content body
           : -key               => Key to sign the request
           : -headers           => Hash with extra headers
           :
=cut

###########################################################################
sub rest_request {
    my $self           = shift;
    my $request_method = shift;
    my $url_text       = shift;
    my $api_version    = shift;
    my $content        = shift;
    my $content_type   = shift;
    my $key            = shift;
    my $headers        = shift;

    my $url;
    my $request;
    my $response;
    my $client;

    if ($url_text eq $EMPTY) {
        $self->debug_msg(0, "Error: blank URL in rest_request.");
        $self->opts->{exitcode} = $ERROR;
        return $EMPTY;
    }

    $url = URI->new($url_text);
    if ($request_method eq "POST") {
        $request = HTTP::Request->new(POST => $url);
    }
    elsif ($request_method eq "DELETE") {
        $request = HTTP::Request->new(DELETE => $url);
    }
    elsif ($request_method eq "PUT") {
        $request = HTTP::Request->new(PUT => $url);
    }
    else {
        $request = HTTP::Request->new(GET => $url);
    }

    $client = LWP::UserAgent->new;

    $request->content_type($content_type);

    $self->sign_request($request, $key, $api_version, length($content), $headers);

    if ($content_type ne $EMPTY) {
        $request->content_type($content_type);
    }
    else {
        $request->content_type('application/xml');
    }
    if ($content ne $EMPTY) {
        $request->content($content);
    }

    $self->debug_msg(2, "\nRequest:\n" . $request->as_string);
    $response = $client->request($request);
    $self->debug_msg(1, "\nResponse:\n" . $response->content);

    if ($response->is_error) {
        $self->debug_msg(0, "Failed to " . $request_method . " '$url': " . $response->status_line);
        $self->opts->{exitcode} = $ERROR;
        return $EMPTY;
    }

    return $response;

}

###########################################################################

=head2 azure_request
 
  Title    : azure_request
  Usage    : $self->azure_request();
  Function : Send a rest request to Windows Azure using the client in C#
  Returns  : none
  Args     : -operation         => Name of the operation to request
           : -subscription_id   => Subscription id for Windows Azure
           : -thumbprint        => Thumbprint of the certificate to use
           : -request_method    => HTTP request method
           : -url_text          => Suffix for the url to send the request
           : -api_version       => Windows Azure API version
           : -content           => Content body
           :
=cut

###########################################################################
sub azure_request {
    my $self            = shift;
    my $operation       = shift;
    my $subscription_id = shift;
    my $thumbprint      = shift;
    my $request_method  = shift;
    my $url_text        = shift;
    my $api_version     = shift;
    my $content         = shift;

    my @cmd;
    my $command;
    my $result = $EMPTY;

    #Create command line
    push(@cmd, qq{"$PATH_TO_CLIENT"});    # Executable
    push(@cmd, qq{$operation});           # Operation
    push(@cmd, qq{$subscription_id});     # Subscription_id
    push(@cmd, qq{"$thumbprint"});        # thumbprint
    push(@cmd, qq{$request_method});      # Method
    push(@cmd, qq{$url_text});            # Url
    push(@cmd, qq{$api_version});         # version

    if ($content && $content ne $EMPTY) {
        push(@cmd, qq{$content});
    }

    $command = join(" ", @cmd);
    $self->debug_msg(3, $command);

    eval { $result = `$command 2>&1`; };

    if (($result =~ m/.*\n(Error|Exception|Call\sto\sWindows\sAzure\sreturned\san\serror).*/ixmsg) | ($result eq $EMPTY)) {

        $self->debug_msg(0, "Call to Windows Azure returned an error:");
        my $error = $result;
        my $req   = $result;

        $error =~ m/.*(Call\sto\sWindows\sAzure\sreturned\san\serror:\n|Error:^\n)(.+)/ixsmg;
        $self->debug_msg(0, "$2");

        if ($req =~ m/.*Request:\n(.+)(Call|Error:^\n)/ixsmg) {
            $self->debug_msg(0, "\nRequest:\n$1");
        }

        $self->opts->{exitcode} = $ERROR;
        return $EMPTY;
    }

    $self->debug_msg(2, $result);

    return $result;
}

###########################################################################

=head2 initializePropPrefix
 
  Title    : initializePropPrefix
  Usage    : $self->initializePropPrefix();
  Function : Initialize PropPrefix value and check valid location
  Returns  : none
  Args     : none
           :
=cut

###########################################################################
sub initializePropPrefix {
    my $self = shift;

    # setup the property sheet where information will be exchanged
    if (!defined($self->opts->{results_location_outpsp}) || $self->opts->{results_location_outpsp} eq "") {
        if ($self->opts->{JobStepId} ne "1") {
            $self->opts->{results_location_outpsp} = $DEFAULT_LOCATION;    # default location to save properties
            $self->debug_msg(5, "Using default location for results");
        }
        else {
            $self->debug_msg(0, "Must specify property sheet location when not running in job");
            $self->opts->{exitcode} = $ERROR;
            return;
        }
    }
    $self->opts->{PropPrefix} = $self->opts->{results_location_outpsp};
    if (defined($self->opts->{tag_outpp}) && $self->opts->{tag_outpp} ne "") {
        $self->opts->{PropPrefix} .= "/" . $self->opts->{tag_outpp};
    }
    $self->debug_msg(5, "Results will be in:" . $self->opts->{PropPrefix});

    # test that the location is valid
    if ($self->checkValidLocation) {
        $self->opts->{exitcode} = $ERROR;
        return;
    }
}

###########################################################################

=head2 checkValidLocation
 
  Title    : checkValidLocation
  Usage    : $self->checkValidLocation();
  Function : Check if location specified in PropPrefix is valid
  Returns  : 0 - Success
           : 1 - Error
  Args     : none
           :
=cut

###########################################################################
sub checkValidLocation {
    my $self     = shift;
    my $location = "/test-" . $self->opts->{JobStepId};

    # Test set property in location
    my $result = $self->setProp($location, "Test property");
    if (!defined($result) || $result eq "") {
        $self->debug_msg(0, "Invalid location: " . $self->opts->{PropPrefix});
        return $ERROR;
    }

    # Test get property in location
    $result = $self->getProp($location);
    if (!defined($result) || $result eq "") {
        $self->debug_msg(0, "Invalid location: " . $self->opts->{PropPrefix});
        return $ERROR;
    }

    # Delete property
    $result = $self->deleteProp($location);
    return $SUCCESS;
}

1;

