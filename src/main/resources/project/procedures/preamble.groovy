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
@Grapes([
    @Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1'),
    @Grab(group='com.microsoft.azure', module='azure-svc-mgmt', version='0.9.3'),
    @Grab(group='com.microsoft.azure', module='azure-svc-mgmt-storage', version='0.9.3'),
    @Grab(group='com.microsoft.azure', module='azure-mgmt-utility', version='0.9.3'),
    @Grab(group='com.microsoft.azure', module='azure-mgmt-compute', version='0.9.3'),
    @Grab(group='com.microsoft.azure', module='azure-mgmt-resources', version='0.9.3'),
    @Grab(group='com.microsoft.azure', module='azure-mgmt-storage', version='0.9.3'),
    @Grab(group='com.microsoft.azure', module='azure-mgmt-network', version='0.9.3'),
    @Grab(group='com.microsoft.azure', module='azure-mgmt-sql', version='0.9.3'),
    @Grab(group='com.microsoft.azure', module='azure-core', version='0.9.3'),
    @Grab(group='org.slf4j', module='slf4j-jdk14', version='1.7.16'),
    @Grab(group='com.microsoft.azure', module='adal4j', version='1.0.0'),
    @Grab(group='commons-logging', module='commons-logging', version='1.2'),
    @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.1'),
    @Grab(group='com.sun.jersey', module='jersey-core', version='1.13-b01'),
    @Grab(group='net.sourceforge.jtds', module = 'jtds', version = '1.3.1'),
    @Grab(group='com.microsoft.azure', module='azure-storage', version='4.0.0'),
    @Grab(group='net.sf.json-lib', module='json-lib', version='2.3', classifier ='jdk15')
])

import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import groovyx.net.http.RESTClient;
import static groovyx.net.http.ContentType.JSON
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.microsoft.windowsazure.Configuration
import com.microsoft.azure.utility.AuthHelper
import com.microsoft.azure.utility.ComputeHelper
import com.microsoft.azure.utility.NetworkHelper
import com.microsoft.azure.utility.StorageHelper
import com.microsoft.azure.utility.ResourceContext
import com.microsoft.azure.management.compute.models.VirtualMachine
import com.microsoft.azure.management.compute.ComputeManagementService
import com.microsoft.azure.management.storage.StorageManagementService
import com.microsoft.azure.management.network.NetworkResourceProviderService
import com.microsoft.azure.management.resources.ResourceManagementService
import com.microsoft.azure.management.compute.models.ImageReference
import com.microsoft.azure.utility.ConsumerWrapper
import com.microsoft.windowsazure.core.OperationStatus
import com.microsoft.azure.management.compute.models.OSDisk
import com.microsoft.azure.management.compute.models.StorageProfile
import com.microsoft.azure.management.compute.models.LinuxConfiguration;
import com.microsoft.azure.management.compute.models.OSProfile;
import com.microsoft.azure.management.compute.models.SshConfiguration;
import com.microsoft.azure.management.compute.models.SshPublicKey;
import com.microsoft.azure.management.compute.models.CachingTypes
import com.microsoft.azure.management.compute.models.VirtualHardDisk
import com.microsoft.azure.management.storage.models.StorageAccount
import com.microsoft.azure.management.compute.models.DeleteOperationResponse
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration
import com.microsoft.azure.management.compute.models.ProvisioningStateTypes
import com.microsoft.azure.management.sql.models.ServerGetResponse
import com.microsoft.azure.management.sql.models.ServerCreateOrUpdateParameters
import com.microsoft.azure.management.sql.models.ServerCreateOrUpdateProperties
import com.microsoft.azure.management.sql.models.DatabaseCreateOrUpdateResponse
import com.microsoft.azure.management.sql.models.DatabaseCreateOrUpdateParameters
import com.microsoft.azure.management.sql.models.DatabaseCreateOrUpdateProperties
import com.microsoft.azure.management.sql.SqlManagementService;
import com.microsoft.azure.management.compute.models.ComputeLongRunningOperationResponse
import com.microsoft.azure.management.compute.models.ComputeOperationStatus
import com.microsoft.azure.management.network.models.VirtualNetwork
import com.microsoft.azure.management.network.models.AddressSpace
import com.microsoft.azure.management.network.models.Subnet
import com.microsoft.azure.management.network.models.DhcpOptions
import com.microsoft.azure.management.network.models.AzureAsyncOperationResponse

//Import for SQL
import groovy.sql.Sql
//Import for NoSQL Table
import java.util.UUID
import groovy.json.JsonSlurper
import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.table.*;
import com.microsoft.azure.storage.table.TableQuery.*;

//Imports
import com.microsoft.azure.management.network.models.SecurityRule
import com.microsoft.windowsazure.core.OperationResponse
import com.microsoft.azure.management.network.models.NetworkInterfaceIpConfiguration
import com.microsoft.azure.management.network.models.ResourceId
import com.microsoft.azure.management.network.models.NetworkSecurityGroup
import com.microsoft.azure.management.network.models.Subnet
enum RequestMethod {
    GET, POST, PUT, DELETE
}

public class ElectricCommander {
    def commanderServer = 'https://' + System.getenv('COMMANDER_SERVER')
    def commanderPort = System.getenv("COMMANDER_HTTPS_PORT")

    def sessionId = System.getenv('COMMANDER_SESSIONID')

    def client = new RESTClient(commanderServer + ":" + commanderPort);

    def sysJobId = System.getenv('COMMANDER_JOBID')
    def sysJobStepId = System.getenv('COMMANDER_JOBSTEPID')

    def azure
    def jobStepId = '$[/myJobStep/jobStepId]'

    def configProperties

    ElectricCommander(def config = "") {

        client.ignoreSSLIssues()
        if(config)
        {
            setConfigurations(config)
            initializeAzure()
        }
    }

    def initializeAzure() {
        exceptionHandler{
            azure = new Azure([tenantID : configProperties.tenant_id,
                               subscriptionID : configProperties.subscription_id,
                               clientID : configProperties.client_id,
                               clientSecret : configProperties.client_secret,
                               exceptionHandler: this.&exceptionHandler])
            azure.createManagementClient()
        }
    }

    def setConfigurations(def config) {
        exceptionHandler{
            def resp
            resp = PerformHTTPRequest(RequestMethod.GET, '/rest/v1.0/jobsSteps/' + jobStepId + '/credentials/' + config, [])
            if( resp == null ) {
                throw new Exception("Error : Invalid configuration " + config);
            }
            if(resp.status != 200) {
                throw new Exception("Commander did not respond with 200 for credentials")
            }
            configProperties = getProperties('/myProject/azure_cfgs/' + config)
            configProperties.client_id = resp.data.credential.userName
            configProperties.client_secret = resp.data.credential.password
            return true
        }
    }

    public setProperty(String propName, String propValue, boolean step = false) {
        def jsonData
        if(step)
        {
            jsonData = [propertyName : propName, value : propValue, jobStepId : System.getenv('COMMANDER_JOBSTEPID')]
        }
        else
        {
            jsonData = [propertyName : propName, value : propValue, jobId : System.getenv('COMMANDER_JOBID')]
        }

        def resp = PerformHTTPRequest(RequestMethod.POST, '/rest/v1.0/properties', jsonData)
        if(resp == null ) {
          println("Could not set property on the Commander. Request failed")
        }

    }

