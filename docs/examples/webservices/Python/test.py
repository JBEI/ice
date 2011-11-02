from RegistryAPIService_client import RegistryAPIServiceLocator, login
from RegistryAPIService_types import ns0

import sys

def testTraceSequenceUpload(sessionId):
    request = getByPartNumber()
    request._sessionId = sessionId
    request._partNumber = "JBx_000902"

    response = portType.getByPartNumber(request)

    recordId = response.Return.RecordId

    list_request = listTraceSequenceFiles()
    list_request._sessionId = sessionId
    list_request._recordId = recordId

    response = portType.listTraceSequenceFiles(list_request)

    print response.Return
    
    # new file
    fileName = "RDR008_RR01_2010-07-24_G10.ab1"
    fh = open(fileName)
    fileData = fh.read().encode("base64")
    
    request = uploadTraceSequenceFile()
    request._sessionId = sessionId
    request._recordId = recordId
    request._fileName = fileName
    request._base64FileData = fileData

    response = portType.uploadTraceSequenceFile(request)

    newFileId = response.Return

    print newFileId

    response = portType.listTraceSequenceFiles(list_request)
    print response.Return
    
    
    delete_request = deleteTraceSequenceFile()
    delete_request._sessionId = sessionId
    delete_request._fileId = newFileId

    response = portType.deleteTraceSequenceFile(delete_request)
    print portType.listTraceSequenceFiles(list_request).Return
    
    response = portType.listTraceSequenceFiles(list_request)
    print response.Return

if __name__ == "__main__":
    registryAPIServiceLocator = RegistryAPIServiceLocator()
    
    kw = { 'tracefile' : sys.stdout }
    portType = registryAPIServiceLocator.getRegistryAPIPort(**kw)
    
    request = login()
    request._login = 'YOUR_USER_NAME_GOES_HERE'
    request._password = 'YOUR_PASSWORD_GOES_HERE'
    
    response = portType.login(request)
    print response.Return
    