/*
*
* Copyright 2015 Electric Cloud, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

$[/myProject/procedure_helpers/preamble]

try {
    ElectricCommander ec = new ElectricCommander()
    String storageAccount = '$[storage_account]'.trim()
    String storageContainer = '$[storage_container]'.trim()
    String serverName = '$[server_name]'.trim()
    String resourceGroupName = '$[resource_group_name]'.trim()
    String config = '$[connection_config]'.trim()
    String location = '$[location]'.trim()
    String imageURN = '$[image]'.trim()
    String userImage = '$[is_user_image]'.trim()
    String vmCreds = ec.configProperties.vm_credential
    String createPublicIP = '$[create_public_ip]'.trim()
    String osType = '$[os_type]'.trim()
    String disablePasswordPrompt = '$[disable_password_auth]'.trim()
    String publicKey = '$[public_key]'.trim()
    //Commander Resource 
    String resourcePool = '$[resource_pool]'.trim()
    String resourcePort = '$[resource_port]'.trim()
    String resourceWorkspace = '$[resource_workspace]'.trim()
    String resourceZone = '$[resource_zone]'.trim()
    boolean publicIP = false
    boolean isUserImage = false
    boolean disablePasswordAuth = false
    if (createPublicIP == '1')
    {
        publicIP = true
    }
    if (userImage == '1')
    {
        isUserImage = true
    }
    if (disablePasswordPrompt == '1')
    {
        disablePasswordAuth = true
    }
    //TODO: validate parameters before creating the VM
    // Need to validate resource workspace and resource zone
    // only if the resource pool was specified
    if (resourcePool) {
        //1. resourceWorkspace should exist if specified
        //2. resourceZone should exist if specified
    }

    def (adminName, adminPassword)= ec.getFullCredentials(vmCreds)
    ec.azure.createVM(serverName, isUserImage, imageURN, storageAccount, storageContainer, location, resourceGroupName, publicIP, adminName, adminPassword, osType, publicKey, disablePasswordAuth)

    //TODO: Confirm that the VM was created before creating the EF resource

    if (resourcePool) {
        //TODO: Get IP from created VM and pass it here
        // TODO: Passing a temporary IP here
        String resourceIP = "104.41.151.132"
        // TODO: This should be a running counter
        int count = 1

        String resourceName = "${resourcePool}_${count}_${System.currentTimeMillis()}"

        def resourceCreated = ec.createCommanderResource(resourceName, resourceWorkspace, resourceIP, resourcePort)
        if (resourceCreated) {

            // Add resource to pool through a separate call
            // This is to work-around the issue that createResource API does
            // not support resource pool name with spaces.
            def added = ec.addResourceToPool(resourceName, resourcePool)
            if (added) {
                println("Created commander resource: $resourceName in $resourcePool")
            } else {
                //TODO: rollback - delete all Azure VMs and EF resources created so far.
            }
        } else {
            //TODO: rollback - delete all Azure VMs and EF resources created so far.
        }
    }

}catch(Exception e){
    e.printStackTrace();
    return
}
