Requirements:
- Flex 4 SDK
- Adobe Flash Builder 4

To compile:
1. Open project in Adobe Flash Builder 4
2. Go to menu Data->Connect to WebService ...
3. Enter WSDL URI: https://registry.jbei.org/api/RegistryAPI?wsdl and press Finish; it will generate required classes for you
4. Run or Debug

Because WSDL might be updated so your generated classes might not work anymore or be outdated so try to remove all generated classes (services/ and valueObjects/ folders) and try to generate classes again. 