    public setPropertyInResource(String resource, String name, String value)
    {
        println("Going for setting property: " + name + " with value: " + value + " in resource " + resource)
        def jsonData = [propertyName : "ec_cloud_instance_details/" + name , propertyType : "string", resourceName : resource, value : value]
        def resp = PerformHTTPRequest(RequestMethod.POST, '/rest/v1.0/properties', jsonData)
        if(resp == null ) {
          println("Could not create property on the Commander. Request failed")
        }
    }

    public getFullCredentials( String parameterName ) {
        client.ignoreSSLIssues()

        def resp
        resp = PerformHTTPRequest(RequestMethod.GET, '/rest/v1.0/jobsSteps/' + jobStepId + '/credentials/' + parameterName, [])
        if( resp == null ) {
            throw new Exception("Error : Invalid configuration " + parameterName);
        }
        if(resp.status != 200) {
            throw new Exception("Commander did not respond with 200 for credentials")
        }
        return [resp.getData().credential.userName, resp.getData().credential.password]
    }

    public getCommanderProperty(String propName) {

        sysJobStepId = System.getenv('COMMANDER_JOBSTEPID')
        def url = '/rest/v1.0/properties/' + propName
        def query =  ['jobStepId': "" + sysJobStepId]
        def resp = PerformHTTPRequest(RequestMethod.GET, url, query, [])

        if(resp == null ) {
            println("Could not get property " + propName + " on the Commander. Request failed")
            return
        }

        if(resp.status != 200) {
            println("Commander did not respond with 200 for retrieving property")
            return
        }

        return resp.getData().property.value
    }

    def getResourceProperties(String resourceOrPool) {
        def resourcePropertyMap = [:]
        //First check for resource named resource
        def resources = []
        boolean isResourcePool = false
        def url = '/rest/v1.0/resources/' + resourceOrPool
        def resp = PerformHTTPRequest(RequestMethod.GET, url, [])
        if(resp != null && resp.status == 200) {
            println("Resource " + resourceOrPool + " exists")
            resources.push(resourceOrPool)
        }
        else
        {
            url = '/rest/v1.0/resourcePools/' + resourceOrPool
            resp = PerformHTTPRequest(RequestMethod.GET, url, [])
            if(resp != null && resp.status == 200){
                println("Resource Pool " + resourceOrPool + " exists")
                resources = resp?.data?.resourcePool?.resourceNames?.resourceName
                isResourcePool = true
            }
            else
            {
                println("Could not get property of " + resourceOrPool + " on commander. Request failed.")
                return
            }
        }

        resources.each {resource->
            println("Fetching properties for " + resource)
            url = '/rest/v1.0/properties'
            def query =  ['request' : 'findProperties', 'resourceName' : resource]
            resp = PerformHTTPRequest(RequestMethod.GET, url, query, [])

            if(resp == null ) {
                println("Could not get resource " + resource + " on Commander. Request failed")
                return
            }

            if(resp.status != 200) {
                println("Could not get success response for resource: " + resource + ". Request failed.")
                return
            }

            def properties = [:]
            //Move inside nested property sheet
            def propertyHolder = resp?.data?.object?.property
            if(propertyHolder == null)
            {
                println("Could not get properties for resource: " + resource + ". Request failed.")
                return
            }
            propertyHolder.each {

                while (propertyHolder?.propertySheetId[propertyHolder.indexOf(it)])
                {
                    def sheetId = propertyHolder?.propertySheetId[propertyHolder.indexOf(it)]
                    String sheetUrl = '/rest/v1.0/propertySheets/' + sheetId
                    println("URL: " + sheetUrl)
                    resp = PerformHTTPRequest(RequestMethod.GET, sheetUrl, [])
                    propertyHolder = resp?.data?.propertySheet?.property
                    //This will get all properties and sheets will be ignored
                    propertyHolder.each{
                        if(propertyHolder?.propertySheetId[propertyHolder.indexOf(it)] == null)
                        {
                            properties[propertyHolder.propertyName[propertyHolder.indexOf(it)]] = propertyHolder.value[propertyHolder.indexOf(it)]
                        }
                    }
                    //Move in a propertysheet(Limitation: Only last nested propertysheet will be traversed)
                    propertyHolder.each { insideIt->
                        if(propertyHolder?.propertySheetId[propertyHolder.indexOf(insideIt)])
                        {
                            it = insideIt
                        }
                    }
                }
            }
            resourcePropertyMap[resource] = properties
            println("ResourceName: " + resource + ", Properties: " + JsonOutput.toJson(properties))
        }
        return [resourcePropertyMap, isResourcePool]
    }
    def isPropertyExistsAndNotEmpty(String path) {
        def uri = '/rest/v1.0/properties/' + path;
        def query =[:]
        def sysJobStepId = System.getenv('COMMANDER_JOBSTEPID')
        println("SysJobStepId: " + sysJobStepId)

        query.jobStepId = sysJobStepId
        def resp = PerformHTTPRequest(RequestMethod.GET, uri, query, []);
        def propval = resp.data.property.value;
        if (resp?.status != 200 || !propval) {
            return false;
        }
        return true;
    }
    def getProperties(String path) {

        println(commanderServer + ":" + commanderPort)
        def query =[:]
        def uri = '/rest/v1.0/properties/' + path

        def sysJobStepId = System.getenv('COMMANDER_JOBSTEPID')
        println("SysJobStepId: " + sysJobStepId)

        query.jobStepId = sysJobStepId

        def resp = performHttpGet(uri, query)

        def properties = [:]
        //Move inside nested property sheet

        if (resp?.data?.property?.propertySheetId) {

            def sheetId = resp?.data?.property?.propertySheetId
            uri = '/rest/v1.0/propertySheets/' + sheetId
            resp = performHttpGet(uri)
        }

        resp?.data?.propertySheet?.property.each{
            properties[it.propertyName] = it.value
        }

        println("properties: " + JsonOutput.toJson(properties))

        properties
    }

    def performHttpGet(String uri, def query = [:]) {

        println("URL: " + uri)
        println("Query: " + JsonOutput.toJson(query))

        def resp = PerformHTTPRequest(RequestMethod.GET, uri, query, [])
        println('Response status: ' + resp?.status)
        if(resp?.status != 200) {
            println("ERROR: HTTP GET request failed $uri")
            return [:]
        }
        println(JsonOutput.toJson(resp.data))
        return resp
    }

    public createCommanderWorkspace(String workspaceName){

        println("Creating workspace.")
        def jsonData = [workspaceName : workspaceName, description : workspaceName, agentDrivePath : "C:/Program Files/Electric Cloud/ElectricCommander" , agentUncPath:"C:/Program Files/Electric Cloud/ElectricCommander", agentUnixPath: "/opt/electriccloud/electriccommander", local: true ]
        def resp = PerformHTTPRequest(RequestMethod.POST, '/rest/v1.0/workspaces/',jsonData)

        if(resp?.status == 409)
        {
            println("Workspace " + workspaceName +" already exists.")
            return true
        }
        else if(resp?.status >= 400)
        {
            println("Failed to create the workspace " + resp)
            return false
        }
        else
        {
            println("Workspace " + workspaceName + " created.")
            return true
        }

    }

