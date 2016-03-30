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
    String accountCreds = '$[account_creds]'.trim()
    String operation = '$[operation]'.trim()
    String tableName = '$[table]'.trim()
    //Only for INSERT, UPDATE
    String toBeSet = '$[to_be_set]'.trim()
    //Only for RETRIEVE(SELECT)
    String toBeRetrieved = '$[to_be_retrieved]'.trim()
    //Only for UPDATE, RETRIEVE, DELETE
    String whereClause = '$[where_clause]'.trim()
    String partitionKey = '$[partition_key]'.trim()    
    ElectricCommander ec = new ElectricCommander()
    def (accountName, accountKey) = ec.getFullCredentials(accountCreds)

    if (accountName && accountKey && storageAccount) {
        def db = new NoSQLOperations(accountName, accountKey, storageAccount, ec)
        if (!tableName)
        {
            println("Table name can't be empty")
            ec.setProperty("summary", "Table name can't be empty", true)
            System.exit(1)
        }
        switch (operation) {

            case "createtable":
                db.createTable(tableName)
                break

            case "insert":
                if (!toBeSet)
                {
                    println("ToBeInserted can't be empty for insert operation")
                    System.exit(1)
                }
                if (!partitionKey)
                {
                    //If no partition key provided, taking table name as partition key
                    partitionKey = tableName
                }
                db.insert(tableName, toBeSet, partitionKey)
                break           

            case "retrieve":
                //If toBeRetrieved is not given * is default
                db.retrieve(tableName, toBeRetrieved, whereClause)
                break           

            case "update":
                if (!toBeSet)
                {
                    println("ToBeUpdated can't be empty for update operation")
                    System.exit(1)
                }
                db.update(tableName, toBeSet, whereClause)
                break

            case "delete":
                db.delete(tableName, whereClause)
                break

            case "deletetable":
                db.deleteTable(tableName)
                break

            //No need of default as we are giving dropdown with default option
        }    
    }

}catch(Exception e){
    e.printStackTrace();
    return
}
