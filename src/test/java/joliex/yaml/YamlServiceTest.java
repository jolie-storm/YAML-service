package joliex.yaml;

import jolie.runtime.Value;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class YamlServiceTest extends TestCase {

    public void testYamlToValue() {
        Value testValue = Value.create();


        try {

            BufferedReader rd = new BufferedReader(new FileReader("webserver.yaml"));
            String inputLine = null;
            StringBuilder builder = new StringBuilder();

            //Store the contents of the file to the StringBuilder.
            while ((inputLine = rd.readLine()) != null)
                builder.append(inputLine);


            testValue.getNewChild("yaml").setValue(builder.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        YamlService.yamlToValue(testValue);

    }
}