    public getZone(String zoneName){

        def resp = PerformHTTPRequest(RequestMethod.GET, '/rest/v1.0/zones/'+ zoneName,[])

        if(resp?.status >= 400)
        {
            return false
        }
        else
        {
            println("Zone " + zoneName + " exists.")
            return true
        }
    }

    public createCommanderResourcePool(String resourcePoolName){

        println("Creating Resource Pool")
        def jsonData = [autoDelete : true, description : resourcePoolName , resourcePoolDisabled: false ]
        def resp = PerformHTTPRequest(RequestMethod.POST, '/rest/v1.0/resourcePools', [resourcePoolName: resourcePoolName], jsonData)

        if(resp?.status == 409)
        {
            println("Resource Pool " + resourcePoolName +" already exists.")
            return true
        }
        else if(resp?.status >= 400)
        {
            println("Failed to create the Resource Pool " + resp)
            return false
        }
        else
        {
            println("Resource Pool " + resourcePoolName + " created.")
            return true
        }
    }

    public boolean createCommanderResource(String resourceName, String workspaceName, String resourceIP ,String resourcePort, String zoneName) {

        println("Creating Resource")
        def jsonData = [resourceName : resourceName, description : resourceName , hostName: resourceIP ]
        if (resourcePort) {
            jsonData.port = resourcePort
        }
        if (workspaceName) {
            jsonData.workspaceName = workspaceName
        }
        if (zoneName) {
            jsonData.zoneName = zoneName
        }

        def resp = PerformHTTPRequest(RequestMethod.POST, '/rest/v1.0/resources', [resourceName: resourceName], jsonData)

        if(resp?.status == 409)
        {
            println("Resource " + resourceName +" already exists.")
            //TODO:Adjust resource name
            return false
        }
        else if(resp?.status >= 400)
        {
            println("Failed to create the Resource " + resp)
            return false
        }
        else
        {
            println("Resource " + resourceName + " created.")
            return true
        }
    }

    public boolean addResourceToPool(String resourceName, String resourcePool) {

        println("Adding Resource $resourceName to Pool $resourcePool")

        def jsonData = [resourceName : resourceName, description : resourceName , resourcePoolName: resourcePool ]

        def resp = PerformHTTPRequest(RequestMethod.PUT, "/rest/v1.0/resourcePools/$resourcePool/resources", jsonData)

        if(resp?.status >= 400)
        {
            println("Failed to add Resource $resourceName to Pool $resourcePool")
            return false
        }
        return true
    }

    public deleteCommanderResource(String resourceName) {

        println("Deleting Resource")
        def resp = PerformHTTPRequest(RequestMethod.DELETE, '/rest/v1.0/resources/' + resourceName,[])

        if(resp?.status >= 400)
        {
            println("Failed to delete the Resource " + resp)
            return false
        }
        else
        {
            println("Resource " + resourceName + " deleted.")
            return true
        }
    }

    public deleteCommanderWorkspace(String workspaceName) {

        println("Deleting Workspace")
        def resp = PerformHTTPRequest(RequestMethod.DELETE, '/rest/v1.0/workspaces/' + workspaceName,[])

        if(resp?.status >= 400)
        {
            println("Failed to delete the Workspace " + resp)
            return false
        }
        else
        {
            println("Workspace " + workspaceName + " deleted.")
            return true
        }
    }

    public deleteCommanderResourcePool(String resourcePoolName) {

        println("Deleting Resource Pool")
        def resp = PerformHTTPRequest(RequestMethod.DELETE, '/rest/v1.0/resourcePools/' + resourcePoolName,[])

        if(resp?.status >= 400)
        {
            println("Failed to delete the Resource Pool " + resp)
            return false
        }
        else
        {
            println("Resource Pool " + resourcePoolName + " deleted.")
            return true
        }
    }

    def rollback(String resourceName)
    {
        println "Going for rollback"
        def resourceInfo = getResourceProperties(resourceName)
        if(resourceInfo == null)
        {
            println("Could not fetch properties for resource: " + resourceName)
            System.exit(1)
        }

        def (resourcePropertyMap, isResourcePool) = resourceInfo
        resourcePropertyMap.each { resource, property->

            println("Deleting resource: " + resource + " with instance Id: " + property["instance_id"] + " and resource group name: " + property["resource_group_name"])
            if(property["resource_group_name"] && property["instance_id"])
            {
                azure.deleteVM(property["resource_group_name"],  property["instance_id"])
                deleteCommanderResource(resource)
            }
        }

        if(isResourcePool)
        {
            deleteCommanderResourcePool(resourceName)
        }
    }

    private PerformHTTPRequest(RequestMethod request, String url, Object jsonData) {
        PerformHTTPRequest(request,url,["":""],jsonData)
    }
    private PerformHTTPRequest(RequestMethod request, String url, def query, Object jsonData) {

        def response
        def requestHeaders = ['Cookie': "sessionId=" + sessionId, 'Accept': 'application/json']

        //Standardize the error handling for client.
        client.handler.failure = client.handler.success

        try {
            switch (request) {
                case RequestMethod.GET:
                    response = client.get(path: url, query: query, headers: requestHeaders, requestContentType: JSON)
                    break
                case RequestMethod.POST:
                    if (query) {
                        response = client.post(path: url, query:query, headers: requestHeaders, body: jsonData, requestContentType: JSON)
                    }
                    else {
                        response = client.post(path: url, headers: requestHeaders, body: jsonData, requestContentType: JSON)
                    }
                    break
                case RequestMethod.PUT:
                    response = client.put(path: url, headers: requestHeaders, body: jsonData, requestContentType: JSON)
                    break
                case RequestMethod.DELETE:
                    response = client.delete(path: url, headers: requestHeaders)
                    break
                case RequestMethod.PUT:
                    break
            }
        } catch (groovyx.net.http.HttpResponseException ex) {
            ex.printStackTrace()
            return null
        } catch (java.net.ConnectException ex) {
            ex.printStackTrace()
            return null
        }
        return response
    }

        def exceptionHandler(Closure c) {
        try {
            c.call()
        } catch(Exception e) {
            e.printStackTrace()
            def errorMessage;
            def json;
            def finalMessage;
            try {
                errorMessage = e.getCause().getMessage()
            } catch (any) {
                errorMessage = e.getMessage();
            }
            try {
                json = new JsonSlurper().parseText(errorMessage)
                if (json.error) {
                    finalMessage = json.error.code + " : " + json.error.message;
                }
            }
            catch (any) {
                finalMessage = errorMessage;
            }
            if (!finalMessage) {
                finalMessage = erorrMessage.toString();
            }
            setProperty("summary", finalMessage.toString(), true)
            System.exit(1)
        }
    }
}

