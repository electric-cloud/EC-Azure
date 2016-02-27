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

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.azure.management.compute.ComputeManagementClient;
import com.microsoft.azure.management.compute.ComputeManagementService;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementService;
import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.compute.models.VirtualMachineImageResourceList;
import com.microsoft.azure.management.compute.models.ImageReference
import com.microsoft.azure.utility.ConsumerWrapper;
import com.microsoft.azure.management.compute.models.OSDisk
import com.microsoft.azure.management.compute.models.OSProfile
import com.microsoft.azure.management.compute.models.StorageProfile
import com.microsoft.azure.management.compute.models.VirtualHardDisk
import com.microsoft.azure.management.compute.models.HardwareProfile
import com.microsoft.azure.management.compute.models.NetworkProfile
import com.microsoft.azure.management.compute.models.NetworkInterfaceReference
import com.microsoft.azure.management.compute.models.AvailabilitySet
import com.microsoft.azure.management.compute.models.AvailabilitySetReference;

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

        azure = new Azure([tenantID : getCommanderProperty("tenant_id"), subscriptionID : getCommanderProperty("subscription_id"), clientID : resp.getData().credential.userName, clientSecret : resp.getData().credential.password])
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

    public getFullCredentials( String parameterName ) {
        client.ignoreSSLIssues()

        def resp

        if(config == true) {
            resp = PerformHTTPRequest(RequestMethod.GET, '/rest/v1.0/jobsSteps/' + jobStepId + '/credentials/' + parameterName, [])
        }
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
                case RequestMethod.DELETE:
                    response = client.delete(path: url, headers: requestHeaders, body: jsonData, requestContentType: JSON)
                    break
                case RequestMethod.PUT:
                    break
            }
        } catch (groovyx.net.http.HttpResponseException ex) {
            println(ex.getResponse().getData())
            return null
        } catch (java.net.ConnectException ex) {
            println(ex.getResponse().getData())
            return null
        }
        return response
    }

}

public class Azure {
	def baseURI
	def managementURL
	def aadURL
	String tenantID
	String subscriptionID
	String clientID
	String clientSecret
	def config
	def resourceManagementClient
	def storageManagementClient
	def computeManagementClient
	def networkResourceProviderClient

	//Azure( tenantID = tenantID, subscriptionID = subscriptionID, clientID = clientID, clientSecret = clientSecret) {
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

	public createVM( String vmName, String imageName, String storageAccountName, String storageContainerName, String location, String resourceGroupName, boolean createPublicIPAddress, String adminName, String adminPassword ) {
		try {
			ResourceContext context = new ResourceContext(location, resourceGroupName, subscriptionID, createPublicIPAddress);

			context.setStorageAccountName(storageAccountName)
			context.setContainerName(storageContainerName)
			VirtualMachine vm = ComputeHelper.createVM(
											resourceManagementClient, computeManagementClient,
											networkResourceProviderClient, storageManagementClient,
											context, vmName, adminName, adminPassword)/*,
											new ConsumerWrapper<VirtualMachine>() {
			@Override
			public void accept(VirtualMachine vm) {
				println(vm.getStorageProfile().getOSDisk().getVirtualHardDisk().getUri());
				vm.getStorageProfile().getOSDisk().setOperatingSystemType("Windows")
				vm.getStorageProfile().getOSDisk().setCreateOption("fromImage")
				println(vm.getStorageProfile().getOSDisk().virtualHardDisk.setUri("https://avinashmsys7.blob.core.windows.net/vhds/myOSDisk123.vhd"))
				//vm.getNetworkProfile().getNetworkInterfaces().setName("azureResGroupMsys123nicbbomy")
				vm.getNetworkProfile().getNetworkInterfaces().
				//vm.getStorageProfile().getOSDisk().virtualHardDisk.setUri("https://avinashmsys7.blob.core.windows.net/vhds/myOSDisk123.vhd")
				//vm.getStorageProfile().getOSDisk().sourceImage.setUri("https://avinashmsys7.blob.core.windows.net/vhds/ososvhd.vhd");
				//m.getStorageProfile().getOSDisk().name("myOSDisk123")
				//vm.getStorageProfile().getOSDisk().
				println(vm.getStorageProfile().getOSDisk().getVirtualHardDisk().getUri());
			}
			})*/
			.getVirtualMachine();
			
			//System.out.println(vm.getName() + " is created");
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
}

