package joliex.yaml;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import jolie.runtime.FaultException;
import jolie.runtime.JavaService;
import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import jolie.runtime.embedding.RequestResponse;

import java.io.IOException;

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
    private static final String START_OBJECT_EXPECTED = "Expected to start with an object definition";
    private static final String EXPECTED_ENDOBJECT_FIELDNAME = "Expected END_OBJECT / FIELD_NAME, founded: ";
    private static final String PARSER_CURRENTNAME = "Unable to get the current field name";

    // Parse related errors
    private static final String EXPECTED_FIELD_NAME = "Expected FIELD_NAME token, found: ";
    private static final String EXPECTED_VALUES = "Expected simple value, found: ";

    private static final String EXPECTED_VALUES_SETARRAY = "Expected START_OBJECT/START_ARRAY/VALUE_xx/END_ARRAY, found: ";

    // Parser related errors
    private static final String UNABLE_GET_FIRST_TOKEN = "Unable to get the first token from file.";
    private static final String UNABLE_TOREAD_TOKEN = "Unable to read the next token (IOExecption)";
    private static final String UNABLE_TOGET_TOKEN_STRING = "Unable to get the string associated with the current token";

    private JsonToken getNextToken(YAMLParser parser) throws FaultException {
        JsonToken token = null;

        try {
            token = parser.nextToken();
        } catch (IOException e) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(UNABLE_TOREAD_TOKEN + token.toString());
            throw new FaultException(YAMLERROR, faultMessage);
        }

        return token;
    }

    private String getCurrentName(YAMLParser parser) throws FaultException {
        try {
            return parser.getCurrentName();
        } catch (IOException e) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(UNABLE_TOGET_TOKEN_STRING);
            throw new FaultException(YAMLERROR, faultMessage);
        }
    }

    @RequestResponse
    public Value yamlToValue(Value request) throws Exception {

        YAMLFactory factory = new YAMLFactory();
        YAMLParser parser = null;

        try {
            parser = factory.createParser(request.getFirstChild("yamlContent").strValue());
        } catch (IOException e) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(UNABLE_TO_CREATE_YAML_FACTORY);
            throw new FaultException(YAMLERROR, faultMessage);
        }

        JsonToken token = null;

        try {
            token = parser.nextToken();
        } catch (IOException e) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(UNABLE_GET_FIRST_TOKEN);
            throw new FaultException(YAMLERROR, faultMessage);
        }

        if (token != JsonToken.START_OBJECT) {
            Value faultMessage = Value.create();
            faultMessage.getNewChild(MSG).setValue(START_OBJECT_EXPECTED);
            throw new FaultException(YAMLERROR, faultMessage);
        }

        return parseYamlObject(parser);

    }



    // start of an object
    // fill the response Value, with childs
    // each corresponding with the fields in yaml file

    private Value parseYamlObject(YAMLParser parser) throws FaultException {
        Value response = Value.create();

        JsonToken token = getNextToken(parser);

        while (token != JsonToken.END_OBJECT) {
            // next token must be a field identifier
            if (token == JsonToken.FIELD_NAME) {
                // look ahead : must manage differently array start
                // and simple values or object start definition
                token = getNextToken(parser);

                if (token == JsonToken.START_ARRAY) {
                    // assign array
                    ValueVector valueVector = response.getChildren(getCurrentName(parser));
                    valueVector.deepCopy(parseYamlArray(parser));
                } else if (token == JsonToken.START_OBJECT) {
                    ValueVector valueVector = response.getChildren(getCurrentName(parser));
                    valueVector.add(parseYamlObject(parser));
                    //response.getNewChild(getCurrentName(parser)).assignValue(parseYamlObject(parser));
                } else {
                    response.getNewChild(getCurrentName(parser)).assignValue(parseYamlSimpleValue(token,parser));
                }
            } else {
                Value faultMessage = Value.create();
                faultMessage.getNewChild(MSG).setValue(EXPECTED_FIELD_NAME + token.toString());
                throw new FaultException(YAMLERROR, faultMessage);
            }

            token = getNextToken(parser);
        }

        return response;
    }

    private ValueVector parseYamlArray(YAMLParser parser) {
        return ValueVector.create();
        // TO DO - complete
    }

    private Value parseYamlSimpleValue(JsonToken token,YAMLParser parser) throws FaultException {
        switch (token) {
            case VALUE_STRING:
                try {
                    return Value.create(parser.getValueAsString());
                } catch (IOException e) {
                    Value faultMessage = Value.create();
                    faultMessage.getNewChild(MSG).setValue(UNABLE_TOGET_TOKEN_STRING);
                    throw new FaultException(YAMLERROR, faultMessage);
                }
            case VALUE_FALSE:
            case VALUE_TRUE:
                try {
                    return Value.create(parser.getBooleanValue());
                } catch (IOException e) {
                    Value faultMessage = Value.create();
                    faultMessage.getNewChild(MSG).setValue(UNABLE_TOGET_TOKEN_STRING);
                    throw new FaultException(YAMLERROR, faultMessage);
                }
            case VALUE_NULL:
                return Value.create();
            case VALUE_NUMBER_FLOAT:
                try {
                    return Value.create(parser.getDoubleValue());
                } catch (IOException e) {
                    Value faultMessage = Value.create();
                    faultMessage.getNewChild(MSG).setValue(UNABLE_TOGET_TOKEN_STRING);
                    throw new FaultException(YAMLERROR, faultMessage);
                }
            case VALUE_NUMBER_INT:
                try {
                    return Value.create(parser.getLongValue());
                } catch (IOException e) {
                    Value faultMessage = Value.create();
                    faultMessage.getNewChild(MSG).setValue(UNABLE_TOGET_TOKEN_STRING);
                    throw new FaultException(YAMLERROR, faultMessage);
                }
            default:
                Value faultMessage = Value.create();
                faultMessage.getNewChild(MSG).setValue(EXPECTED_VALUES + token.toString());
                throw new FaultException(YAMLERROR, faultMessage);
        }
    }

    private void setArray(Value response, YAMLParser parser, String nameField) throws Exception {


        JsonToken token = parser.nextToken();

    }
}
