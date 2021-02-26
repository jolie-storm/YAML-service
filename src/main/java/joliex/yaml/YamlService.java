package joliex.yaml;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import jolie.runtime.FaultException;
import jolie.runtime.JavaService;
import jolie.runtime.Value;
import jolie.runtime.embedding.RequestResponse;

import javax.swing.text.html.parser.Parser;
import java.io.IOException;
import java.util.regex.Pattern;

public class YamlService extends JavaService {

    private static final String CHILDYAML = "yaml";

    // Jolie type of error
    private static final String YAMLERROR = "YamlError";

    // Jolie field message name
    private static final String MSG = "msg";

    // Name of the dummy node used to set an array inside an array
    private static final String DUMMY_NODE = "_";

    // Error Messages
    private static final String UNABLE_TO_CREATE_YAML_FACTORY = "Unable to create a Yaml factory to analise yaml file";
    private static final String UNABLE_GET_FIRST_TOKEN = "Unable to get the first token from file.";
    private static final String START_OBJECT_EXPECTED = "Expected to start with an object definition";
    private static final String EXPECTED_ENDOBJECT_FIELDNAME = "Expected END_OBJECT / FIELD_NAME, founded: ";
    private static final String PARSER_CURRENTNAME = "Unable to get the current field name";
    private static final String EXPECTED_VALUES = "Expected START_OBJECT/START_ARRAY/VALUE_xx, found: ";
    private static final String EXPECTED_VALUES_SETARRAY = "Expected START_OBJECT/START_ARRAY/VALUE_xx/END_ARRAY, found: ";
    private static final String UNABLE_TOREAD_TOKEN = "Unable to read the next token (IOExecption)";


    @RequestResponse
    public Value yamlToValue(Value request) throws Exception {
        System.out.println("sono qua");
        Value response = Value.create();

        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = null;

        try {
            parser = factory.createParser(request.getFirstChild("yamlContent").strValue());
        } catch (IOException e) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(UNABLE_TO_CREATE_YAML_FACTORY);
            throw new FaultException(YAMLERROR, faultMessage);
        }

        try {
            JsonToken token = parser.nextToken();

            if (token != JsonToken.START_OBJECT) {
                Value faultMessage = Value.create();
                faultMessage.getNewChild(MSG).setValue(START_OBJECT_EXPECTED);
                throw new FaultException(YAMLERROR, faultMessage);
            }

            parseYamlObject(response, parser);

        } catch (IOException e) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(UNABLE_GET_FIRST_TOKEN);
            throw new FaultException(YAMLERROR, faultMessage);
        }

        return response;
    }

    // start of an object
    // fill the response Value, with childs
    // each corresponding with the fields in yaml file
    private void parseYamlObject(Value response, YAMLParser parser) throws Exception {
        JsonToken token = parser.nextToken();

        switch (token) {
            case END_OBJECT:
                return;
            case START_ARRAY:
                break;
            case START_OBJECT:
                //setObject(newChild, parser);
                break;
            case VALUE_STRING:
                response.getChildren(parser.getCurrentName()).add(Value.create(parser.getValueAsString()));
                break;
            case VALUE_FALSE:
            case VALUE_TRUE:
                response.getChildren(parser.getCurrentName()).add(Value.create(parser.getBooleanValue()));
                break;
            case VALUE_NULL:
                response.getChildren(parser.getCurrentName()).add(Value.create());
                break;
            case VALUE_NUMBER_FLOAT:
                response.getChildren(parser.getCurrentName()).add(Value.create(parser.getDoubleValue()));
                break;
            case VALUE_NUMBER_INT:
                response.getChildren(parser.getCurrentName()).add(Value.create(parser.getLongValue()));
                break;
            case FIELD_NAME:
                break;
            default:
                Value faultMessage = Value.create();
                faultMessage.getNewChild(MSG).setValue(EXPECTED_VALUES + token.toString());
                throw new FaultException(YAMLERROR, faultMessage);
        }

        parseYamlObject(response,parser);


}

    private void setArray(Value response , YAMLParser parser , String nameField) throws Exception {


        JsonToken token = parser.nextToken();
        switch (token) {
            case END_OBJECT:
                return;
            case END_ARRAY:
                break;
            case START_OBJECT:
                //setObject(newChild, parser);
                break;
            case VALUE_STRING:
                response.getChildren(parser.getCurrentName()).add(Value.create(parser.getValueAsString()));
                break;
            case VALUE_FALSE:
            case VALUE_TRUE:
                response.getChildren(parser.getCurrentName()).add(Value.create(parser.getBooleanValue()));
                break;
            case VALUE_NULL:
                response.getChildren(parser.getCurrentName()).add(Value.create());
                break;
            case VALUE_NUMBER_FLOAT:
                response.getChildren(parser.getCurrentName()).add(Value.create(parser.getDoubleValue()));
                break;
            case VALUE_NUMBER_INT:
                response.getChildren(parser.getCurrentName()).add(Value.create(parser.getLongValue()));
                break;
            case FIELD_NAME:
                break;
            default:
                Value faultMessage = Value.create();
                faultMessage.getNewChild(MSG).setValue(EXPECTED_VALUES + token.toString());
                throw new FaultException(YAMLERROR, faultMessage);
        }

    }
}
