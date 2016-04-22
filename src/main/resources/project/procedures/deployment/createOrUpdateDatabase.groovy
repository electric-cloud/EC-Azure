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
    String resourceGroupName = '$[resource_group_name]'.trim()
    String serverName = '$[server_name]'.trim()
    String databaseName = '$[database_name]'.trim()
    String location = '$[location]'.trim()
    String edition = '$[edition]'.trim()
    String collation = '$[collation]'.trim()
    String createMode = '$[create_mode]'.trim()
    String elasticPool = '$[elastic_pool]'.trim()
    String maxSizeMB = '$[max_size_mb]'.trim()
    String serviceObjectiveId = '$[service_objective_id]'.trim()
    String sourceDatabaseId = '$[source_database_id]'.trim()

    ElectricCommander ec = new ElectricCommander(config)
    ec.azure.createOrUpdateDatabase(resourceGroupName, serverName, databaseName, location, collation, edition, maxSizeMB, createMode, elasticPool, serviceObjectiveId, sourceDatabaseId)
}catch(Exception e){
    e.printStackTrace();
    return
}
