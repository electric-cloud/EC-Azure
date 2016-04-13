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

$opts->{wap_url}                  = q{$[wap_url]};
$opts->{tenant_id}                = q{$[tenant_id]};
$opts->{cloudservice_name}        = q{$[cloudservice_name]};

$opts->{vm_role_name}             = q{$[vm_role_name]};
$opts->{vm_role_size}             = q{$[vm_role_size]};
$opts->{vm_role_network}          = q{$[vm_role_network]};
$opts->{vm_role_hdi}              = q{$[vm_role_hdi]};
$opts->{vm_role_allocation_meth}  = q{$[vm_role_allocation_meth]};
$opts->{vm_role_hostname_pattern} = q{$[vm_role_hostname_pattern]};
$opts->{vm_role_domain}           = q{$[vm_role_domain]};
$opts->{vm_role_timezone}         = q{$[vm_role_timezone]};

$opts->{vm_role_label}            = q{$[vm_role_label]};
$opts->{vm_role_vm_name}          = q{$[vm_role_vm_name]};
$opts->{wap_public_key}           = q{$[wap_public_key]};
$opts->{wap_private_key}          = q{$[wap_private_key]};

main($opts);


sub main {
    my ($o) = @_;

    my $ec = ElectricCommander->new();
    my $ua = get_ua($o->{wap_public_key}, $o->{wap_private_key});

    my $resp;
    my $content;
    $o->{wap_url} =~ s|\/+$||gs;
    my $req_data = {
        base_url => $o->{wap_url},
        ua => $ua,
    };
    $resp = rr(
        $req_data,
        GET => "$o->{tenant_id}/Gallery/GalleryItems/\$/MicrosoftCompute.VMRoleGalleryItem?api-version=2013-03"
    );
    if ($resp->code() > 399) {
        print Dumper $resp;
        exit 1;
    }
    $content = decode_json($resp->decoded_content());
    my $reference;
    for my $role (@{$content->{value}}) {
        if ($role->{Name} ne $o->{vm_role_name}) {
            next;
        }
        print "Role found!\n";
        $reference = $role->{ResourceDefinitionUrl}
    }
    if (!$reference) {
        print "Role $o->{vm_role_name} wasn't found\n";
    }
    print "ResourceDefinitionURL: $reference\n";

    # rd = Resource Definition
    my $rd;
    $resp = rr(
        $req_data,
        GET => "/$o->{tenant_id}/$reference?api-version=2013-03"
    );
    if ($resp->code() > 399) {
        print "Error occured:", Dumper $resp;
        exit 1;
    }

    $rd = decode_json($resp->decoded_content());

    my $request_body = {
        ResourceDefinition => $rd,
        Name => $o->{vm_role_label},
        Label => $o->{vm_role_vm_name},
        InstanceView => undef,
        ProvisioningState => undef,
        Substate => undef,
    };

    $request_body->{ResourceConfiguration} = {
        ParameterValues => encode_json({ # O_o
            VMRoleInitialInstanceCount => '1',
            VMRoleMinimumInstanceCount => '1',
            VMRoleMaximumInstanceCount => '1',
            VMRoleVMSize => $o->{vm_role_size},
            VMRoleOSVHDImageNameVersion => $o->{vm_role_hdi},
            VMRoleNetworkRef => $o->{vm_role_network},
            VMRoleNetworkIPAddressAllocationMethod => $o->{vm_role_allocation_meth},
            VMRoleComputerNamePattern => $o->{vm_role_hostname_pattern},
            VMRoleAdminCredential => 'root:pwd',
            VMRoleTimezone => $o->{vm_role_timezone},
            VMRoleDNSDomainName => $o->{vm_role_domain},
            VMRoleSSHPublicKey => 'NULL',
        }),
        Version => '1.0.0.0',
    };

    my $new_request_json = encode_json($request_body);
    print Dumper $request_body;
    my $request_url = "/$o->{tenant_id}/CloudServices/$o->{cloudservice_name}/Resources/MicrosoftCompute/VMRoles?api-version=2013-03";
    $resp = rr(
        $req_data,
        POST => $request_url,
        $new_request_json
    );
    # print Dumper $resp;
    $request_url = "/$o->{tenant_id}/CloudServices/gw1/Resources/MicrosoftCompute/VMRoles/$o->{vm_role_label}?api-version=2013-03";
    my $provisioned = 0;
    while (!$provisioned) {
        $resp = rr($req_data, GET => $request_url);
        my $j = decode_json($resp->decoded_content());
        if ($j->{ProvisioningState} eq 'Provisioned') {
            print "Provisioning done\n";
            $provisioned++;
            next;
        }
        print "Provisioning...\n";
        sleep 10;
    }
    print "Creating resource...\n";
    $request_url = "/$o->{tenant_id}/CloudServices/gw1/Resources/MicrosoftCompute/VMRoles/$o->{vm_role_label}/VMs?api-version=2013-03";
    $resp = rr($req_data, GET => $request_url);
    my $j = decode_json($resp->decoded_content());
    my @ips = ();
    for my $vm (@{$j->{value}}) {
        for my $addr (@{$vm->{ConnectToAddresses}}) {
            push @ips, $addr->{IPAddress};
        }
    }
    print "Got IPs:", Dumper \@ips;

    my $res_name = 'WAP_Role_' . $o->{vm_role_vm_name} . '_' . time();
    for my $ip (@ips) {
        my $cmdrresult = $ec->createResource(
            $res_name,
            {
                description   => q{Provisioned resource (dynamic) for } . $o->{vm_role_vm_name},
                hostName      => $ip,
                port          => 7800
            }
        );
    }
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

