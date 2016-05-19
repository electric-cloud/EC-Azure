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
    String storageAccount = '$[storage_account]'.trim()
    String storageContainer = '$[storage_container]'.trim()
    String serverName = '$[vm_name]'.trim()
    String resourceGroupName = '$[resource_group_name]'.trim()
    String config = '$[connection_config]'.trim()
    String location = '$[location]'.trim()
    String imageURN = '$[image]'.trim()
    String userImage = '$[is_user_image]'.trim()
    String createPublicIP = '$[create_public_ip]'.trim()
    String osType = '$[os_type]'.trim();
    String disablePasswordPrompt = '$[disable_password_auth]'.trim()
    String publicKey = '$[public_key]'.trim()
    String vnet = '$[vnet]'.trim()
    String subnet = '$[subnet]'.trim()
    //Commander Resource 
    String resourcePool = '$[resource_pool]'.trim()
    String resourcePort = '$[resource_port]'.trim()
    String resourceWorkspace = '$[resource_workspace]'.trim()
    String resourceZone = '$[resource_zone]'.trim()
    int instances = '$[instance_count]'.trim().toInteger()
    boolean publicIP = false
    boolean isUserImage = false
    boolean disablePasswordAuth = false
    def VMList = []
    
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

    ElectricCommander ec = new ElectricCommander(config)
    //TODO: This will be changed when multiple credential issue is resolved in dynamic environment
    String vmCreds = ec.configProperties.vm_credential

    //TODO: validate parameters before creating the VM
    // Need to validate resource workspace and resource zone
    // only if the resource pool was specified
    if (resourcePool) {
        if(ec.createCommanderResourcePool(resourcePool))
        {
            //Create workspace if not present.
            if(resourceWorkspace)
                ec.createCommanderWorkspace(resourceWorkspace)
            //Check if the zone is present.    
            if(resourceZone)
                if(!ec.getZone(resourceZone)) 
                {
                    println("Zone "+ resourceZone +" not present")  
                    System.exit(1)
                }    
        }
    }

    def count = 1
    instances.times{

        String instanceSuffix = "${count}-${System.currentTimeMillis()}"
        String VMName 

        if(instances > 1)
            VMName = "${serverName}-${instanceSuffix}"
        else 
            VMName = serverName

        def (adminName, adminPassword) = ec.getFullCredentials(vmCreds) 
        def (resourceIP, VMStatus) = ec.azure.createVM(VMName, isUserImage, imageURN, storageAccount, storageContainer, location, resourceGroupName, publicIP, adminName, adminPassword, osType, publicKey, disablePasswordAuth, vnet, subnet)
        
        if(VMStatus == ProvisioningStateTypes.SUCCEEDED)
            VMList.push(VMName)

        //VM is created if Public IP is fetched successfully.
        //EF Resources are generated only if the VM is created without errors.
        if (VMStatus == ProvisioningStateTypes.SUCCEEDED) {

            println("Created VM " + VMName + " successfully.")
                
            if(resourcePool)
            {
                println("Public IP assigned to the VM: " + resourceIP)

                String resourceName = "${resourcePool}-${instanceSuffix}"
                def resourceCreated = ec.createCommanderResource(resourceName, resourceWorkspace, resourceIP, resourcePort, resourceZone)
                if (resourceCreated) {

                    // Add resource to pool through a separate call
                    // This is to work-around the issue that createResource API does
                    // not support resource pool name with spaces.
                    def added = ec.addResourceToPool(resourceName, resourcePool)
                    
                    if (added) {
                        println("Created commander resource: $resourceName in $resourcePool")
                        ec.setPropertyInResource(resourceName, 'created_by', 'EC-Azure')
                        ec.setPropertyInResource(resourceName, 'instance_id', VMName)
                        ec.setPropertyInResource(resourceName, 'config', config)
                        ec.setPropertyInResource(resourceName, 'etc/public_ip', resourceIP)
                        ec.setPropertyInResource(resourceName, 'etc/storage_account', storageAccount)
                        ec.setPropertyInResource(resourceName, 'etc/resource_group_name', resourceGroupName)
                    } else {
                        //rollback - delete all Azure VMs and EF resources created so far.
                        println("Could not add resource to resource pool, going for the rollback operation.")
                        ec.rollback(resourcePool)
                    }
                } else {
                    //rollback - delete all Azure VMs and EF resources created so far.
                    println("Could not create commander resource, going for the rollback operation.")
                    ec.rollback(resourcePool)
                }
            }
        }
        else
        {
            println("Failed to create the VM " + VMName)
            def listSize = VMList.size()
            listSize.times{
                ec.azure.deleteVM(resourceGroupName, VMList.pop())
            }
        }  
        count = count + 1  
    }
}catch(Exception e){
    e.printStackTrace()
    return
}
