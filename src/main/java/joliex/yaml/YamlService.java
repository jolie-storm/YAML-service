package joliex.yaml;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import jolie.runtime.JavaService;
import jolie.runtime.Value;
import jolie.runtime.embedding.RequestResponse;

import java.io.IOException;

public class YamlService extends JavaService {
    
    @RequestResponse
    public static Value yamlToValue(Value request) {
        Value response = Value.create();

          /* req.node.node2 = "ciao"
          request.getFirstChild("node1").getFirstChild("node2").strValue()*/
        YAMLFactory factory = new YAMLFactory();

        try {

            YAMLParser parser = factory.createParser(request.getFirstChild("yaml").strValue());
            parser.nextToken();
            buildJolieCustomData(response, parser , "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private static void buildJolieCustomData(Value response, YAMLParser parser , String nodeName) throws IOException {
        JsonToken token = parser.nextToken();

        switch (token) {
            case FIELD_NAME:
                System.out.println("FIELD_NAME " +parser.getCurrentName());
                buildJolieCustomData(response, parser , parser.getCurrentName());
                break;
            case VALUE_STRING:
                 System.out.println("VALUE_STRING " + parser.getValueAsString());
                 response.getNewChild(nodeName).setValue(token.asString());
                 break;
            case END_ARRAY:
                break;
            case END_OBJECT:
                break;
            case START_OBJECT:
                break;

        }
    }


}
