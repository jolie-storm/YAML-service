include "console.iol"
include "string_utils.iol"
include "file.iol"

include "YamlService.iol"


main {
    readFile@File({.filename="../src/test/resources/testNestedMap.yaml"})(rsfile);
    println@Console(rsfile)()
    ;
    yamlToValue@YamlToValuePort({.yaml = rsfile})(yamlObj)
    ;
    valueToPrettyString@StringUtils(yamlObj)(s)
    ;
    println@Console(s)()
}
