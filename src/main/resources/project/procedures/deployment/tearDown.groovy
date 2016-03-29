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
    //This can be a resource or resource pool
	String resourceName = '$[resource_name]'.trim()

    def resourceInfo = ec.getResourceProperties(resourceName)
    if(resourceInfo == null)
    {
        println("Could not fetch properties for resource: " + resourceName)
        System.exit(1)
    }

    def (resourcePropertyMap, isResourcePool) = resourceInfo
    resourcePropertyMap.each { resource, property->
        if(!ec.setConfigurations(property["config"]))
        {
            println("Could not set configurations for azure")
            System.exit(1)
        }
        if(!ec.initializeAzure())
        {
            println("Could not initialize azure")
            System.exit(1)
        }

        println("Going for deleting resource: " + resource + " with instance Id: " + property["instance_id"] + " and resource group name: " + property["resource_group_name"])
        if(property["resource_group_name"] && property["instance_id"])
        {
            ec.azure.deleteVM(property["resource_group_name"],  property["instance_id"])
            ec.deleteCommanderResource(resource)
        }

    }

    if(isResourcePool)
    {
        ec.deleteCommanderResourcePool(resourceName)
    }
}catch(Exception e){
    e.printStackTrace();
    return
}
