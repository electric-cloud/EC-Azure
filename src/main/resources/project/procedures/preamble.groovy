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
	@Grab(group='com.microsoft.azure', module='azure-core', version='0.9.3'),
	@Grab(group='org.slf4j', module='slf4j-jdk14', version='1.7.16'),
	@Grab(group='com.microsoft.azure', module='adal4j', version='1.0.0'),
	@Grab(group='commons-logging', module='commons-logging', version='1.2'),
	@Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.1'),
	@Grab(group='com.sun.jersey', module='jersey-core', version='1.13-b01'),

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

    def configProperties;

    ElectricCommander() {

        client.ignoreSSLIssues()

        def resp
        //Get Azure Connection Config
        resp = PerformHTTPRequest(RequestMethod.GET, '/rest/v1.0/jobsSteps/' + jobStepId + '/credentials/$[connection_config]', [])

        if( resp == null ) {
            throw new Exception("Error : Invalid configuration $[connection_config].");
        }
        if(resp.status != 200) {
            throw new Exception("Commander did not respond with 200 for credentials")
        }

        configProperties = getProperties('/myProject/azure_cfgs/$[connection_config]')

        azure = new Azure([tenantID : configProperties.tenant_id,
                           subscriptionID : configProperties.subscription_id,
                           clientID : resp.data.credential.userName,
                           clientSecret : resp.data.credential.password])
        azure.createManagementClient()
    }

    public setProperty(String propName, String propValue) {

        sysJobId = System.getenv('COMMANDER_JOBID')
        def jsonData = [propertyName : propName, value : propValue, jobId : sysJobId]

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


    def getProperties(String path) {

        // Add this line
        println(commanderServer + ":" + commanderPort)

        def uri = '/rest/v1.0/properties/' + path
        def resp = performHttpGet(uri)

        def properties = [:]
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

        def sysJobStepId = System.getenv('COMMANDER_JOBSTEPID')
        println("SysJobStepId: " + sysJobStepId)

        query.jobStepId = sysJobStepId

        println("query: " + JsonOutput.toJson(query))

        def resp = PerformHTTPRequest(RequestMethod.GET, uri, query, [])
        //println('Response status: ' + resp?.status)
        if(resp?.status != 200) {
            println("ERROR: HTTP GET request failed $uri")
            return [:]
        }
        //println(JsonOutput.toJson(resp.data))
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

    public createCommanderZone(String zoneName){

        println("Creating zone.")    
        def jsonData = [zoneName : zoneName, description : zoneName]
        def resp = PerformHTTPRequest(RequestMethod.POST, '/rest/v1.0/zones',jsonData)

        if(resp?.status == 409)     
        {
            println("Zone " + zoneName +" already exists.")
            return true
        }
        else if(resp?.status >= 400) 
        {
            println("Failed to create the zone " + resp)
            return false
        }
        else
        {
            println("Zone " + zoneName + " created.")
            return true
        }         
    }

    public createCommanderResourcePool(String resourcePoolName){  

        println("Creating Resource Pool")
        def jsonData = [resourcePoolName : resourcePoolName, autoDelete : true, description : resourcePoolName , resourcePoolDisabled: false ]        
        def resp = PerformHTTPRequest(RequestMethod.POST, '/rest/v1.0/resourcePools/', jsonData)

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

    public boolean createCommanderResource(String resourceName, String workspaceName, String resourceIP ,String resourcePort, String resourceZone) {
        
        println("Creating Resource")
        def jsonData = [resourceName : resourceName, description : resourceName , hostName: resourceIP , zoneName: resourceZone]
        if (resourcePort) {
            jsonData.port = resourcePort
        }
        if (workspaceName) {
            jsonData.workspaceName = workspaceName
        }

        def resp = PerformHTTPRequest(RequestMethod.POST, '/rest/v1.0/resources/', jsonData)

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


    private PerformHTTPRequest(RequestMethod request, String url, Object jsonData) {
        PerformHTTPRequest(request,url,["":""],jsonData)
    }
    private PerformHTTPRequest(RequestMethod request, String url, def query, Object jsonData) {
        def response
        def requestHeaders = ['Cookie': "sessionId=" + sessionId, 'Accept': 'application/json']

        try {
            switch (request) {
                case RequestMethod.GET:
                    response = client.get(path: url, query: query, headers: requestHeaders, requestContentType: JSON)
                    break
                case RequestMethod.POST:
                    response = client.post(path: url, headers: requestHeaders, body: jsonData, requestContentType: JSON)
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
}

public class Azure {
    def baseURI =  "https://management.azure.com/"
    def managementURL = "https://management.core.windows.net/"
    def aadURL = "https://login.windows.net/"
	String tenantID
	String subscriptionID
	String clientID
	String clientSecret
	def config
	def resourceManagementClient
	def storageManagementClient
	def computeManagementClient
	def networkResourceProviderClient

	private createManagementClient () {
		try {
			config = createConfiguration();
			resourceManagementClient = ResourceManagementService.create(config);
			storageManagementClient = StorageManagementService.create(config);
			computeManagementClient = ComputeManagementService.create(config);
			networkResourceProviderClient = NetworkResourceProviderService.create(config);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	private createConfiguration() throws Exception {
		return ManagementConfiguration.configure(
				null,
				baseURI != null ? new URI(baseURI) : null,
				subscriptionID,
				AuthHelper.getAccessTokenFromServicePrincipalCredentials(
						managementURL, aadURL, tenantID, clientID, clientSecret)
						.getAccessToken());
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

	public String createVM( String vmName, boolean isUserImage, String imageURN, String storageAccountName, String storageContainerName, String location, String resourceGroupName, boolean createPublicIPAddress, String adminName, String adminPassword, String osType, String publicKey, boolean disablePasswordAuth) {
		try {
			println("Going for creating VM=> Virtual Machine Name:" + vmName + ", Image URN:" + imageURN + ", Is User Image:" + isUserImage + ", Storage Account:" + storageAccountName + ", Storage Container:" + storageContainerName + ", Location:" + location + ", Resource Group Name:" + resourceGroupName + ", Create Public IP Address:" + createPublicIPAddress + ", Virtual Machine User:" + adminName + ", Virtual Machine Password:xxxxxx, OS Type:" + osType + ", Disable Password Authentication: " + disablePasswordAuth)
			ResourceContext context = new ResourceContext(location, resourceGroupName, subscriptionID, createPublicIPAddress);

			context.setStorageAccountName(storageAccountName)
			context.setContainerName(storageContainerName)

			StorageAccount storageAccount= StorageHelper.getStorageAccount(storageManagementClient, context)
            String storageURI = ""
			if(storageAccount)
			{
					context.setStorageAccount(storageAccount)
					println("Set already existing storage account in context: " + storageAccountName)
					storageURI = String.format("https://%s.blob.core.windows.net/%s", context.getStorageAccount().getName(), context.getContainerName()) + String.format("/os-%s.vhd", vmName)
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
            return getPublicIP(context.getPublicIpName(),resourceGroupName, vmName)
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		}    

		public deleteVM(String resourceGroupName,String vmName){
		try {
			println("Going for deleting VM=> Virtual Machine Name: " + vmName + " , Resource Group Name: " + resourceGroupName)
			DeleteOperationResponse deleteOperationResponse = computeManagementClient.getVirtualMachinesOperations().delete(resourceGroupName,vmName);
			if(deleteOperationResponse.getStatusCode() == OperationStatus.Succeeded  || deleteOperationResponse.getRequestId() != null)
			{
				println("Deleted VM: " + vmName )
			}
		}catch(Exception ex) {
			println(ex.toString());
		}
		}

}

