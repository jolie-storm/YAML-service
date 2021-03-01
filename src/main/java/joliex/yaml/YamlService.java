package joliex.yaml;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import jolie.runtime.FaultException;
import jolie.runtime.JavaService;
import jolie.runtime.Value;
import jolie.runtime.embedding.RequestResponse;

import java.io.IOException;

import java.util.LinkedList;



public class YamlService extends JavaService {


    // Jolie type of error
    private static final String YAMLERROR = "YamlError";

    // Jolie field message name
    private static final String MSG = "msg";

    // Name of the dummy node used to set an array inside an array
    private static final String DUMMY_NODE = "_";

    // Error Messages
    private static final String UNABLE_TO_CREATE_YAML_FACTORY = "Unable to create a Yaml factory to analise yaml file";
    private static final String UNABLE_GET_FIRST_TOKEN = "Unable to get the first token from file.";

    private static final String EXPECTED_VALUES = "Expected START_OBJECT/START_ARRAY/VALUE_xx, found: ";


    private static LinkedList<YamlJolieObject> linkedListYamlObject;


    @RequestResponse
    public Value yamlToValue(Value request) throws Exception {

        linkedListYamlObject = new LinkedList<>();
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
            YamlJolieObject yamlJolieObject = new YamlJolieObject("root" , response);
            linkedListYamlObject.add(yamlJolieObject);
            parseYamlObject(response, parser, null);
            linkedListYamlObject.clear();

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
    private void parseYamlObject(Value response, YAMLParser parser, String nameNode) throws Exception {
        JsonToken token = parser.nextToken();
        if (token != null) {

            switch (token) {
                case END_OBJECT:
                    if (linkedListYamlObject.size()>1) {
                        YamlJolieObject yamlJolieObjectList = linkedListYamlObject.get(linkedListYamlObject.size() - 1);
                        YamlJolieObject yamlJolieObjectList1 = linkedListYamlObject.get(linkedListYamlObject.size() - 2);
                        response = yamlJolieObjectList1.value;
                        response.getChildren(yamlJolieObjectList.nodeName).add(yamlJolieObjectList.value);
                        linkedListYamlObject.remove(linkedListYamlObject.size() - 1);
                    }
                    break;
                case START_ARRAY:
                    nameNode = parser.getCurrentName();
                    break;
                case END_ARRAY:
                    nameNode = null;
                    break;
                case START_OBJECT:
                    Value objectValue = Value.create();
                    YamlJolieObject yamlJolieObject = new YamlJolieObject(parser.getCurrentName(), objectValue);
                    linkedListYamlObject.add(yamlJolieObject);
                    parseYamlObject(objectValue, parser, null);
                    break;
                case VALUE_STRING:
                    if (nameNode == null) {
                        response.getChildren(parser.getCurrentName()).add(Value.create(parser.getValueAsString()));

                    } else if ((nameNode != null)) {
                        response.getChildren(nameNode).add(Value.create(parser.getValueAsString()));

                    }
                    break;
                case VALUE_FALSE:
                case VALUE_TRUE:
                    if (nameNode == null) {
                        response.getChildren(parser.getCurrentName()).add(Value.create(parser.getBooleanValue()));

                    } else if ((nameNode != null) && (parser.getCurrentName() == null)) {
                        response.getChildren(nameNode).add(Value.create(parser.getBooleanValue()));

                    }
                    break;
                case VALUE_NULL:
                    if (nameNode == null) {
                        response.getChildren(parser.getCurrentName()).add(Value.create(Value.create()));

                    } else if ((nameNode != null) && (parser.getCurrentName() == null)) {
                        response.getChildren(nameNode).add(Value.create(Value.create()));
                    }
                    break;
                case VALUE_NUMBER_FLOAT:
                    if (nameNode == null) {
                        response.getChildren(parser.getCurrentName()).add(Value.create(parser.getDoubleValue()));

                    } else if ((nameNode != null) && (parser.getCurrentName() == null)) {
                        response.getChildren(nameNode).add(Value.create(Value.create(parser.getDoubleValue())));

                    }
                    break;
                case VALUE_NUMBER_INT:
                    if (nameNode == null) {
                        response.getChildren(parser.getCurrentName()).add(Value.create(parser.getLongValue()));

                    } else if ((nameNode != null) && (parser.getCurrentName() == null)) {
                        response.getChildren(nameNode).add(Value.create(Value.create(parser.getLongValue())));
                    }
                    break;
                case FIELD_NAME:
                    break;
                default:
                    Value faultMessage = Value.create();
                    faultMessage.getNewChild(MSG).setValue(EXPECTED_VALUES + token.toString());
                    throw new FaultException(YAMLERROR, faultMessage);
            }


            if (nameNode == null) {
                parseYamlObject(response, parser, null);
            } else {
                parseYamlObject(response, parser, nameNode);
            }
        }


    }
    


    private class YamlJolieObject{


        private String nodeName;
        private Value value;

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }



        public YamlJolieObject (String nodeName , Value value){
            this.nodeName = nodeName;
            this.value = value;
        }

    }

}
