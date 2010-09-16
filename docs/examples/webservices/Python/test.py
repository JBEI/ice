from RegistryAPIService_client import RegistryAPIServiceLocator, login
from RegistryAPIService_types import ns0

import sys

if __name__ == "__main__":
    registryAPIServiceLocator = RegistryAPIServiceLocator()
    
    kw = { 'tracefile' : sys.stdout }
    portType = registryAPIServiceLocator.getRegistryAPIPort(**kw)
    
    request = login()
    request._login = 'YOUR_USER_NAME_GOES_HERE'
    request._password = 'YOUR_PASSWORD_GOES_HERE'
    
    response = portType.login(request)
    print response.Return
    