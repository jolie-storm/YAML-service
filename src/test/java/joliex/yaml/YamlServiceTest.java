package joliex.yaml;

import jolie.runtime.Value;
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
        testValue.getNewChild("yaml").setValue(getFileContent(testName.getMethodName()));

        Value response = yamlService.yamlToValue(testValue);

        Value node = response.getFirstChild("key");
        Assert.assertNotNull("key child not found" , node);
        Assert.assertTrue("expected value found "+ node.strValue(),"value".equals(node.strValue()));

        node = response.getFirstChild("another_key");
        Assert.assertNotNull("another_key child not found" , node);
        Assert.assertTrue("expected 'Another value goes here.' found "+ node.strValue(),"Another value goes here.".equals(node.strValue()));

        node = response.getFirstChild("a_number_value");
        Assert.assertNotNull("a_number_value child not found" , node);
        Assert.assertTrue("expected 100 found: "+ node.strValue(),"100".equals(node.strValue()));

        node = response.getFirstChild("scientific_notation");
        Assert.assertNotNull("scientific_notation child not found" , node);

        BigDecimal expectedValue = new BigDecimal("1e+12");
        BigDecimal actualValue = new BigDecimal(node.strValue());
        Assert.assertTrue("expected 1e+12 found: "+ node.strValue(), expectedValue.compareTo(actualValue) == 0);

        node = response.getFirstChild("boolean");
        Assert.assertNotNull("boolean child not found" , node);
        Assert.assertTrue("expected true found: "+ node.strValue(), node.boolValue());

        node = response.getFirstChild("null_value");
        Assert.assertNotNull("null_value child not found" , node);
        Assert.assertNull("expected null found: "+ node.strValue(),node.valueObject());

        node = response.getFirstChild("key with spaces");
        Assert.assertNotNull("'key with spaces' child not found" , node);
        Assert.assertTrue("expected value found: "+ node.strValue(),"value".equals(node.strValue()));

        node = response.getFirstChild("Keys can be quoted too.");
        Assert.assertNotNull("'Keys can be quoted too.' child not found" , node);
        Assert.assertTrue("expected \"Useful if you want to put a ':' in your key.\" found: "+ node.strValue(),"Useful if you want to put a ':' in your key.".equals(node.strValue()));

        node = response.getFirstChild("single quotes");
        Assert.assertNotNull("\"single quotes\" child not found" , node);
        Assert.assertTrue("expected \"'have ''one'' escape pattern'\" found: "+ node.strValue(),"have 'one' escape pattern".equals(node.strValue()));

        node = response.getFirstChild("double quotes");
        Assert.assertNotNull("\"double quotes\" child not found" , node);
        byte[] stringBytes = new String ("have many: \", \0, \t, \u263A, \r\n == \r\n, and more.").getBytes(StandardCharsets.UTF_8);
        String expectedStringValue = new String(stringBytes, StandardCharsets.UTF_8);
        Assert.assertTrue("expected \"have many: \\\", \\0, \\t, \\u263A, \\x0d\\x0a == \\r\\n, and more.\" found: "+ node.strValue(),expectedStringValue.equals(node.strValue()));
    }

    @Test
    public void testNestedMap() throws Exception {
        Value testValue = Value.create();
        testValue.getNewChild("yaml").setValue(getFileContent(testName.getMethodName()));

        Value response = yamlService.yamlToValue(testValue);

        Value node = response.getFirstChild("0.25");
        Assert.assertNotNull("key 0.25 not found" , node);
        Assert.assertTrue("expected 'a float key', found "+ node.strValue(),"a float key".equals(node.strValue()));

        node = response.getFirstChild("This is a key_" +
                "  that has multiple lines_");
        Assert.assertNotNull("key 'This is a key_" +
                "  that has multiple lines_' not found" , node);
 //       Assert.assertTrue("expected 'and this is its value', found "+ node.strValue(),"and this is its value".equals(node.strValue()));

    }

}