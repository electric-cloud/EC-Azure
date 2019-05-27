EC-Azure
============

The CloudBees Flow Azure integration

## Compile ##

Run gradlew to compile the plugin

`./gradlew`

## Tests ##

## Compile And Upload ##
0. Install git
   sudo apt-get install git
1. Get this plugin
   git clone https://github.com/electric-cloud/EC-Azure.git
2. Run gradlew to compile the plugin
   `./gradlew jar` (in EC-Azure directory)
3. Upload the plugin to EC server
4. Create a configuration for the EC-Azure plugin.

####Prerequisites:####
    1.An existing Azure account with the required credentials:
      [Authenticating Azure Resource Manager requests](https://azure.microsoft.com/en-us/documentation/articles/resource-group-create-service-principal-portal/)

####Required files:####
    1. Create a file called ecplugin.properties inside EC-Azure directory with the below mentioned contents.

####Contents of ecplugin.properties:####
    COMMANDER_SERVER=<COMMANDER_SERVER>(Commander server IP)
    COMMANDER_USER=<COMMANDER_USER>
    COMMANDER_PASSWORD=<COMMANDER_PASSWORD>

    CLIENT_ID=<AZURE_ACCOUNT_CLIENT_ID>
    TENANT_ID=<AZURE_ACCOUNT_TENANT_ID>
    CLIENT_SECRET=<AZURE_ACCOUNT_CLIENT_SECRET>
    SUBSCRIPTION_ID=<AZURE_ACCOUNT_SUBSCRIPTION_ID>

####Contents of Configurations.json:####
    1. Configurations.json is a configurable file.
    2. Refer to the sample Configurations.json file, `/src/test/java/ecplugins/azure/Configurations.json`. It has to be updated with the user specific, valid inputs.
   
####Run the tests:#####
`./gradlew test`

## Licensing ##
EC-Azure is licensed under the Apache License, Version 2.0. See [LICENSE](https://github.com/electric-cloud/EC-Azure/blob/master/LICENSE) for the full license text.

