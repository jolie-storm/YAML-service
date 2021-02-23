package joliex.yaml;

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

            YAMLParser parse = factory.createParser(request.getFirstChild("yaml").strValue());
            System.out.println(parse.getCurrentName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }


}
