use warnings;
use strict;
use Encode;
use Data::Dumper;
use utf8;
use HTTP::Request;
use LWP::UserAgent;
use JSON;
use ElectricCommander;

use open IO => ':encoding(utf8)';
$| = 1;
$ENV{PERL_LWP_SSL_VERIFY_HOSTNAME} = 0;
my $opts;

$opts->{tenant_id}            = q{$[tenant_id]};
$opts->{cloud_id}             = q{$[cloud_id]};
$opts->{vm_template_id}       = q{$[vm_template_id]};
$opts->{vm_name}              = q{$[vm_name]};
$opts->{vm_network_name}      = q{$[vm_network_name]};
$opts->{stamp_id}             = q{$[stamp_id]};
$opts->{local_admin_username} = q{$[local_admin_username]};
$opts->{local_admin_password} = q{$[local_admin_password]};
$opts->{admin_password}       = q{$[admin_password]};
$opts->{wap_url}              = q{$[wap_url]};
$opts->{wap_public_key}       = q{$[wap_public_key]};
$opts->{wap_private_key}      = q{$[wap_private_key]};

main($opts);


sub main {
    my ($o) = @_;

    my $ec = ElectricCommander->new();
    my $ua = get_ua($o->{wap_public_key}, $o->{wap_private_key});
    print "UA constructed\n";
    $o->{wap_url} =~ s|\/+$||gs;
    my $req_data = {
        base_url => $o->{wap_url},
        ua => $ua,
    };
    print "Performing request\n";
    my $tenant = rr(
        $req_data,
        GET => "/subscriptions/$o->{tenant_id}"
    );

    print "Done request\n";
    if ($tenant->code() > 399) {
        print "Error occured." . Dumper $tenant;
        exit 1;
    }

    my $dc = decode_json($tenant->decoded_content());
    print "Email: $dc->{AccountAdminLiveEmailId}\n";
    my $json_template = join '', <DATA>;
    # cloud id substitution
    $json_template =~ s|###cloud_id###|$o->{cloud_id}|gms;
    $json_template =~ s|###local_admin_password###|$o->{local_admin_password}|gms;
    $json_template =~ s|###local_admin_username###|$o->{local_admin_username}|gms;
    $json_template =~ s|###vm_network_name###|$o->{vm_network_name}|gms;
    $json_template =~ s|###owner_email###|$dc->{AccountAdminLiveEmailId}|gms;
    $json_template =~ s|###admin_password###|$o->{admin_password}|gms;
    $json_template =~ s|###stamp_id###|$o->{stamp_id}|gms;
    $json_template =~ s|###vm_template_id###|$o->{vm_template_id}|gms;
    $json_template =~ s|###vm_name###|$o->{vm_name}|gms;

    my $vm = rr(
        $req_data,
        POST => "/$o->{tenant_id}/services/systemcenter/vmm/VirtualMachines",
        $json_template
    );
    if ($vm->code() > 399) {
        print "Unable to create vm. Response: " . Dumper $vm;
        exit 1;
    }
    my $obj = decode_json($vm->decoded_content);
    print "Will wait for task: $obj->{MostRecentTaskId}\n";
    my $time = time();
    my $end = $time + 900;
    $req_data->{ua}->timeout(10);
    
    while (1) {

        my $SERVER = "/$o->{tenant_id}/services/systemcenter/vmm/Jobs(StampId=guid'$o->{stamp_id}',ID=guid'$obj->{MostRecentTaskId}')";
        my $task = rr($req_data, GET => $SERVER);
        my $decoded_task = undef;
        eval {
            $decoded_task = decode_json($task->decoded_content());
            1;
        } or do {
            print "Error occured during decoding task: $@\n";
        };
        if ($decoded_task && $decoded_task->{ProgressValue} == 100) {
            print "Task completed!: $decoded_task->{StatusString}\n";
            last;
        }
        elsif ($decoded_task->{ProgressValue} ne 100) {
            print "Progress: $decoded_task->{ProgressValue}\n";
        }
        if (time() > $end) {
            print "Timed out. Exiting";
            exit 1;
        }
        sleep 10;
    }
    print "VM: $obj->{ID} was successfully created!\n";
    my $ut = "/$o->{tenant_id}/services/systemcenter/vmm/VirtualMachines(StampId=guid'$o->{stamp_id}',ID=guid'$obj->{ID}')";
    my $ut2 = $ut;
    $ut .= '?$expand=VirtualNetworkAdapters,VirtualDVDDrives,VirtualDiskDrives,VirtualHardDisks';

    my $created_vm = rr($req_data, GET => $ut);
    $created_vm = decode_json($created_vm->decoded_content());

    my $ips = $created_vm->{VirtualNetworkAdapters}->[0]->{IPv4Addresses};
    print "IPS: ", Dumper $ips;
    print "Power On";
    my $poweron_json = encode_json({Operation=>'Start'});
    print Dumper rr($req_data, PATCH => $ut2, $poweron_json);
    print "Done!\n";
    print "Creating resource!\n";
    my $res_name = 'WAP_' . $obj->{ID};
    my $cmdrresult = $ec->createResource(
        $res_name,
        {
            description   => q{Provisioned resource (dynamic) for } . $obj->{ID},
            hostName      => $ips->[0],
            port          => 7800
        }
    );
    print "Deployment finished\n";
}

sub rr {
    my ($req_data, $meth, $url, $content) = @_;

    my $request_url = $req_data->{base_url} . '/' . $url;
    my $req = HTTP::Request->new($meth => $request_url);
    $req->header('Accept' => 'application/json');
    $req->header('Content-Type' => 'application/json');

    if ($content) {
        $req->content($content);
    }

    my $ua = $req_data->{ua};
    return $ua->request($req);
}


