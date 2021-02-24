package joliex.yaml;

import jolie.runtime.FaultException;
import jolie.runtime.Value;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


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
    }
}