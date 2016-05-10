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

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ConfigurationsParser {
    public static HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>> actions = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, String>>>>();

    public static void parseConfigurations() {
        try {
            HashMap<String, HashMap<String, HashMap<String, String>>> configurations = new HashMap<String, HashMap<String, HashMap<String, String>>>();
            HashMap<String, HashMap<String, String>> runs;
            HashMap<String, String> runProperties;
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/src/test/java/ecplugins/azure/Configurations.json"));
            String line = null, configuration = "";
            while ((line = reader.readLine()) != null) {
                if (line.contains("*/"))
                {
                    break;
                }
            }
            while ((line = reader.readLine()) != null) {
                configuration += line;   
            }
            reader.close();
            JSONParser jsonParser = new JSONParser();
            JSONObject actionObject = (JSONObject) jsonParser.parse(configuration.toString());

            Iterator<?> keyAction = actionObject.keySet().iterator();
            while (keyAction.hasNext()) {
                String actionKey = (String) keyAction.next();
                if (actionObject.get(actionKey) instanceof JSONObject) {

                    JSONObject configObject = (JSONObject) actionObject
                        .get(actionKey);
                    configurations = new HashMap<String, HashMap<String, HashMap<String, String>>>();
                    Iterator<?> keyConfig = configObject.keySet().iterator();
                    while (keyConfig.hasNext()) {
                        String configKey = (String) keyConfig.next();

                        if (configObject.get(configKey) instanceof JSONArray) {

                            JSONArray runsArray = (JSONArray) configObject
                                .get(configKey);
                            runs = new HashMap<String, HashMap<String, String>>();
                            for (int i = 0; i < runsArray.size(); i++) {
                                JSONObject propertiesObject = (JSONObject) runsArray
                                    .get(i);

                                Iterator<?> keyProperties = propertiesObject
                                    .keySet().iterator();
                                runProperties = new HashMap<String, String>();
                                while (keyProperties.hasNext()) {
                                    String propertiesKey = (String) keyProperties.next();
                                    runProperties.put(propertiesKey,
                                            (String) propertiesObject
                                            .get(propertiesKey));
                                }
                                runs.put("run" + i, runProperties);
                            }
                            configurations.put(configKey, runs);

                        }
                    }
                }
                actions.put(actionKey, configurations);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