class Azure {
    def baseURI =  "https://management.azure.com/"
    def managementURL = "https://management.core.windows.net/"
    def aadURL = "https://login.windows.net/"
    String tenantID
    String subscriptionID
    String clientID
    String clientSecret
    def exceptionHandler
    def resourceManagementClient
    def storageManagementClient
    def computeManagementClient
    def sqlManagementClient
    def networkResourceProviderClient

    private String[] parseArgWithResourceGroup(String resourceGroupName, String arg) {
        def capture = (arg =~ /^\s*-rgn\s+(.+)\s+(.+)\s*$/) // resouce group:vnet
        capture.size() == 1 ? capture[0][1..2] : [resourceGroupName, arg]
    }


    private createManagementClient() {
        exceptionHandler{
            def config = createConfiguration()
            resourceManagementClient = ResourceManagementService.create(config)
            storageManagementClient = StorageManagementService.create(config)
            computeManagementClient = ComputeManagementService.create(config)
            sqlManagementClient = SqlManagementService.create(config);
            networkResourceProviderClient = NetworkResourceProviderService.create(config)
        }
    }

    private createConfiguration() {
        exceptionHandler{
            ManagementConfiguration.configure(
                null,
                baseURI != null ? new URI(baseURI) : null,
                subscriptionID,
                AuthHelper.getAccessTokenFromServicePrincipalCredentials(
                        managementURL, aadURL, tenantID, clientID, clientSecret)
                        .getAccessToken())
        }
    }

    private String getPublicIP(String publicIpAddressName ,String resourceGroupName, String vmName)
    {
            def VMStatus = getVMStatus(resourceGroupName, vmName)

            if(VMStatus == ProvisioningStateTypes.SUCCEEDED )
              return networkResourceProviderClient.getPublicIpAddressesOperations().get(resourceGroupName, publicIpAddressName).getPublicIpAddress().getIpAddress()
            else
              return null;
    }

    private String getVMStatus(String resourceGroupName, String vmName )
    {
            def VMStatus = computeManagementClient.getVirtualMachinesOperations().getWithInstanceView( resourceGroupName, vmName).getVirtualMachine().getProvisioningState()

            while(VMStatus == ProvisioningStateTypes.CREATING )
            {
              sleep(10000)
              VMStatus = computeManagementClient.getVirtualMachinesOperations().getWithInstanceView( resourceGroupName, vmName).getVirtualMachine().getProvisioningState()
            }
            return VMStatus
    }