sub get_ua {
    my ($cert, $privkey) = @_;

    my $cert_path = "$ENV{COMMANDER_WORKSPACE}/public.cert";
    my $key_path =  "$ENV{COMMANDER_WORKSPACE}/key.pem";


    $ENV{HTTPS_CERT_FILE} = "$ENV{COMMANDER_WORKSPACE}/public.cert";
    $ENV{HTTPS_KEY_FILE}  = "$ENV{COMMANDER_WORKSPACE}/key.pem";
    open (my $certfile, '>', $cert_path);
    open (my $privkeyfile, '>',$key_path );

    print $certfile $cert;
    print $privkeyfile $privkey;

    close $certfile;
    close $privkeyfile;
    my $ua = LWP::UserAgent->new(
        ssl_opts => {
            verify_hostname => 0,
            SSL_use_cert => 1,
            SSL_cert_file   => $cert_path,
            SSL_key_file    => $key_path,
        },
    );

    return $ua;
}




__DATA__
{
  "odata.type": "VMM.VirtualMachine",
  "AddedTime": null,
  "Agent": null,
  "AllocatedGPU": null,
  "BackupEnabled": null,
  "BlockDynamicOptimization": null,
  "BlockLiveMigrationIfHostBusy": null,
  "CanVMConnect": null,
  "CapabilityProfile": null,
  "CheckpointLocation": null,
  "CloudId": "###cloud_id###",
  "CloudVMRoleName": null,
  "CostCenter": null,
  "CPUCount": null,
  "CPULimitForMigration": null,
  "CPULimitFunctionality": null,
  "CPUMax": null,
  "CPURelativeWeight": null,
  "CPUReserve": null,
  "CPUType": null,
  "CPUUtilization": null,
  "CreationSource": null,
  "CreationTime": null,
  "DataExchangeEnabled": null,
  "DelayStart": null,
    "DelayStartSeconds": null,
  "DeployPath": null,
  "Description": null,
  "DiskIO": null,
  "Dismiss": null,
  "Domain": null,
  "DynamicMemoryBufferPercentage": null,
  "DynamicMemoryDemandMB": null,
  "DynamicMemoryEnabled": null,
  "DynamicMemoryMaximumMB": null,
  "Enabled": null,
  "ExcludeFromPRO": null,
  "ExpectedCPUUtilization": null,
  "FullName": null,
  "Generation": null,
  "GrantedToList@odata.type": "Collection(VMM.UserAndRole)",
  "GrantedToList": [],
  "HasPassthroughDisk": null,
  "HasSavedState": null,
  "HasVMAdditions": null,
  "HeartbeatEnabled": null,
  "HighlyAvailable": null,

  "ID": "00000000-0000-0000-0000-000000000000",
  "IsFaultTolerant": null,
  "IsHighlyAvailable": null,
  "IsRecoveryVM": null,
  "IsUndergoingLiveMigration": null,

  "LibraryGroup": null,
  "LimitCPUForMigration": null,
  "LimitCPUFunctionality": null,
  "LinuxAdministratorSSHKey": null,
  "LinuxAdministratorSSHKeyString": null,
  "LinuxDomainName": null,
  "LocalAdminPassword": "###local_admin_password###",
  "LocalAdminRunAsAccountName": null,
  "LocalAdminUserName": "###local_admin_username###",
  "Location": null,
  "MarkedAsTemplate": null,
  "Memory": null,
  "MemoryAssignedMB": null,
  "MemoryAvailablePercentage": null,
  "MemoryWeight": null,
  "ModifiedTime": null,

  "Name": "###vm_name###",
  "NetworkUtilization": null,
  "NewVirtualNetworkAdapterInput@odata.type": "Collection(VMM.NewVMVirtualNetworkAdapterInput)",
    "NewVirtualNetworkAdapterInput": [
        {"VMNetworkName": "###vm_network_name###"}
    ],
  "NumLock": null,
  "OperatingSystem": null,
  "OperatingSystemInstance": {
    "odata.type": "VMM.OperatingSystem",
    "Architecture": null,
    "Description": null,
    "Edition": null,
    "Name": null,
    "OSType": null,
    "ProductType": null,
    "Version": null
  },
  "OperatingSystemShutdownEnabled": null,
  "Operation": null,
  "OrganizationName": null,
  "Owner": {
    "odata.type": "###owner_email###"

  },
  "Password": "###admin_password###",
  "Path": null,
  "PerfCPUUtilization": null,
  "PerfDiskBytesRead": null,
  "PerfDiskBytesWrite": null,
  "PerfNetworkBytesRead": null,
  "PerfNetworkBytesWrite": null,
  "ProductKey": null,
  "Retry": null,
  "RunAsAccountUserName": null,
  "RunGuestAccount": null,
  "ServiceDeploymentErrorMessage": null,

  "SharePath": null,
  "SourceObjectType": null,
  "StampId": "###stamp_id###",
  "StartAction": null,
  "StartVM": null,
  "Status": null,
  "StatusString": null,
  "StopAction": null,
  "Tag": null,
  "TimeSynchronizationEnabled": null,
  "TimeZone": null,
  "TotalSize": null,
  "Undo": null,
  "UndoDisksEnabled": null,
  "UpgradeDomain": null,
  "UseCluster": null,
  "UseLAN": null,
  "UserName": null,

  "VirtualizationPlatform": null,
  "VirtualMachineState": null,
  "VMConfigResource": null,
  "VMCPath": null,
  "VMHostName": null,
  "VMNetworkAssignments@odata.type": "Collection(VMM.VMNetworkAssignment)",
  "VMNetworkAssignments": [],
  "VMResource": null,
  "VMResourceGroup": null,
  "VMTemplateId": "###vm_template_id###",
  "WorkGroup": null
}
