In order to make webservices calls from Python you have to install few libraries.

1. Install python-zsi module
$ sudo apt-get install python-zsi

2. Make sure that path /usr/share/pyshared in your PYTHON_PATH

3. Open this url https://registry.jbei.org/api/RegistryAPI?wsdl with your browser and save it to RegistryAPI.wsdl file

4. Run wsdl2py -b RegistryAPI.wsdl

5. Update login and password values with your valid JBEI registry credentials in test.py

6. Run python test.py
