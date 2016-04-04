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
	    String config = '$[connection_config]'.trim()
	    String networkSecurityGroup = '$[network_security_group]'.trim()
	    String resourceGroup = '$[resource_group]'.trim()

	    ElectricCommander ec = new ElectricCommander(config)
        ec.azure.deleteNetworkSecurityGroup(resourceGroup, networkSecurityGroup)
    
}catch(Exception e){
    e.printStackTrace();
    return
}



	    