    def createVM( String vmName, boolean isUserImage, String imageURN, String storageAccountName, String storageContainerName, String location, String resourceGroupName, boolean createPublicIPAddress, String adminName, String adminPassword, String osType, String publicKey, boolean disablePasswordAuth, String vnet, String subnet, String machineSize) {
        exceptionHandler{

            println("Going for creating VM=> Virtual Machine Name:" + vmName + ", Image URN:" + imageURN + ", Is User Image:" + isUserImage + ", Storage Account:" + storageAccountName + ", Storage Container:" + storageContainerName + ", Location:" + location + ", Resource Group Name:" + resourceGroupName + ", Create Public IP Address:" + createPublicIPAddress + ", Virtual Machine User:" + adminName + ", Virtual Machine Password:xxxxxx, OS Type:" + osType + ", Disable Password Authentication: " + disablePasswordAuth)
            ResourceContext context = new ResourceContext(location, resourceGroupName, subscriptionID, createPublicIPAddress);
            context.setStorageAccountName(storageAccountName)
            context.setContainerName(storageContainerName)

            def storageAccountResourceGroupName
            (storageAccountResourceGroupName, storageAccountName) = parseArgWithResourceGroup(resourceGroupName, storageAccountName)
            ResourceContext storageAccountContext = new ResourceContext(location, storageAccountResourceGroupName, subscriptionID, createPublicIPAddress);
            storageAccountContext.setStorageAccountName(storageAccountName)
            storageAccountContext.setContainerName(storageContainerName)

            StorageAccount storageAccount = StorageHelper.getStorageAccount(storageManagementClient, storageAccountContext)
            String storageURI = ""
            if(storageAccount)
            {
                context.setStorageAccount(storageAccount)
                println("Set already existing storage account in context: " + storageAccountName)
                def unixTime = System.currentTimeMillis().toString();
                storageURI = String.format("https://%s.blob.core.windows.net/%s", context.getStorageAccount().getName(), context.getContainerName()) + String.format("/os-%s-%s.vhd", vmName, unixTime)
            }

            def vnetResourceGroupName
            if(vnet)
            {
                (vnetResourceGroupName, vnet) = parseArgWithResourceGroup(resourceGroupName, vnet)
                VirtualNetwork createdVnet = networkResourceProviderClient.getVirtualNetworksOperations()
                                                                            .get(vnetResourceGroupName, vnet)
                                                                            .getVirtualNetwork()
                context.setVirtualNetwork(createdVnet)
            }
            if(subnet && vnet)
            {
                def subnetContext = new ResourceContext(location, vnetResourceGroupName, subscriptionID, createPublicIPAddress);
                subnetContext.setStorageAccountName(storageAccountName)
                subnetContext.setContainerName(storageContainerName)

                if (subnetContext.isCreatePublicIpAddress() && subnetContext.getPublicIpAddress() == null)
                    NetworkHelper.createPublicIpAddress(networkResourceProviderClient, subnetContext)

                NetworkHelper.createNIC(networkResourceProviderClient, subnetContext,
                                                                              networkResourceProviderClient.getSubnetsOperations()
                                                                              .get(subnetContext.getResourceGroupName(), vnet ,subnet).getSubnet())
            }

            VirtualMachine virtualMachine
            if (!isUserImage)
            {
                def (publisher, offer, sku, version) = imageURN.tokenize(':')
                virtualMachine = ComputeHelper.createVM(resourceManagementClient, computeManagementClient,
                                networkResourceProviderClient, storageManagementClient,
                                context, vmName, adminName, adminPassword,
                                new ConsumerWrapper<VirtualMachine>() {
                                @Override
                                public void accept(VirtualMachine vm) {
                                    vm.getStorageProfile().setDataDisks(null);
                                    ImageReference ir = new ImageReference();
                                    ir.setPublisher(publisher.trim());
                                    ir.setOffer(offer.trim());
                                    ir.setSku(sku.trim());
                                    ir.setVersion(version.trim());
                                    vm.getStorageProfile().setImageReference(ir);
                                    if (machineSize){
                                        vm.getHardwareProfile().setVirtualMachineSize(machineSize);
                                    }
                                    if (storageURI)
                                    {
                                        vm.getStorageProfile().getOSDisk().virtualHardDisk.setUri(storageURI)
                                    }
                                    if (osType == "Linux" && publicKey)
                                    {
                                        //Make SSH Configgurations only if Linux Machine
                                        OSProfile osProfile = vm.getOSProfile();
                                        String sshPath = String.format("%s%s%s","/home/", osProfile.getAdminUsername(), "/.ssh/authorized_keys");
                                        // set linux configuration
                                        LinuxConfiguration linuxConfiguration = new LinuxConfiguration();
                                        if (disablePasswordAuth)
                                        {
                                            linuxConfiguration.setDisablePasswordAuthentication(true);
                                        }
                                        else
                                        {
                                            linuxConfiguration.setDisablePasswordAuthentication(false);
                                        }
                                        SshConfiguration sshConfiguration = new SshConfiguration();
                                        ArrayList<SshPublicKey> publicKeys = new ArrayList<SshPublicKey>(1);
                                        SshPublicKey sshPublicKey = new SshPublicKey();
                                        sshPublicKey.setPath(sshPath);
                                        sshPublicKey.setKeyData(publicKey);
                                        publicKeys.add(sshPublicKey);
                                        sshConfiguration.setPublicKeys(publicKeys);
                                        linuxConfiguration.setSshConfiguration(sshConfiguration);
                                        osProfile.setLinuxConfiguration(linuxConfiguration);
                                    }
                                }
                        }).getVirtualMachine();
            }
            else
            {

                virtualMachine = ComputeHelper.createVM(resourceManagementClient, computeManagementClient,
                                networkResourceProviderClient, storageManagementClient,
                                context, vmName, adminName, adminPassword,
                                new ConsumerWrapper<VirtualMachine>() {
                                @Override
                                public void accept(VirtualMachine vm) {
                                    VirtualHardDisk vmDisk = new VirtualHardDisk();
                                    VirtualHardDisk imageDisk = new VirtualHardDisk();
                                    if (machineSize) {
                                        vm.getHardwareProfile().setVirtualMachineSize(machineSize);
                                    }
                                    if (storageURI)
                                    {
                                        vmDisk.setUri(storageURI);
                                    }
                                    imageDisk.setUri(imageURN);
                                    OSDisk osDisk = new OSDisk(vmName + "-osdisk", vmDisk, "fromImage");
                                    osDisk.setCaching(CachingTypes.NONE);
                                    osDisk.setOperatingSystemType(osType);
                                    osDisk.setSourceImage(imageDisk);
                                    StorageProfile storageProfile = new StorageProfile()
                                    storageProfile.setOSDisk(osDisk);
                                    vm.setStorageProfile(storageProfile);
                                    if (osType == "Linux" && publicKey)
                                    {
                                        //Make SSH Configgurations only if Linux Machine
                                        OSProfile osProfile = vm.getOSProfile();
                                        String sshPath = String.format("%s%s%s","/home/", osProfile.getAdminUsername(), "/.ssh/authorized_keys");
                                        // set linux configuration
                                        LinuxConfiguration linuxConfiguration = new LinuxConfiguration();
                                        if (disablePasswordAuth)
                                        {
                                            linuxConfiguration.setDisablePasswordAuthentication(true);
                                        }
                                        else
                                        {
                                            linuxConfiguration.setDisablePasswordAuthentication(false);
                                        }
                                        SshConfiguration sshConfiguration = new SshConfiguration();
                                        ArrayList<SshPublicKey> publicKeys = new ArrayList<SshPublicKey>(1);
                                        SshPublicKey sshPublicKey = new SshPublicKey();
                                        sshPublicKey.setPath(sshPath);
                                        sshPublicKey.setKeyData(publicKey);
                                        publicKeys.add(sshPublicKey);
                                        sshConfiguration.setPublicKeys(publicKeys);
                                        linuxConfiguration.setSshConfiguration(sshConfiguration);
                                        osProfile.setLinuxConfiguration(linuxConfiguration);
                                    }
                                }
                        }).getVirtualMachine();
            }

            def publicIP
            if(createPublicIPAddress)
                 publicIP = getPublicIP(context.getPublicIpName(),resourceGroupName, vmName)

            return [publicIP, getVMStatus(resourceGroupName, vmName)]
        }
    }

public deleteVM(String resourceGroupName,String vmName){
    exceptionHandler{

           if(computeManagementClient.getVirtualMachinesOperations().get(resourceGroupName,vmName).getVirtualMachine().getName())
           {
                println("Going for deleting VM=> Virtual Machine Name: " + vmName + " , Resource Group Name: " + resourceGroupName)
                DeleteOperationResponse deleteOperationResponse = computeManagementClient.getVirtualMachinesOperations().delete(resourceGroupName,vmName)
                if(deleteOperationResponse.getStatusCode() == OperationStatus.Succeeded  || deleteOperationResponse.getRequestId() != null)
                    println("Deleted VM: " + vmName )
                else
                    println("Failed to delete VM:" + vmName)
            }
    }
}

public startVM(String resourceGroupName, String vmName){
    exceptionHandler{

            println("Going for starting VM=> Virtual Machine Name: " + vmName + " , Resource Group Name: " + resourceGroupName)
            ComputeLongRunningOperationResponse startOperationResponse = computeManagementClient.getVirtualMachinesOperations().start(resourceGroupName,vmName)
            if(startOperationResponse.getStatus() == ComputeOperationStatus.Succeeded  || startOperationResponse.getRequestId() != null)
                println("Started VM: " + vmName )
            else
                println("Failed to start the VM: " + vmName)
   }
}

public stopVM(String resourceGroupName, String vmName){
    exceptionHandler{

            println("Going for stopping VM=> Virtual Machine Name: " + vmName + " , Resource Group Name: " + resourceGroupName)
            ComputeLongRunningOperationResponse stopOperationResponse = computeManagementClient.getVirtualMachinesOperations().powerOff(resourceGroupName,vmName)
            if(stopOperationResponse.getStatus()==ComputeOperationStatus.Succeeded  || stopOperationResponse.getRequestId() != null)
                println("Stopped VM: " + vmName )
            else
                println("Failed to stop the VM: " + vmName)
    }
}

public restartVM(String resourceGroupName, String vmName){
    exceptionHandler{

            println("Going for restarting VM=> Virtual Machine Name: " + vmName + " , Resource Group Name: " + resourceGroupName)
            ComputeLongRunningOperationResponse restartOperationResponse = computeManagementClient.getVirtualMachinesOperations().restart(resourceGroupName,vmName)
            if(restartOperationResponse.getStatus()==ComputeOperationStatus.Succeeded  || restartOperationResponse.getRequestId() != null)
                println("Restarted VM: " + vmName )
            else
                println("Failed to restart the VM: " + vmName)
    }
}

    def createOrUpdateDatabaseServer(String resourceGroupName, String serverName, String location,
                                     String adminUser, String adminPassword, String version) {
        exceptionHandler{
            println("Going for creating or updating database server: " + serverName + "(Resource Group: " +
                     resourceGroupName + ") in location " + location)

            ServerCreateOrUpdateProperties serverProperties = new ServerCreateOrUpdateProperties();
            if (adminUser)
            {
                serverProperties.setAdministratorLogin(adminUser)
                println("Set Administrator login to:" + adminUser)
            }
            if (adminPassword)
            {
                serverProperties.setAdministratorLoginPassword(adminPassword)
                println("Set Administrator login password to: xxxxx")
            }
            if (version)
            {
                serverProperties.setVersion(version)
                println("Set version to: " + version)
            }

            ServerCreateOrUpdateParameters serverParameters = new ServerCreateOrUpdateParameters(serverProperties, location)
            ServerGetResponse response = sqlManagementClient.getServersOperations().createOrUpdate(resourceGroupName, serverName, serverParameters)
            if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
                {
                    println("Successfully created SQL Database server: " + serverName )
                }
        }
    }

