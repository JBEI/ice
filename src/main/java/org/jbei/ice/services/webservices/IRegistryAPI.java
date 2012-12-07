package org.jbei.ice.services.webservices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.SequenceTraceFile;
import org.jbei.ice.shared.dto.search.SearchQuery;
import org.jbei.ice.shared.dto.search.SearchResults;

/**
 * @author Hector Plahar
 */
@WebService(targetNamespace = "https://api.registry.jbei.org/")
public interface IRegistryAPI {
    String login(@WebParam(name = "login") String login, @WebParam(name = "password") String password)
            throws SessionException, ServiceException;

    void logout(@WebParam(name = "sessionId") String sessionId) throws ServiceException;

    boolean isAuthenticated(@WebParam(name = "sessionId") String sessionId) throws ServiceException;

    boolean isModerator(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "login") String login)
            throws SessionException, ServiceException;

    Entry getEntryByName(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "name") String name)
            throws ServiceException;

    boolean hasSequence(@WebParam(name = "recordId") String recordId) throws ServiceException;

    Entry getByRecordId(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException,
            ServicePermissionException;

    Entry getPublicEntryByRecordId(@WebParam(name = "recordId") String recordId) throws ServiceException;

    Entry getByPartNumber(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "partNumber") String partNumber) throws SessionException,
            ServiceException, ServicePermissionException;

    boolean hasReadPermissions(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException,
            ServicePermissionException;

    boolean hasWritePermissions(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException;

    Plasmid createPlasmid(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "plasmid") Plasmid plasmid) throws SessionException, ServiceException;

    Strain createStrain(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "strain") Strain strain) throws SessionException, ServiceException;

    Part createPart(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "part") Part part) throws SessionException, ServiceException;

    ArabidopsisSeed createSeed(@WebParam(name = "sessionId") String sessionId, @WebParam(
            name = "seed") ArabidopsisSeed seed) throws SessionException, ServiceException;

    Plasmid updatePlasmid(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "plasmid") Plasmid plasmid)
            throws SessionException, ServiceException, ServicePermissionException;

    Strain updateStrain(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "strain") Strain strain)
            throws SessionException, ServiceException, ServicePermissionException;

    Part updatePart(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "part") Part part)
            throws SessionException, ServiceException, ServicePermissionException;

    void removeEntry(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException, ServicePermissionException;

    FeaturedDNASequence getSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException, ServicePermissionException;

    FeaturedDNASequence getPublicSequence(@WebParam(name = "entryId") String entryId) throws ServiceException;

    String getOriginalGenBankSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException, ServicePermissionException;

    String getGenBankSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException, ServicePermissionException;

    String getFastaSequence(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException, ServicePermissionException;

    FeaturedDNASequence createSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId,
            @WebParam(name = "sequence") FeaturedDNASequence featuredDNASequence)
            throws SessionException, ServiceException, ServicePermissionException;

    void removeSequence(@WebParam(name = "sessionId") String sessionId, @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException, ServicePermissionException;

    FeaturedDNASequence uploadSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId, @WebParam(name = "sequence") String sequence)
            throws SessionException, ServiceException, ServicePermissionException;

    ArrayList<Sample> retrieveEntrySamples(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId)
            throws SessionException, ServiceException, ServicePermissionException;

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
     * WARNING
     * This is experimental and should not be used under any circumstances by third party applications
     */
    boolean transmitEntries(@WebParam(name = "entrySequenceMap") HashMap<Entry, String> entrySequenceMap)
            throws ServiceException;
}
