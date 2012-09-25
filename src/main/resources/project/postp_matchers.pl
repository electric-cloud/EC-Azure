use ElectricCommander;

push(
    @::gMatchers,
    {
       id      => "Status",
       pattern => q{^HTTP\/1\.1\s([\d]+)\s(.*)},
       action  => q{
         
                              my $desc = ((defined $::gProperties{"summary"}) ? $::gProperties{"summary"} : '');

                              $desc .= "Status: \'$1 $2\'";
                              
                              setProperty("summary", $desc . "\n");
                             },
    },
    {
       id      => "failed",
       pattern => q{^Failed\sto\s(.+)\s'(.+)':},
       action  => q{
         
                              my $desc = ((defined $::gProperties{"summary"}) ? $::gProperties{"summary"} : '');

                              $desc .= "Failed to $1 \'$2\'";
                              
                              setProperty("summary", $desc . "\n");
                             },
    },
    {
       id      => "call_error",
       pattern => q{^Call\sto\sWindows\sAzure\sreturned\san\serror},
       action  => q{
         
                              my $desc = ((defined $::gProperties{"summary"}) ? $::gProperties{"summary"} : '');

                              $desc .= "Call to Windows Azure returned an error.";
                              
                              setProperty("summary", $desc . "\n");
                             },
    },

    {
       id      => "error_code",
       pattern => q{^Error\sCode:\s(.+)},
       action  => q{
         
                              my $desc = ((defined $::gProperties{"summary"}) ? $::gProperties{"summary"} : '');

                              $desc .= "Error Code: \'$1\'.";
                              
                              setProperty("summary", $desc . "\n");
                             },
    },

    {
       id      => "error_message",
       pattern => q{^Error\sMessage:\s(.+)},
       action  => q{
         
                              my $desc = ((defined $::gProperties{"summary"}) ? $::gProperties{"summary"} : '');

                              $desc .= "Error Message: \'$1\'.";
                              
                              setProperty("summary", $desc . "\n");
                             },
    },

    {
       id      => "exception",
       pattern => q{Exception\s.+:(.+)},
       action  => q{
         
                              my $desc = ((defined $::gProperties{"summary"}) ? $::gProperties{"summary"} : '');

                              $desc .= "Exception Caught: $1";
                              
                              setProperty("summary", $desc . "\n");
                             },
    },
    {
       id      => "win_only",
       pattern => q{This\splugin\sis\sWindows\sOnly},
       action  => q{
         
                              my $desc = ((defined $::gProperties{"summary"}) ? $::gProperties{"summary"} : '');

                              $desc .= "Error: This plugin is Windows Only!";
                              
                              setProperty("summary", $desc . "\n");
                             },
    },
);