    def deleteDatabaseServer(String resourceGroupName, String serverName){
        exceptionHandler{
            println("Going for deleting database server: " + serverName + "(Resource Group: " + resourceGroupName + ")")
            sqlManagementClient.getServersOperations().delete(resourceGroupName, serverName)
        }
    }

    def createOrUpdateSubnet(String resourceGroupName, String virtualNetworkName, String subnetName, String addressPrefix, String networkSecurityGroupJson, String routeTableJson){
        exceptionHandler{
            println("Going for creating/updating subnet : " + subnetName + " in (Resource Group: " + resourceGroupName + " , Virtual Network: " + virtualNetworkName + ") with Address Prefix: " + addressPrefix + ", Network Security Group: " + networkSecurityGroupJson + ", Route Table : " + routeTableJson + ")")
            Subnet subnetParameters = new Subnet()
            //It should follow CIDR notation, for example 10.0.0.0/24.
            //This is required parameter
            if(addressPrefix){
                subnetParameters.setAddressPrefix(addressPrefix)
                println("Address prefix set to: " + addressPrefix)
            }

            //RouteTable of same name can be present in multiple resource group
            //Therfore we are using Json to uniquely identify RouteTable
            //{"RG1":"routeTable"}
            if(routeTableJson){
                ResourceId routeTable
                jsonParser(routeTableJson, 0).each{rg, route->
                    routeTable = new ResourceId()
                    routeTable.setId(networkResourceProviderClient.getRouteTablesOperations().
                                                                   get(rg, route).getRouteTable().getId())
                    subnetParameters.setRouteTable(routeTable)
                }
            }

            //Network Security Group of same name can be present in multiple resource group
            //Therfore we are using Json to uniquely identify Network Security Group
            //{"RG1":"NSG1"}
            if(networkSecurityGroupJson){
                ResourceId nsgResourceId
                jsonParser(networkSecurityGroupJson, 0).each{rg, nsg->
                    nsgResourceId= new ResourceId()
                    nsgResourceId.setId(networkResourceProviderClient.getNetworkSecurityGroupsOperations().get(rg, nsg).
                                                                      getNetworkSecurityGroup().getId())
                    subnetParameters.setNetworkSecurityGroup(nsgResourceId)
                }
            }

            AzureAsyncOperationResponse response = networkResourceProviderClient.getSubnetsOperations().createOrUpdate(resourceGroupName, virtualNetworkName, subnetName, subnetParameters)

            if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
            {
                println("Successfully created Subnet: " + subnetName )
            }
        }
    }

    def deleteSubnet(String resourceGroupName, String virtualNetworkName, String subnetName){
        exceptionHandler{

            if(networkResourceProviderClient.getSubnetsOperations().get(resourceGroupName,virtualNetworkName,subnetName).getSubnet().getName() != null)
            {
                println("Going for deleting subnet: " + subnetName + " in (Resource Group: " + resourceGroupName + " , Virtual Network: " + virtualNetworkName + ")")
                OperationResponse response = networkResourceProviderClient.getSubnetsOperations().delete(resourceGroupName, virtualNetworkName, subnetName)

                if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
                {
                    println("Successfully deleted Subnet: " + subnetName )
                }
            }
        }
    }

    def createOrUpdateNetworkSecurityGroup(String resourceGroupName, String location, String networkSecurityGroupName, String securityRuleJson){
        exceptionHandler{
            println("Going for creating/updating network security group : " + networkSecurityGroupName + " in (Resource Group: " + resourceGroupName + " , Location: " + location + ") with Security Rule: " + securityRuleJson + ")")
            NetworkSecurityGroup nsgParameters = new NetworkSecurityGroup(location)

            if(securityRuleJson){
                //Security Rule with same name can be created in multiple Network Security Group
                //Network Security Group with same name can be created in multiple Resource Group
                //Therfore we are using nested JSON to uniquely identify security rule
                //{"RG-1":{"NSG-1":["Rule-A", "Rule-B"], "NSG-2":["Rule-C", "Rule-D"]}, "RG-2":{"NSG-3":"RULE-G"}}
                ArrayList<SecurityRule> securityRules = new ArrayList<SecurityRule>()
                jsonParser(securityRuleJson, 1).each{rg, nsg, rule->
                    securityRules.add(networkResourceProviderClient.getSecurityRulesOperations().get(rg, nsg, rule).getSecurityRule())
                }
                nsgParameters.setSecurityRules(securityRules)
            }

            AzureAsyncOperationResponse response = networkResourceProviderClient.getNetworkSecurityGroupsOperations().createOrUpdate(resourceGroupName, networkSecurityGroupName, nsgParameters)

            if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
            {
                println("Successfully created Network Security Group: " + networkSecurityGroupName )
            }
        }
    }

    def deleteNetworkSecurityGroup(String resourceGroupName, String networkSecurityGroupName){
        exceptionHandler{
            if(networkResourceProviderClient.getNetworkSecurityGroupsOperations().get(resourceGroupName, networkSecurityGroupName).getNetworkSecurityGroup().getName() != null)
            {
                println("Going for deleting Network Security Group: " + networkSecurityGroupName + " in (Resource Group: " + resourceGroupName + ")")
                OperationResponse response = networkResourceProviderClient.getNetworkSecurityGroupsOperations().delete(resourceGroupName, networkSecurityGroupName)

                if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
                {
                    println("Successfully deleted Network Security Group: " + networkSecurityGroupName )
                }
            }
        }
    }

    def createOrUpdateNetworkSecurityRule(String resourceGroupName, String networkSecurityGroupName, String securityRuleName,             String access, String description, String sourceAddressPrefix, String sourcePortRange, String destinationAddressPrefix, String destinationPortRange, String direction, String priority, String protocol){
        exceptionHandler{
            println("Going for creating/updating network security rule : " + securityRuleName + " in (Resource Group: " + resourceGroupName + " , Network Security Group: " + networkSecurityGroupName  + ") with Access: " + access + ", Source Address Prefix: " + sourceAddressPrefix + ", Source Port Range: " + sourcePortRange  + ", Source Address Prefix: " + destinationAddressPrefix + ", Destination Port Range: " + destinationPortRange + " Protocol:" + protocol +" and Direction: " + direction)
            SecurityRule securityRuleParameters = new SecurityRule()

            if(access){
                securityRuleParameters.setAccess(access)
                println("Set access: " + access)
            }
            if(description){
               securityRuleParameters.setDescription(description)
               println("Set description: " + description)
            }
            if(destinationAddressPrefix){
                securityRuleParameters.setDestinationAddressPrefix(destinationAddressPrefix)
                println("Set destinationAddressPrefix: " + destinationAddressPrefix)
            }
            if(destinationPortRange){
                securityRuleParameters.setDestinationPortRange(destinationPortRange)
                println("Set destinationPortRange: " + destinationPortRange)
            }
            if(direction){
                securityRuleParameters.setDirection(direction)
                println("Set direction: " + direction)
            }
            if(priority){
                securityRuleParameters.setPriority(priority.toInteger())
                println("Set priority: " + priority)
            }
            if(protocol){
                securityRuleParameters.setProtocol(protocol)
                println("Set protocol: " + protocol)
            }

            if(sourceAddressPrefix){
                securityRuleParameters.setSourceAddressPrefix(sourceAddressPrefix)
                println("Set sourceAddressPrefix: " + sourceAddressPrefix)
            }
            if(sourcePortRange){
                securityRuleParameters.setSourcePortRange(sourcePortRange)
                println("Set sourcePortRange: " + sourcePortRange)
            }

            AzureAsyncOperationResponse response = networkResourceProviderClient.getSecurityRulesOperations().createOrUpdate(resourceGroupName, networkSecurityGroupName, securityRuleName, securityRuleParameters)

            if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
            {
                println("Successfully created Security Rule: " + securityRuleName )
            }
        }
    }

