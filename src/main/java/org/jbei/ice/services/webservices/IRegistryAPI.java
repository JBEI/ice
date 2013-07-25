package org.jbei.ice.services.webservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.search.SearchResults;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.SequenceTraceFile;

/**
 * @author Hector Plahar
 */
@WebService(targetNamespace = "https://api.registry.jbei.org/")
public interface IRegistryAPI {
    String login(@WebParam(name = "login") String login, @WebParam(name = "password") String password)
            throws SessionException, ServiceException;

    void logout(@WebParam(name = "sessionId") String sessionId) throws ServiceException;

    boolean isAuthenticated(@WebParam(name = "sessionId") String sessionId) throws ServiceException;

//    boolean isAdministrator(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "login") String login)
//            throws SessionException, ServiceException;

    PartData getPartByUniqueName(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "name") String name)
            throws ServiceException;

    boolean hasSequence(@WebParam(name = "recordId") String recordId) throws ServiceException;

    boolean hasUploadedSequence(@WebParam(name = "recordId") String recordId) throws ServiceException;

    PartData getPartByRecordId(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException;

    PartData getPublicPart(@WebParam(name = "entryId") long entryId) throws ServiceException;

    PartData getByPartNumber(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "partNumber") String partNumber) throws SessionException,
            ServiceException;

    void deleteEntry(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "entryId") long entryId)
            throws SessionException, ServiceException;

    FeaturedDNASequence getSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException;

    FeaturedDNASequence getPublicSequence(@WebParam(name = "entryId") String entryId) throws ServiceException;

    String getOriginalGenBankSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException;

//    String getGenBankSequence(@WebParam(name = "sessionId") String sessionId,
//            @WebParam(name = "entryId") String entryId)
//            throws SessionException, ServiceException;

//    String getFastaSequence(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "entryId") String
// entryId)
//            throws SessionException, ServiceException;
//
//    FeaturedDNASequence createSequence(@WebParam(name = "sessionId") String sessionId,
//            @WebParam(name = "entryId") String entryId,
//            @WebParam(name = "sequence") FeaturedDNASequence featuredDNASequence)
//            throws SessionException, ServiceException;
//
//    void removeSequence(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "entryId") String entryId)
//            throws SessionException, ServiceException;
//
//    FeaturedDNASequence uploadSequence(@WebParam(name = "sessionId") String sessionId,
//            @WebParam(name = "entryId") String entryId, @WebParam(name = "sequence") String sequence)
//            throws SessionException, ServiceException;

    ArrayList<Sample> retrieveEntrySamples(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException;

    ArrayList<Sample> retrieveSamplesByBarcode(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "barcode") String barcode) throws SessionException, ServiceException;

    String samplePlate(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "samples") Sample[] samples) throws SessionException, ServiceException;

    void createStrainSample(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "recordId") String recordId, @WebParam(name = "rack") String rack,
            @WebParam(name = "location") String location,
            @WebParam(name = "barcode") String barcode, @WebParam(name = "label") String label)
            throws ServiceException, PermissionException, SessionException;

    List<Sample> checkAndUpdateSamplesStorage(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "samples") Sample[] samples, @WebParam(name = "plateId") String plateId)
            throws SessionException, ServiceException;

    List<TraceSequence> listTraceSequenceFiles(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "recordId") String recordId) throws ServiceException, SessionException;

    String uploadTraceSequenceFile(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "recordId") String recordId,
            @WebParam(name = "fileName") String fileName,
            @WebParam(name = "base64FileData") String base64FileData) throws ServiceException, SessionException;

    SequenceTraceFile getTraceSequenceFile(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "fileId") String fileId) throws ServiceException, SessionException;

    void deleteTraceSequenceFile(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "fileId") String fileId) throws ServiceException, SessionException;

    SearchResults runSearch(@WebParam(name = "searchQuery") SearchQuery query) throws ServiceException;

    /**
     * adds or removes a partner from the web of registries. This call is typically used for the node master
     * who maintains (any maybe curates? the information)
     *
     * @param uri  unique resource identifier for the machine(s) hosting the partner
     * @param name name of the partner. Should be unique but is not enforced
     * @param add  whether to add or remove partner
     * @throws ServiceException
     */
    void processWebOfRegistryPartnerInformation(String uri, String name, boolean add) throws ServiceException;

    /**
     * WARNING
     * This is experimental and should not be used under any circumstances by third party applications
     */
    boolean transmitEntries(@WebParam(name = "entrySequenceMap") HashMap<PartData, String> entrySequenceMap)
            throws ServiceException;

    PartData retrieveStrainForSampleBarcode(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "barcode") String barcode) throws SessionException, ServiceException;
}
