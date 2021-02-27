package joliex.yaml;

import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class YamlServiceTest {
    private static YamlService yamlService;

    private static String readFromInputStream(InputStream inputStream)
            throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                     = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    private static String getFileContent (String fileName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName + ".yaml");

        return readFromInputStream(inputStream);
    }

    @BeforeClass
    public static void setYamlService () {
        yamlService = new YamlService();
    }

    @Rule
    public TestName testName = new TestName();

    @Test
    public void testSimpleFields() throws Exception {
        Value testValue = Value.create();
        testValue.getNewChild("yamlContent").setValue(getFileContent(testName.getMethodName()));

        Value response = yamlService.yamlToValue(testValue);

        Assert.assertTrue("key 'key' not found" , response.hasChildren("key"));

        Value node = response.getFirstChild("key");
        Assert.assertTrue("expected value found "+ node.strValue(),"value".equals(node.strValue()));

        Assert.assertTrue("key 'another_key' not found" , response.hasChildren("another_key"));

        node = response.getFirstChild("another_key");
        Assert.assertTrue("expected 'Another value goes here.' found "+ node.strValue(),"Another value goes here.".equals(node.strValue()));

        Assert.assertTrue("key 'a_number_value' not found" , response.hasChildren("a_number_value"));
        node = response.getFirstChild("a_number_value");
        Assert.assertTrue("expected 100 found: "+ node.strValue(),"100".equals(node.strValue()));

        Assert.assertTrue("key 'scientific_notation' not found" , response.hasChildren("scientific_notation"));
        node = response.getFirstChild("scientific_notation");

        BigDecimal expectedValue = new BigDecimal("1e+12");
        BigDecimal actualValue = new BigDecimal(node.strValue());
        Assert.assertTrue("expected 1e+12 found: "+ node.strValue(), expectedValue.compareTo(actualValue) == 0);

        Assert.assertTrue("key 'boolean' not found" , response.hasChildren("boolean"));
        node = response.getFirstChild("boolean");
        Assert.assertTrue("expected true found: "+ node.strValue(), node.boolValue());

        Assert.assertTrue("key 'null_value' not found" , response.hasChildren("null_value"));
        node = response.getFirstChild("null_value");
        Assert.assertNull("expected null found: "+ node.strValue(),node.valueObject());

        Assert.assertTrue("key 'key with spaces' not found" , response.hasChildren("key with spaces"));
        node = response.getFirstChild("key with spaces");
        Assert.assertTrue("expected value found: "+ node.strValue(),"value".equals(node.strValue()));

        Assert.assertTrue("key 'Keys can be quoted too.' not found" , response.hasChildren("Keys can be quoted too."));
        node = response.getFirstChild("Keys can be quoted too.");
        Assert.assertTrue("expected \"Useful if you want to put a ':' in your key.\" found: "+ node.strValue(),"Useful if you want to put a ':' in your key.".equals(node.strValue()));


        Assert.assertTrue("key 'single quotes' not found" , response.hasChildren("single quotes"));
        node = response.getFirstChild("single quotes");
        Assert.assertTrue("expected \"'have ''one'' escape pattern'\" found: "+ node.strValue(),"have 'one' escape pattern".equals(node.strValue()));

        Assert.assertTrue("key 'double quotes' not found" , response.hasChildren("double quotes"));
        node = response.getFirstChild("double quotes");
        byte[] stringBytes = new String ("have many: \", \0, \t, \u263A, \r\n == \r\n, and more.").getBytes(StandardCharsets.UTF_8);
        String expectedStringValue = new String(stringBytes, StandardCharsets.UTF_8);
        Assert.assertTrue("expected \"have many: \\\", \\0, \\t, \\u263A, \\x0d\\x0a == \\r\\n, and more.\" found: "+ node.strValue(),expectedStringValue.equals(node.strValue()));
    }

    @Test
    public void testNestedMap() throws Exception {
        Value testValue = Value.create();
        testValue.getNewChild("yamlContent").setValue(getFileContent(testName.getMethodName()));

        Value response = yamlService.yamlToValue(testValue);

        Assert.assertTrue("Children expected: no one founded", response.hasChildren());

        Assert.assertTrue("key 0.25 not found" , response.hasChildren("0.25"));
        Value node = response.getFirstChild("0.25");
        Assert.assertTrue("expected 'a float key', found "+ node.strValue(),"a float key".equals(node.strValue()));

        Assert.assertTrue("key 'This is a key\nthat has multiple lines\n' not found" , response.hasChildren("This is a key\n" +
                "that has multiple lines\n"));
        node = response.getFirstChild("This is a key\n" +
                "that has multiple lines\n");
        Assert.assertTrue("expected 'and this is its value', found "+ node.strValue(),"and this is its value".equals(node.strValue()));

        Assert.assertTrue("key 'a_nested_map' not found" , response.hasChildren("a_nested_map"));
        int nrChildren = response.getChildren("a_nested_map").size();
        Assert.assertEquals("a_nested_map: expected 3 children founded "+ nrChildren,nrChildren,3);
        node = response.getFirstChild("a_nested_map");
        Assert.assertTrue("key 'key' not found" , node.hasChildren("key"));
        Assert.assertTrue("key 'another_key' not found" , node.hasChildren("another_key"));
        Assert.assertTrue("key 'another_nested_map' not found" , node.hasChildren("another_nested_map"));

        node = response.getFirstChild("another_nested_map");
        nrChildren = node.getChildren("another_nested_map").size();
        Assert.assertEquals("another_nested_map: expected 1 child, founded: "+nrChildren,nrChildren,1);
        Assert.assertTrue("key 'hello' not found" , node.hasChildren("hello"));

    }

}