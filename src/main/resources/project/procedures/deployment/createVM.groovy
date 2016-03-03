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
    boolean publicIP = false
    boolean isUserImage = false
    if (createPublicIP == '1')
    {
        publicIP = true
    }
    if (userImage == '1')
    {
        isUserImage = true
    }
    def (adminName, adminPassword)= ec.getFullCredentials(vmCreds)
    ec.azure.createVM(serverName, isUserImage, imageURN, storageAccount, storageContainer, location, resourceGroupName, publicIP, adminName, adminPassword, osType)
}catch(Exception e){
    e.printStackTrace();
    return
}