    def deleteNetworkSecurityRule(String resourceGroupName, String networkSecurityGroupName, String securityRuleName)
    {
        exceptionHandler{
            if(networkResourceProviderClient.getSecurityRulesOperations().get(resourceGroupName, networkSecurityGroupName, securityRuleName).getSecurityRule().getName() != null)
            {
                println("Going for deleting Network Security Rule: " + securityRuleName + " in (Resource Group: " + resourceGroupName + " ,Network Security Group: " + networkSecurityGroupName + ")")
                OperationResponse response = networkResourceProviderClient.getSecurityRulesOperations().delete(resourceGroupName,networkSecurityGroupName, securityRuleName)

                if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
                {
                    println("Successfully deleted Security Rule: " + securityRuleName )
                }
            }
        }
    }

    //Nesting Level 0- Simple Json
    //Nesting Level 1- Json inside Json
    def jsonParser(jsonString, nestingLevel){
        exceptionHandler{
            def json = new JsonSlurper().parseText(jsonString)
            ArrayList result = []
            if(nestingLevel == 0){
                json.each{i,u->
                    makeJsonArray(u).each{j->
                        result.push([i.trim(), j.trim()])
                    }
                }
            }
            else if (nestingLevel == 1){
                json.each{i,u->
                    u.each{j,v->
                        makeJsonArray(v).each{k->
                            result.push([i.trim(), j.trim(), k.trim()])
                        }
                    }
                }
            }
            else
            {
                println("Nesting Level: " + nestingLevel + " not supported")
            }
            result
        }
    }

    def makeJsonArray(json){
        exceptionHandler{
            def jsonArray = []
            if (json instanceof ArrayList){
                jsonArray = json
            }
            else
            {
                jsonArray.push(json)
            }
            jsonArray
        }
    }

public deleteDatabase(String resourceGroupName, String serverName, String databaseName){
    exceptionHandler{

        if(sqlManagementClient.getDatabasesOperations().get(resourceGroupName, serverName, databaseName).getDatabase().getName() != null)
        {
            println("Going for deleting database: " + databaseName + "(Resource Group: " + resourceGroupName + " , Server Name: " + serverName + ")")
            sqlManagementClient.getDatabasesOperations().delete(resourceGroupName, serverName, databaseName)
        }
    }
}

public deleteVnet(String resourceGroupName ,String vnetName)
{
    exceptionHandler{

        if(networkResourceProviderClient.getVirtualNetworksOperations().get(resourceGroupName,vnetName).getVirtualNetwork().getName() != null)
        {
            println("Going for deleting Virtual Network: " + vnetName + "  in Resource Group: " + resourceGroupName )
            OperationResponse response = networkResourceProviderClient.getVirtualNetworksOperations().delete(resourceGroupName,vnetName)
            if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
                    {
                        println("Successfully deleted Vnet: " + vnetName )
                    }
        }
     }
}

public createOrUpdateDatabase(String resourceGroupName, String serverName, String databaseName, String location, String expectedCollationName, String expectedEdition, String expectedMaxSizeInMB, String createModeValue, String elasticPoolName, String requestedServiceObjectiveIdValue, String sourceDatabaseIdValue) {
    exceptionHandler{

        println("Going for creating or updating database: " + databaseName + "(Resource Group: " + resourceGroupName + " , Server Name: " + serverName + ") in location " + location)
        DatabaseCreateOrUpdateProperties dbProperties = new DatabaseCreateOrUpdateProperties();
        if (expectedCollationName)
        {
            dbProperties.setCollation(expectedCollationName)
            println("Set Collation name to " + expectedCollationName)
        }
        if (createModeValue)
        {
            dbProperties.setCreateMode(createModeValue)
            println("Set Create Mode to " + createModeValue)
        }
        if (expectedEdition)
        {
            dbProperties.setEdition(expectedEdition)
            println("Set Edition to " + expectedEdition)
        }
        if (elasticPoolName)
        {
            dbProperties.setElasticPoolName(elasticPoolName)
            println("Set Elastic Pool Name to " + elasticPoolName)
        }
        if (expectedMaxSizeInMB)
        {
            expectedMaxSizeInBytes = (expectedMaxSizeInMB as int) * 1024 *1024
            dbProperties.setMaxSizeBytes(expectedMaxSizeInBytes)
            println("Set Maximum Size  to " + expectedMaxSizeInBytes + " bytes")
        }
        if (requestedServiceObjectiveIdValue)
        {
            dbProperties.setRequestedServiceObjectiveId(requestedServiceObjectiveIdValue)
            println("Set Requested Service Objective Id to " + requestedServiceObjectiveIdValue)
        }
        if (sourceDatabaseIdValue)
        {
            dbProperties.setSourceDatabaseId(sourceDatabaseIdValue)
            println("Set Source Database Id to " + sourceDatabaseIdValue)
        }
        DatabaseCreateOrUpdateParameters dbParameters = new DatabaseCreateOrUpdateParameters(dbProperties, location);
        DatabaseCreateOrUpdateResponse response = sqlManagementClient.getDatabasesOperations().createOrUpdate(resourceGroupName, serverName, databaseName, dbParameters);
        if(response.getStatusCode() == OperationStatus.Succeeded  || response.getRequestId() != null)
                    println("Created SQL Database: " + databaseName )
                else
                    println("Failed to create SQL Database:" + databaseName)
    }
}

public createVnet(def vnetName, def subnetName, def vnetAddressSpace, def subnetAddressSpace, def resourceGroupName , def location, def dnsServer)
    {
        exceptionHandler{

                VirtualNetwork vnet = new VirtualNetwork(location);

                // set AddressSpace
                AddressSpace asp = new AddressSpace()
                ArrayList<String> addrPrefixes = new ArrayList<String>(1)
                addrPrefixes.add(vnetAddressSpace)
                asp.setAddressPrefixes(addrPrefixes)
                vnet.setAddressSpace(asp)

                 // set subNet
                Subnet subnet = new Subnet(subnetAddressSpace)
                subnet.setName(subnetName)
                ArrayList<Subnet> subNets = new ArrayList<Subnet>(1);
                subNets.add(subnet);
                vnet.setSubnets(subNets)

                if(dnsServer)
                {
                    // set DhcpOptions
                    DhcpOptions dop = new DhcpOptions()
                    ArrayList<String> dnsServers = new ArrayList<String>(2)
                    dnsServers.add(dnsServer)
                    dop.setDnsServers(dnsServers)
                    vnet.setDhcpOptions(dop)
                }




                AzureAsyncOperationResponse createVnetResponse = networkResourceProviderClient
                                                                    .getVirtualNetworksOperations()
                                                                    .createOrUpdate(resourceGroupName, vnetName, vnet)

                if(createVnetResponse.getStatusCode() == OperationStatus.Succeeded  || createVnetResponse.getRequestId() != null)
                    println("Virtual Network: " + vnetName + " was successfully created/updated")
                else
                    println("Failed to create Virtual Network:" + vnetName)
        }
    }

}

