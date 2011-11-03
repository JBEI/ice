from RegistryAPIService_client import RegistryAPIServiceLocator, login
from RegistryAPIService_types import ns0

import sys

def testGetEntry(sessionId):
    request = getByPartNumber()
    request._sessionId = sessionId
    request._partNumber = "JBx_000902"

    response = portType.getByPartNumber(request)

    #recordId = dir(response.Return)
    #print dir(response.Return)
    print response.Return.PartNumbers[0].PartNumber
    return response.Return.RecordId

def testlistTraceFiles(sessionId, recordId):
    list_request = listTraceSequenceFiles()
    list_request._sessionId = sessionId
    list_request._recordId = recordId

    response = portType.listTraceSequenceFiles(list_request)
    print dir(response.Return[0])
    print response.Return[0].Filename

def testGetTraceFile(sessionId):
    recordId = testGetEntry(sessionId)

    list_request = listTraceSequenceFiles()
    list_request._sessionId = sessionId
    list_request._recordId = recordId

    response = portType.listTraceSequenceFiles(list_request)

    fileId = response.Return[-1]
    
    print "fileId " + fileId

    request = getTraceSequenceFile()
    request._sessionId = sessionId
    request._fileId = fileId

    response = portType.getTraceSequenceFile(request)
    print dir(response.Return)
    
def testSequenceTraceUpload(sessionId):
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

    ###
    #not_my_file = response.Return[0]
    #
    #request = deleteTraceSequenceFile()
    #request._sessionId = sessionId
    #request._fileId = not_my_file
    #
    #response = portType.deleteTraceSequenceFile(request)

    #print response.Return
    
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

#    response = portType.listTraceSequenceFiles(list_request)
#    print response.Return
    
    
    delete_request = deleteTraceSequenceFile()
    delete_request._sessionId = sessionId
    delete_request._fileId = newFileId

#    response = portType.deleteTraceSequenceFile(delete_request)
#    print portType.listTraceSequenceFiles(list_request).Return
    


if __name__ == "__main__":
    registryAPIServiceLocator = RegistryAPIServiceLocator()
    
    kw = { 'tracefile' : sys.stdout }
    portType = registryAPIServiceLocator.getRegistryAPIPort(**kw)
    
    request = login()
    request._login = 'tsham@lbl.gov'
    request._password = 'YOUR_PASSWORD_GOES_HERE'
    
    response = portType.login(request)
    sessionId = response.Return

    print "sessionID: " + sessionId

    #recordId = testGetEntry(sessionId)
    #testlistTraceFiles(sessionId, recordId)
    #testSequenceTraceUpload(sessionId)
    #testGetTraceFile(sessionId)
    