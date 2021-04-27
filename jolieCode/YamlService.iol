type YamlToValueRequest: void {
    .yaml : string
}

type YamlToValueResponse : undefined

interface YamlServiceInterface {
    RequestResponse:
        yamlToValue(YamlToValueRequest)(YamlToValueResponse)
}

outputPort YamlToValuePort {
  Interfaces: YamlServiceInterface
}

embedded {
Java:
	"joliex.yaml.YamlService" in YamlToValuePort
}