class SQLOperations {
    //Database Connection object.
    def dbCon
    def exceptionHandler
    SQLOperations(server, database, port, user, password, exceptionHandlerClosure)
    {
        exceptionHandler = exceptionHandlerClosure
        setDatabaseConnection(server, database, port, user, password)
    }

    def loadDriverJar(toLoad)
    {
        //extract path to .jar file
        def res='/'+toLoad.replaceAll('\\.','/')+'.class';
        def c=this.class.classLoader.loadClass(toLoad);
        def file=''+ c.getResource(res).file;
        file=file.substring(0,file.size()-(res.size()+1));
        // add .jar to systemClassLoader
        ClassLoader.systemClassLoader.addURL(new URL(file));
    }

    def setDatabaseConnection(server, database, port, user, password){
        exceptionHandler{
            def connectionUrl = "jdbc:jtds:sqlserver://" + server + ".database.windows.net:" + port +
                ";database=" + database +
                ";encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30"
            def driverClass="net.sourceforge.jtds.jdbc.Driver"
            loadDriverJar(driverClass)
            dbCon = Sql.newInstance( connectionUrl, user , password , driverClass)
        }
    }

    def execute(sqlQuery){
        exceptionHandler{
            if(sqlQuery.toLowerCase().startsWith("select")){
                dbCon.eachRow(sqlQuery) { row ->
                      println row
                }
            }
            else{
                dbCon.execute sqlQuery
            }
            dbCon.close()
        }
    }
}

public class NoSQLOperations {
    def tableClient
    def exceptionHandler
    NoSQLOperations(accountName, accountKey, storageAccount, exceptionHandlerClosure)
    {
        exceptionHandler = exceptionHandlerClosure
        setDatabaseConnection(accountName, accountKey, storageAccount)
    }

    private setDatabaseConnection(accountName, accountKey, storageAccount){
        exceptionHandler{
            def storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + accountName + ";AccountKey=" + accountKey + ";TableEndpoint=https://" + storageAccount + ".table.core.windows.net/;"
            def storage = CloudStorageAccount.parse(storageConnectionString)
            tableClient = storage.createCloudTableClient()
        }
    }

    def createTable(tableName){
        exceptionHandler{
           println("Going for creating table: " + tableName)
           CloudTable table = tableClient.getTableReference(tableName)
           table.createIfNotExists()
           println("Table created successfully. URI: " + table.getUri().toString())
        }
    }

    def insert(tableName, toBeSet, partitionKey){
        exceptionHandler{
           println("Going for inserting " + toBeSet + " in table: " + tableName + " with partition key: " + partitionKey)
           CloudTable table = tableClient.getTableReference(tableName)
           def rows = new JsonSlurper().parseText(toBeSet)
           def rowList = []
           //For handling single objects
           if (!(rows instanceof ArrayList))
           {
               rowList.push(rows)
           }
           else
           {
               rowList = rows
           }
           DynamicTableEntity entity
           TableBatchOperation batchOperation = new TableBatchOperation()
           rowList.each{row->
               entity = new DynamicTableEntity()
               entity.setPartitionKey(partitionKey);
               entity.setRowKey(UUID.randomUUID().toString());
               row.each{column, value->
                   entity.getProperties().put(column, new EntityProperty(value))
               }
               batchOperation.insert(entity)
           }
           table.execute(batchOperation)
           println("Entities inserted successfully")
        }
    }

    private getWhereClause(whereClause){
        exceptionHandler{
           TableQuery<DynamicTableEntity> query
           if (whereClause)
           {
               query = TableQuery.from(DynamicTableEntity.class).where(whereClause)
           }
           else
           {
               query = TableQuery.from(DynamicTableEntity.class)
           }
           query
        }
    }

    def retrieve(tableName, toBeRetrieved, whereClause){
        exceptionHandler{
           println("Going for retrieving " + toBeRetrieved + " in table: " + tableName + " where : " + whereClause)
           CloudTable table = tableClient.getTableReference(tableName)
           TableQuery<DynamicTableEntity> query = getWhereClause(whereClause)
           if (toBeRetrieved != "*")
           {
               query.select(toBeRetrieved)
           }

           ResultSegment<DynamicTableEntity> result = table.executeSegmented(query, null);
           for (row in result.getResults())
           {
               print(" RowKey:" + row.getRowKey())
               print(" PartitionKey:" + row.getPartitionKey())
               row.getProperties().each{column, value->
                   print(" " + column + ":" + row.getProperties().get(column).getValueAsString())
               }
               println("")
           }
           println("Entities retrieved successfully")
        }
    }

    def delete(tableName, whereClause){
        exceptionHandler{
           println("Going for deletion in table: " + tableName + " where : " + whereClause)
           TableBatchOperation batchOperation = new TableBatchOperation()
           CloudTable table = tableClient.getTableReference(tableName)
           TableQuery<DynamicTableEntity> query = getWhereClause(whereClause)
           for (entity in table.execute(query))
           {
               batchOperation.delete(entity)
           }
           table.execute(batchOperation)
           println("Entities deleted successfully")
        }
    }

    def update(tableName, toBeUpdated, whereClause){
        exceptionHandler{
           println("Going for updating " + toBeUpdated + " in table: " + tableName + " where : " + whereClause)
           DynamicTableEntity entity
           TableBatchOperation batchOperation = new TableBatchOperation()
           CloudTable table = tableClient.getTableReference(tableName)
           def toBeUpdatedJson = new JsonSlurper().parseText(toBeUpdated)

           TableQuery<DynamicTableEntity> query = getWhereClause(whereClause)

           for (oldEntity in table.execute(query))
           {
               HashMap<String, EntityProperty> properties = oldEntity.getProperties()
               toBeUpdatedJson.each{column,value->
                   //Updating values
                   properties.put(column, new EntityProperty(value))
               }
               oldEntity.setProperties(properties)
               batchOperation.replace(oldEntity)
           }
           table.execute(batchOperation)
           println("Entities updated successfully")
        }
    }

    def deleteTable(tableName){
        exceptionHandler{
           println("Going for deleting table: " + tableName)
           CloudTable table = tableClient.getTableReference(tableName)
           table.deleteIfExists()
           println("Table deleted successfully")
        }
    }
}

