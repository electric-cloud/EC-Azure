/*
   Copyright 2015 Electric Cloud, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package test.java.ecplugins.azure;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TestUtils {

	private static final long jobStatusPollIntervalMillis = 15000;
	private static Properties props;

	private static String vmName = "";
	private static String rgName = "";
	private static String vnetName = "";
	private static String subnetName = "";
	private static String nsgName = "";
	private static String nsrName = "";
	private static String dbName = "";
	private static String serverName = "";
	
	public static Properties getProperties() throws Exception {

		if (props == null) {
			props = new Properties();
			InputStream is = null;
			is = new FileInputStream("ecplugin.properties");
			props.load(is);
			is.close();
		}

		return props;
	}

	public static void setResourceAndWorkspace(String resourceName,
			String workspaceName) throws Exception {

		props = getProperties();
		//CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpClient httpClient = new DefaultHttpClient();
		JSONObject jo = new JSONObject();
		jo.put("projectName", "EC-Azure-" + StringConstants.PLUGIN_VERSION);
		jo.put("resourceName", resourceName);
		jo.put("workspaceName", workspaceName);

		HttpPut httpPutRequest = new HttpPut("http://"
				+ props.getProperty(StringConstants.COMMANDER_SERVER)
				+ ":8000/rest/v1.0/projects/" + "EC-Azure-"
				+ StringConstants.PLUGIN_VERSION);

		String encoding = new String(
				org.apache.commons.codec.binary.Base64
						.encodeBase64(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(props
								.getProperty(StringConstants.COMMANDER_USER)
								+ ":"
								+ props.getProperty(StringConstants.COMMANDER_PASSWORD))));

		StringEntity input = new StringEntity(jo.toString());

		input.setContentType("application/json");
		httpPutRequest.setEntity(input);
		httpPutRequest.setHeader("Authorization", "Basic " + encoding);
		HttpResponse httpResponse = httpClient.execute(httpPutRequest);

		if (httpResponse.getStatusLine().getStatusCode() >= 400) {
			throw new RuntimeException("Failed to set resource  "
					+ resourceName + " to project " + "EC-Azure-"
					+ StringConstants.PLUGIN_VERSION);
		}
		System.out.println("Set the resource as " + resourceName
				+ " and workspace as " + workspaceName + " successfully.");
	}

	/**
	 * Creates a new workspace. If the workspace already exists,It continues.
	 * 
	 */
	static void createCommanderWorkspace(String workspaceName) throws Exception {

		props = getProperties();
		//CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpClient httpClient = new DefaultHttpClient();
		JSONObject jo = new JSONObject();

		try {

			String url = "http://"
					+ props.getProperty(StringConstants.COMMANDER_SERVER)
					+ ":8000/rest/v1.0/workspaces/";
			String encoding = new String(
					org.apache.commons.codec.binary.Base64
							.encodeBase64(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(props
									.getProperty(StringConstants.COMMANDER_USER)
									+ ":"
									+ props.getProperty(StringConstants.COMMANDER_PASSWORD))));

			HttpPost httpPostRequest = new HttpPost(url);
			jo.put("workspaceName", workspaceName);
			jo.put("description", workspaceName);
			jo.put("agentDrivePath",
					"C:/Program Files/Electric Cloud/ElectricCommander");
			jo.put("agentUncPath",
					"C:/Program Files/Electric Cloud/ElectricCommander");
			jo.put("agentUnixPath", "/opt/electriccloud/electriccommander");
			jo.put("local", true);

			StringEntity input = new StringEntity(jo.toString());

			input.setContentType("application/json");
			httpPostRequest.setEntity(input);
			httpPostRequest.setHeader("Authorization", "Basic " + encoding);
			HttpResponse httpResponse = httpClient.execute(httpPostRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 409) {
				System.out
						.println("Commander workspace already exists.Continuing....");
			} else if (httpResponse.getStatusLine().getStatusCode() >= 400) {
				throw new RuntimeException(
						"Failed to create commander workspace "
								+ httpResponse.getStatusLine().getStatusCode()
								+ "-"
								+ httpResponse.getStatusLine()
										.getReasonPhrase());
			}
		} finally {
			//httpClient.close(); 
			httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * 
	 * @return
	 */
	static void createCommanderResource(String resourceName,
			String workspaceName, String resourceIP) throws Exception {

		props = getProperties();
		//CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpClient httpClient = new DefaultHttpClient();
		JSONObject jo = new JSONObject();

		try {
			HttpPost httpPostRequest = new HttpPost("http://"
					+ props.getProperty(StringConstants.COMMANDER_SERVER)
					+ ":8000/rest/v1.0/resources/");
			String encoding = new String(
					org.apache.commons.codec.binary.Base64
							.encodeBase64(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(props
									.getProperty(StringConstants.COMMANDER_USER)
									+ ":"
									+ props.getProperty(StringConstants.COMMANDER_PASSWORD))));

			jo.put("resourceName", resourceName);
			jo.put("description", "Resource created for test automation");
			jo.put("hostName", resourceIP);
			jo.put("port", StringConstants.EC_AGENT_PORT);
			jo.put("workspaceName", workspaceName);
			jo.put("pools", "default");
			jo.put("local", true);

			StringEntity input = new StringEntity(jo.toString());

			input.setContentType("application/json");
			httpPostRequest.setEntity(input);
			httpPostRequest.setHeader("Authorization", "Basic " + encoding);
			HttpResponse httpResponse = httpClient.execute(httpPostRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 409) {
				System.out
						.println("Commander resource already exists.Continuing....");

			} else if (httpResponse.getStatusLine().getStatusCode() >= 400) {
				throw new RuntimeException(
						"Failed to create commander workspace "
								+ httpResponse.getStatusLine().getStatusCode()
								+ "-"
								+ httpResponse.getStatusLine()
										.getReasonPhrase());
			}
		} finally {
			//httpClient.close();
			 httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * callRunProcedure
	 * 
	 * @param jo
	 * @return the jobId of the job launched by runProcedure
	 * @throws IOException 
	 */
	public static String callRunProcedure(JSONObject jo) throws IOException {
		//CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpClient httpClient = new DefaultHttpClient();
		JSONObject result = null;

		try {
			props = getProperties();
			String encoding = new String(
					org.apache.commons.codec.binary.Base64
							.encodeBase64(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(props
									.getProperty(StringConstants.COMMANDER_USER)
									+ ":"
									+ props.getProperty(StringConstants.COMMANDER_PASSWORD))));
			HttpPost httpPostRequest = new HttpPost("http://"
					+ props.getProperty(StringConstants.COMMANDER_SERVER)
					+ ":8000/rest/v1.0/jobs?request=runProcedure");
			StringEntity input = new StringEntity(jo.toString());
			input.setContentType("application/json");
			httpPostRequest.setEntity(input);
			httpPostRequest.setHeader("Authorization", "Basic " + encoding);
			HttpResponse httpResponse = httpClient.execute(httpPostRequest);
			result = new JSONObject(EntityUtils.toString(httpResponse
					.getEntity()));
			return result.getString("jobId");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			//httpClient.close();
			 httpClient.getConnectionManager().shutdown();
		}

	}

	/**
	 * waitForJob: Waits for job to be completed and reports outcome
	 * 
	 * @param jobId
	 * @return outcome of job
	 */
	static String waitForJob(String jobId, long jobTimeOutMillis)
			throws Exception {
		long timeTaken = 0;
		String url = "http://"
				+ props.getProperty(StringConstants.COMMANDER_SERVER)
				+ ":8000/rest/v1.0/jobs/" + jobId + "?request=getJobStatus";
		JSONObject jsonObject = performHTTPGet(url);

		while (!jsonObject.getString("status").equalsIgnoreCase("completed")) {
			Thread.sleep(jobStatusPollIntervalMillis);
			jsonObject = performHTTPGet(url);
			timeTaken += jobStatusPollIntervalMillis;
			if (timeTaken > jobTimeOutMillis) {
				throw new Exception("Job did not completed within time.");
			}
		}
		return jsonObject.getString("outcome");
	}

	/**
	 * waitForJob: Waits for job to be completed and reports outcome
	 * 
	 * @param jobId
	 * @return outcome of job
	 */
	static String getJobStatus(String jobId) throws IOException, JSONException {
		//CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpClient httpClient = new DefaultHttpClient();
		String output = "";
		String encoding = new String(
				org.apache.commons.codec.binary.Base64
						.encodeBase64(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(props
								.getProperty(StringConstants.COMMANDER_USER)
								+ ":"
								+ props.getProperty(StringConstants.COMMANDER_PASSWORD))));
		HttpGet httpGetRequest = new HttpGet("http://"
				+ props.getProperty(StringConstants.COMMANDER_SERVER)
				+ ":8000/rest/v1.0/jobs/" + jobId + "?request=getJobDetails");
		httpGetRequest.setHeader("Authorization", "Basic " + encoding);
		try {
			HttpResponse httpResponse = httpClient.execute(httpGetRequest);
			if (httpResponse.getStatusLine().getStatusCode() >= 400) {
				throw new RuntimeException("HTTP GET failed with "
						+ httpResponse.getStatusLine().getStatusCode() + "-"
						+ httpResponse.getStatusLine().getReasonPhrase());
			}

			output = new JSONObject(EntityUtils.toString(httpResponse
					.getEntity())).getJSONObject("job").getJSONArray("jobStep")
					.getJSONObject(0).getJSONObject("propertySheet")
					.getJSONArray("property").getJSONObject(1)
					.getString("value");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//httpClient.close();
			 httpClient.getConnectionManager().shutdown();
		}

		return output;
	}

	static String getSubstring(String string, String regex) {

		String substring = null;

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(string);
		if (matcher.find()) {
			substring = matcher.group(1);
		}
		return substring;
	}

	/**
	 * Wrapper around a HTTP GET to a REST service
	 * 
	 * @param url
	 * @return JSONObject
	 */
	static JSONObject performHTTPGet(String url) throws IOException,
			JSONException {

		//CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpClient httpClient = new DefaultHttpClient();
		String encoding = new String(
				org.apache.commons.codec.binary.Base64
						.encodeBase64(org.apache.commons.codec.binary.StringUtils.getBytesUtf8(props
								.getProperty(StringConstants.COMMANDER_USER)
								+ ":"
								+ props.getProperty(StringConstants.COMMANDER_PASSWORD))));
		try {
			HttpGet httpGetRequest = new HttpGet(url);
			httpGetRequest.setHeader("Authorization", "Basic " + encoding);
			HttpResponse httpResponse = httpClient.execute(httpGetRequest);
			if (httpResponse.getStatusLine().getStatusCode() >= 400) {
				throw new RuntimeException("HTTP GET failed with "
						+ httpResponse.getStatusLine().getStatusCode() + "-"
						+ httpResponse.getStatusLine().getReasonPhrase());
			}
			return new JSONObject(
					EntityUtils.toString(httpResponse.getEntity()));

		} finally {
			//httpClient.close(); 
			httpClient.getConnectionManager().shutdown();
		}

	}

	public static int getRandomInt() {
		int min = 100;
		int max = 10000;

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	public static void deleteDirectory(File file) throws IOException {

		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);
					// recursive delete
					deleteDirectory(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}

		} else {
			// if file, then delete it
			file.delete();
		}
	}

	public static void setUpBeforeClass(String testName) throws Exception {
		// configurations is a HashMap having primary key as object type(client,
		// node, data bag)
		// and secondary key as property name
		ConfigurationsParser.parseConfigurations();
		Properties props = getProperties();
		TestUtils.createCommanderWorkspace(StringConstants.WORKSPACE_NAME);
		TestUtils.createCommanderResource(StringConstants.RESOURCE_NAME,
				StringConstants.WORKSPACE_NAME,
				props.getProperty(StringConstants.EC_AGENT_IP));
		TestUtils.setResourceAndWorkspace(StringConstants.RESOURCE_NAME,
				StringConstants.WORKSPACE_NAME);
	}

	public static void test(String procedureAbbrevation) throws Exception {
		// This HashMap will be populated by reading configurations.json file
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("projectName", "EC-Azure-"
				+ StringConstants.PLUGIN_VERSION);
		if (!ConfigurationsParser.actions.containsKey(procedureAbbrevation)) {
			System.out.println("Configurations not present for the test");
			return;
		}
		for (Map.Entry<String, HashMap<String, HashMap<String, String>>> objectCursor : ConfigurationsParser.actions
				.get(procedureAbbrevation).entrySet()) {

			String procedureName = procedureAbbrevation + objectCursor.getKey();
			jsonObject.put("procedureName",
					procedureName);

			System.out.println("Inside "+procedureName+" Unit Test");

			for (Map.Entry<String, HashMap<String, String>> runCursor : objectCursor
					.getValue().entrySet()) {

				// Every run will be a new job
				JSONArray actualParameterArray = new JSONArray();
				for (Map.Entry<String, String> propertyCursor : runCursor
						.getValue().entrySet()) {

					// Get each Run's data and iterate over it to populate
					// parameter array

					if (propertyCursor != null
							&& !propertyCursor.getValue().isEmpty()) {
						actualParameterArray
								.put(new JSONObject().put("value",
										propertyCursor.getValue()).put(
										"actualParameterName",
										propertyCursor.getKey()));
						
						populateVariables(propertyCursor.getKey(), propertyCursor.getValue());
						
					}
				}

				jsonObject.put("actualParameter", actualParameterArray);

				String jobId = TestUtils.callRunProcedure(jsonObject);
				String response = waitForJob(jobId,
						StringConstants.jobTimeoutMillis);

				// Check job status
				assertEquals("Job completed with errors", "success", response);
				System.out.println("JobId: " + jobId
						+ ",\nCompleted "+procedureName +" Unit Test Successfully.");
				
			}
		}
	}
	
	public static void populateVariables(String key, String value)
	{
		if (key.contains(StringConstants.SERVER_NAME)) {
			serverName = value;
		} else if (key.contains(StringConstants.VM_NAME)) {
			vmName = value;
		}else if (key.contains(StringConstants.RGN)) {
			rgName = value;
		} else if (key.equals(StringConstants.VNET)||key.equals(StringConstants.VNET_NAME)) {
			vnetName = value;
		} else if (key.equals(StringConstants.SUBNET)||key.equals(StringConstants.SUBNET_NAME)) {
			subnetName = value;
		} else if (key.contains(StringConstants.NSG)) {
			nsgName = value;
		} else if (key.contains(StringConstants.NSR)) {
			nsrName = value;
		} else if (key.contains(StringConstants.DB_NAME)) {
			dbName = value;
		}
	}

}
