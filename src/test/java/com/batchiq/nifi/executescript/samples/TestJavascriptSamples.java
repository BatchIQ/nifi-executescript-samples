/*
 * Copyright 2016 BatchIQ
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
package com.batchiq.nifi.executescript.samples;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.nifi.components.state.Scope;
import org.apache.nifi.components.state.StateManager;
import org.apache.nifi.components.state.StateMap;
import org.apache.nifi.processors.script.ExecuteScript;
import org.apache.nifi.properties.NiFiPropertiesLoader;
import org.apache.nifi.util.LogMessage;
import org.apache.nifi.util.MockComponentLog;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.NiFiProperties;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestJavascriptSamples extends BaseScriptTest {

    @Before
    public void setup() throws Exception {
        super.setupExecuteScript();
    }

    /**
     * Demonstrates logging from within scripts
     * @throws Exception
     */
    @Test
    public void testAttributes() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(ExecuteScript.SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ExecuteScript.SCRIPT_FILE, "target/test/resources/javascript/attributes.js");
        runner.setProperty(ExecuteScript.MODULES, "target/test/resources/javascript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("greeting", "Hello");
        runner.enqueue("nothing".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        result.assertAttributeEquals("message", "Hello, Script!");
        result.assertAttributeEquals("attribute.one", "true");
        result.assertAttributeEquals("attribute.two", "2");
    }

    /**
     * Demonstrates logging from within scripts
     * @throws Exception
     */
    @Test
    public void testLog() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(ExecuteScript.SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ExecuteScript.SCRIPT_FILE, "target/test/resources/javascript/log.js");
        runner.setProperty(ExecuteScript.MODULES, "target/test/resources/javascript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("greeting", "Hello");
        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);

        MockComponentLog log = runner.getLogger();

        List<LogMessage> debugMessages = log.getDebugMessages();
        Assert.assertEquals(1, debugMessages.size());
        Assert.assertTrue(debugMessages.get(0).getMsg().contains("Hello, Debug"));

        List<LogMessage> infoMessages = log.getInfoMessages();
        Assert.assertEquals(1, infoMessages.size());
        Assert.assertTrue(infoMessages.get(0).getMsg().contains("Hello, Info"));

        List<LogMessage> warnMessages = log.getWarnMessages();
        Assert.assertEquals(1, warnMessages.size());
        Assert.assertTrue(warnMessages.get(0).getMsg().contains("Hello, Warn"));

        List<LogMessage> errorMessages = log.getErrorMessages();
        Assert.assertEquals(1, errorMessages.size());
        Assert.assertTrue(errorMessages.get(0).getMsg().contains("Hello, Error"));
    }


    private static class InputObject {
        public int value;
    }

    private static class OutputObject {
        public int value;
        public String message;
    }

    /**
     * Demonstrates transforming the JSON object in an incoming FlowFile to output
     * @throws Exception
     */
    @Test
    public void testTransform() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(ExecuteScript.SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ExecuteScript.SCRIPT_FILE, "target/test/resources/javascript/transform.js");
        runner.setProperty(ExecuteScript.MODULES, "target/test/resources/javascript");
        runner.assertValid();

        InputObject inputJsonObject = new InputObject();
        inputJsonObject.value = 3;
        ObjectMapper mapper = new ObjectMapper();
        byte[] jsonBytes = mapper.writeValueAsBytes(inputJsonObject);

        runner.enqueue(jsonBytes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        byte[] flowFileBytes = result.toByteArray();

        OutputObject outputJsonObject = mapper.readValue(flowFileBytes, OutputObject.class);
        Assert.assertEquals(9, outputJsonObject.value);
        Assert.assertEquals("Hello", outputJsonObject.message);
    }


    /**
     * Demonstrates splitting an array in a single incoming FlowFile into multiple output FlowFiles
     * @throws Exception
     */
    @Test
    public void testSplit() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(ExecuteScript.SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ExecuteScript.SCRIPT_FILE, "target/test/resources/javascript/split.js");
        runner.setProperty(ExecuteScript.MODULES, "target/test/resources/javascript");
        runner.assertValid();

        String inputContent = "[";
        inputContent += "{ \"color\": \"blue\" },";
        inputContent += "{ \"color\": \"green\" },";
        inputContent += "{ \"color\": \"red\" }";
        inputContent += "]";
        runner.enqueue(inputContent.getBytes(StandardCharsets.UTF_8));
        runner.run();

        MockComponentLog log = runner.getLogger();
        List<LogMessage> infoMessages = log.getInfoMessages();

        runner.assertAllFlowFilesTransferred("success", 3);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile blueFlowFile = successFlowFiles.get(0);
        blueFlowFile.assertAttributeEquals("color", "blue");
        MockFlowFile greenFlowFile = successFlowFiles.get(1);
        greenFlowFile.assertAttributeEquals("color", "green");
        MockFlowFile redFlowFile = successFlowFiles.get(2);
        redFlowFile.assertAttributeEquals("color", "red");
    }

    /**
     * Demonstrates writing to counters
     * @throws Exception
     */
    @Test
    public void testCounter() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(ExecuteScript.SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ExecuteScript.SCRIPT_FILE, "target/test/resources/javascript/counter.js");
        runner.setProperty(ExecuteScript.MODULES, "target/test/resources/javascript");
        runner.assertValid();

        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8));
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        double counterValue = runner.getCounterValue("SampleScriptCounter");
        Assert.assertEquals(1d, counterValue, 0.01d);
    }

    /**
     * Demonstrates reading values from nifi.properties
     * @throws Exception
     */
    @Test
    public void testProperties() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        System.setProperty(NiFiProperties.PROPERTIES_FILE_PATH, "target/test/resources/conf/nifi.properties");
        NiFiPropertiesLoader nifiPropertiesLoader = new NiFiPropertiesLoader();
        NiFiProperties nifiProperties = nifiPropertiesLoader.get();

        runner.setValidateExpressionUsage(false);
        runner.setProperty(ExecuteScript.SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ExecuteScript.SCRIPT_FILE, "target/test/resources/javascript/properties.js");
        runner.setProperty(ExecuteScript.MODULES, "target/test/resources/javascript");
        runner.assertValid();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("property-name", "nifi.version");
        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8), attributes);
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        final List<MockFlowFile> successFlowFiles = runner.getFlowFilesForRelationship("success");
        MockFlowFile result = successFlowFiles.get(0);
        result.assertAttributeEquals("property-value", nifiProperties.getProperty("nifi.version"));
    }

    /**
     * Demonstrates reading and writing processor state values
     * @throws Exception
     */
    @Test
    public void testState() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(new ExecuteScript());
        runner.setValidateExpressionUsage(false);
        runner.setProperty(ExecuteScript.SCRIPT_ENGINE, "ECMAScript");
        runner.setProperty(ExecuteScript.SCRIPT_FILE, "target/test/resources/javascript/state.js");
        runner.setProperty(ExecuteScript.MODULES, "target/test/resources/javascript");
        runner.assertValid();

        StateManager stateManager = runner.getStateManager();
        stateManager.clear(Scope.CLUSTER);
        Map<String, String> initialStateValues = new HashMap<>();
        initialStateValues.put("some-state", "foo");
        stateManager.setState(initialStateValues, Scope.CLUSTER);

        runner.enqueue("sample text".getBytes(StandardCharsets.UTF_8));
        runner.run();

        runner.assertAllFlowFilesTransferred("success", 1);
        StateMap resultStateValues = stateManager.getState(Scope.CLUSTER);
        Assert.assertEquals("foobar", resultStateValues.get("some-state"));
    }

}
