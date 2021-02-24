package joliex.yaml;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import jolie.runtime.FaultException;
import jolie.runtime.JavaService;
import jolie.runtime.Value;
import jolie.runtime.embedding.RequestResponse;

import java.io.IOException;

public class YamlService extends JavaService {
    private static final String CHILDYAML = "yaml";

    // Jolie type of error
    private static final String YAMLERROR = "YamlError";

    // Jolie field message name
    private static final String MSG = "msg";

    // Error Messages
    private static final String UNABLE_TO_CREATE_YAML_FACTORY = "Unable to create a Yaml factory to analise yaml file";
    private static final String UNABLE_GET_FIRST_TOKEN = "Unable to get the first token from file.";
    private static final String START_OBJECT_EXPECTED = "Expected to start with an object definition";
    private static final String EXPECTED_ENDOBJECT_FIELDNAME = "Expected END_OBJECT / FIELD_NAME, founded: ";
    private static final String PARSER_CURRENTNAME = "Unable to get the current field name";
    private static final String EXPECTED_VALUES = "Expected START_OBJECT/START_ARRAY/VALUE_xx, found: ";


    @RequestResponse
    public Value yamlToValue(Value request) throws Exception {
        Value response = Value.create();

        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = null;

        try {
            parser = factory.createParser(request.getFirstChild(CHILDYAML).strValue());
        } catch (IOException e) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(UNABLE_TO_CREATE_YAML_FACTORY);
            throw new FaultException(YAMLERROR,faultMessage);
        }

        try {
            JsonToken token = parser.nextToken();

            if (token != JsonToken.START_OBJECT) {
                Value faultMessage = Value.create();
                faultMessage.getNewChild(MSG).setValue(START_OBJECT_EXPECTED);
                throw new FaultException(YAMLERROR,faultMessage);
            }

            setObject (response, parser);

        } catch (IOException e) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(UNABLE_GET_FIRST_TOKEN);
            throw new FaultException(YAMLERROR,faultMessage);
        }


        return response;
    }

    // start of an object
    // fill the response Value, with childs
    // each corresponding with the fields in yaml file
    private void setObject(Value response, YAMLParser parser) throws Exception {
        JsonToken token = parser.nextToken();

        switch (token) {
            case END_OBJECT:
                return;
            case FIELD_NAME:
                // set the new node
                Value newChild = null;

                try {
                    newChild = response.getNewChild(parser.getCurrentName());
                } catch (IOException e) {
                    Value faultMessage = Value.create();
                    faultMessage.getNewChild(MSG).setValue(PARSER_CURRENTNAME);
                    throw new FaultException(YAMLERROR,faultMessage);
                }
                // look ahead for the cases:
                // 1 - simple value - set the value and continue to find other field name
                // 2 - array: need to manage this special case
                // 3 - object: start a new object
                token = parser.nextToken();

                switch (token) {
                    case START_ARRAY:
                        throw new Exception("START_ARRAY not implemented");
                    case START_OBJECT:
                        setObject(newChild,parser);
                        break;
                    case VALUE_STRING:
                    case VALUE_FALSE:
                    case VALUE_NULL:
                    case VALUE_TRUE:
                    case VALUE_NUMBER_FLOAT:
                    case VALUE_NUMBER_INT:
                        setChildValue(newChild, token, parser);
                        break;
                    default:
                        Value faultMessage = Value.create();
                        faultMessage.getNewChild(MSG).setValue(EXPECTED_VALUES + token.toString());
                        throw new FaultException(YAMLERROR,faultMessage);
                }
                break;
            default:
                Value faultMessage = Value.create();
                faultMessage.getNewChild(MSG).setValue(EXPECTED_ENDOBJECT_FIELDNAME + token.toString());
                throw new FaultException(YAMLERROR,faultMessage);

        }
    }

    private void setChildValue(Value newChild, JsonToken token, YAMLParser parser) throws IOException {
        switch (token) {
            case VALUE_STRING:
                newChild.setValue(parser.getValueAsString());
                break;
            case VALUE_FALSE:
            case VALUE_TRUE:
                newChild.setValue(parser.getBooleanValue());
                break;
            case VALUE_NULL:
                newChild.setValue((Object)null);
                break;
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
                newChild.setValue(parser.getDecimalValue());
        }
    }


}
