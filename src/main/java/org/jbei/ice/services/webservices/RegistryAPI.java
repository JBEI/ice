package org.jbei.ice.services.webservices;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.authentication.InvalidCredentialsException;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.sample.SampleController;
import org.jbei.ice.lib.entry.sample.StorageController;
import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.net.WoRController;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.StrainData;
import org.jbei.ice.lib.shared.dto.permission.AccessPermission;
import org.jbei.ice.lib.shared.dto.search.SearchQuery;
import org.jbei.ice.lib.shared.dto.search.SearchResults;
import org.jbei.ice.lib.shared.dto.user.User;
import org.jbei.ice.lib.shared.dto.web.WebOfRegistries;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.lib.vo.PartTransfer;
import org.jbei.ice.lib.vo.SequenceTraceFile;
import org.jbei.ice.server.ModelToInfoFactory;

/**
 * SOAP API methods.
 *
 * @author Hector Plahar, Zinovii Dmytriv, Timothy Ham
 */
@WebService(targetNamespace = "https://api.registry.jbei.org/")
public class RegistryAPI implements IRegistryAPI {
    /**
     * Login to the ICE SOAP service, with the given login and password. Returns a session key that is intended
     * to be used for restricted access calls
     *
     * @param login    Login.
     * @param password Password.
     * @return Session key.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public String login(@WebParam(name = "login") String login,
            @WebParam(name = "password") String password) throws SessionException, ServiceException {
        String sessionId;

        try {
            AccountController controller = ControllerFactory.getAccountController();
            User info = controller.authenticate(login, password);
            sessionId = info.getSessionId();
        } catch (InvalidCredentialsException e) {
            Logger.warn("Invalid credentials provided by user: " + login);
            throw new SessionException("Invalid credentials!");
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }

        log("User by login '" + login + "' successfully logged in");
        return sessionId;
    }

    /**
     * Logout out of the ICE SOAP service by de-authenticating the session
     *
     * @param sessionId Session key to log out.
     * @throws ServiceException
     */
    @Override
    public void logout(@WebParam(name = "sessionId") String sessionId) throws ServiceException {
        try {
            AccountController.deauthenticate(sessionId);
            log(sessionId, "Logged out");
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }
    }

    /**
     * Check if the session key is still valid.
     *
     * @param sessionId Session key to check.
     * @return True if still authenticated, false otherwise
     * @throws ServiceException
     */
    @Override
    public boolean isAuthenticated(@WebParam(name = "sessionId") String sessionId) throws ServiceException {
        boolean authenticated;

        try {
            authenticated = AccountController.isAuthenticated(sessionId);
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }

        return authenticated;
    }

    /**
     * Retrieve a part by its name. Note that the name has to be unique to the part.
     * Names are generally free form fields and so unless your registry instance
     * enforces uniqueness of names, this call can fail if there are multiple parts with
     * the same name.
     * <p/>
     * If the name is detected to start with the part number prefix (obtained from database properties)
     * then the part number is checked instead.
     *
     * @param sessionId Session key.
     * @param name      Name of the Entry to retrieve.
     * @return Entry object.
     * @throws ServiceException
     */
    @Override
    public PartData getPartByUniqueName(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "name") String name) throws ServiceException {
        log(sessionId, "getPartByUniqueName: " + name);
        try {
            Account account = validateAccount(sessionId);
            String prefix = Utils.getConfigValue(ConfigurationKey.PART_NUMBER_PREFIX);
            if (prefix != null && name.startsWith(prefix))
                return ControllerFactory.getEntryController().getByPartNumber(account, name);
            return ControllerFactory.getEntryController().getByUniqueName(account, name);
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }
    }

    /**
     * Determines if a referenced part has a sample associated with it
     *
     * @param recordId universally unique identifier for the part
     * @return true if the part referenced by the record id has a sequence associated with it, false otherwise
     * @throws ServiceException
     */
    @Override
    public boolean hasSequence(@WebParam(name = "entryId") String recordId) throws ServiceException {
        try {
            log("hasSequence " + recordId);
            PartData entry = ControllerFactory.getEntryController().getPublicEntryByRecordId(recordId);
            return ControllerFactory.getSequenceController().hasSequence(entry.getId());
        } catch (ControllerException ce) {
            throw new ServiceException(ce);
        }
    }

    /**
     * Determines if a part has a sequence associated with it that was uploaded by the user as
     * opposed to creating one using vector editor
     *
     * @param recordId universally unique identifier for
     * @return true if an uploaded sequence is found associated with part, false otherwise
     * @throws ServiceException
     */
    @Override
    public boolean hasUploadedSequence(@WebParam(name = "recordId") String recordId) throws ServiceException {
        try {
            log("hasSequence " + recordId);
            PartData entry = ControllerFactory.getEntryController().getPublicEntryByRecordId(recordId);
            return ControllerFactory.getSequenceController().hasOriginalSequence(entry.getId());
        } catch (ControllerException ce) {
            throw new ServiceException(ce);
        }
    }

    /**
     * Retrieve a part by its recordId, which is a universally unique identifier for the part.
     *
     * @param sessionId Session key. Must be associated with an authenticated session
     * @param recordId  recordId of the Entry.
     * @return retrieves part if one is found with specified record Id. Null otherwise.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public PartData getPartByRecordId(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "recordId") String recordId) throws SessionException, ServiceException {
        log(sessionId, "getPartByRecordId: " + recordId);
        Account account = validateAccount(sessionId);

        try {
            return ControllerFactory.getEntryController().getPartByRecordId(account, recordId);
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException(e);
        }
    }

    /**
     * Retrieves a public part by its local unique identifier. The part must have public access
     * permission enabled for it
     *
     * @param entryId local unique identifier that is
     * @return part referenced by identifier if available and has public access permission, null otherwise
     * @throws ServiceException
     */
    @Override
    public PartData getPublicPart(@WebParam(name = "entryId") long entryId) throws ServiceException {
        log("getByEntryId" + entryId);
        try {
            return ControllerFactory.getEntryController().getPublicEntryById(entryId);
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }
    }

    /**
     * Retrieve a part by its part number. PartNumbers are unique within single registry instances.
     *
     * @param sessionId  Session key.
     * @param partNumber Part number of the desired part.
     * @return Entry object.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public PartData getByPartNumber(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "partNumber") String partNumber) throws SessionException, ServiceException {
        log(sessionId, "getByPartNumber: " + partNumber);
        Account account = validateAccount(sessionId);

        try {
            EntryController entryController = ControllerFactory.getEntryController();
            return entryController.getByPartNumber(account, partNumber);
        } catch (PermissionException e) {
            Logger.error(account.getEmail() + " attempting to retrieve entry by part number " + partNumber
                                 + " without access permissions");
            return null;
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }
    }

    /**
     * Delete the part specified by the entry id from the server.
     *
     * @param sessionId Session key.user referenced by this key must be the owner of the part of have
     *                  administrative privileges on this server
     * @param entryId   unique identifier of the part.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public void deleteEntry(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") long entryId) throws SessionException, ServiceException {
        log(sessionId, "deleteEntry: " + entryId);
        Account account = validateAccount(sessionId);

        try {
            EntryController entryController = ControllerFactory.getEntryController();
            entryController.delete(account, entryId);
            log("User '" + account.getEmail() + "' removed entry: '" + entryId + "'");
        } catch (PermissionException e) {
            Logger.error(e);
            throw new ServiceException(e.getMessage());
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }
    }

    /**
     * Get the {@link FeaturedDNASequence} of the specified part
     *
     * @param sessionId Session key.
     * @param entryId   RecordId of the desired Entry.
     * @return FeaturedDNASequence object.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public FeaturedDNASequence getSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException {
        log(sessionId, "getSequence: " + entryId);
        Account account = validateAccount(sessionId);

        try {
            SequenceController sequenceController = ControllerFactory.getSequenceController();
            log("User '" + account.getEmail() + "' pulled sequence: '" + entryId + "'");
            return sequenceController.retrievePartSequence(account, entryId);
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }
    }

    @Override
    public FeaturedDNASequence getPublicSequence(@WebParam(name = "recordId") String recordId) throws ServiceException {
        log("getPublicSequence: " + recordId);
        try {
            return ControllerFactory.getEntryController().getPublicSequence(recordId);
        } catch (ControllerException ce) {
            throw new ServiceException(ce);
        }
    }

    /**
     * Retrieve the original uploaded sequence (Sequence.sequenceUser) of the specified part
     *
     * @param sessionId Session key.
     * @param entryId   RecordId of the desired Entry.
     * @return Content of the original uploaded sequence file as String.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public String getOriginalGenBankSequence(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException {
        log(sessionId, "getOriginalGenbankSequence: " + entryId);
        String genbankSequence = "";
        Account account = validateAccount(sessionId);

        try {
            SequenceController sequenceController = ControllerFactory.getSequenceController();
            EntryController entryController = ControllerFactory.getEntryController();
            Sequence sequence = sequenceController.getByEntry(entryController.getByRecordId(account, entryId));

            if (sequence != null) {
                genbankSequence = sequence.getSequenceUser();
            }

            log("User '" + account.getEmail() + "' pulled original genbank sequence: '" + entryId + "'");
        } catch (PermissionException e) {
            throw new ServiceException(e);
        } catch (Exception e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }

        return genbankSequence;
    }

    @Override
    public String createStrainWithPlasmid(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "strain") PartTransfer strainTransfer,
            @WebParam(name = "plasmid") PartTransfer plasmidTransfer,
            @WebParam(name = "permissions") ArrayList<AccessPermission> permissions) throws ServiceException {

        try {
            Account account = validateAccount(sessionId);
            Logger.info(account.getEmail() + ": remotely creating strain with plasmid");
            EntryController controller = ControllerFactory.getEntryController();
            return controller.createStrainWithPlasmid(account, strainTransfer, plasmidTransfer, permissions);
        } catch (SessionException | ControllerException se) {
            throw new ServiceException(se);
        }
    }

    @Override
    public boolean updatePartStatus(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "recordId") String recordId, @WebParam(name = "status") String status)
            throws ServiceException {
        try {
            Account account = validateAccount(sessionId);
            ControllerFactory.getEntryController().updatePartStatus(account, recordId, status);
            return true;
        } catch (SessionException | ControllerException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Genbank formatted {@link Sequence} of the specified part record id
     *
     * @param sessionId Session key.
     * @param entryId   RecordId of the desired Entry.
     * @return Genbank file formatted string.
     * @throws SessionException
     * @throws ServiceException
     */
//    @Override
//    public String getGenBankSequence(@WebParam(name = "sessionId") String sessionId,
//            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException {
//        log(sessionId, "getGenBankSequence: " + entryId);
//        String genbankSequence = "";
//        Account account = validateAccount(sessionId);
//
//        try {
//            SequenceController sequenceController = ControllerFactory.getSequenceController();
//            EntryController entryController = ControllerFactory.getEntryController();
//            Entry entry = entryController.getByRecordId(account, entryId);
//            Sequence sequence = sequenceController.getByEntry(entry);
//
//            if (sequence != null) {
//                GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getName());
//                genbankFormatter.setCircular((sequence.getEntry() instanceof Plasmid) ? ((Plasmid) entry)
//                        .getCircular() : false);
//
//                genbankSequence = sequenceController.compose(sequence, genbankFormatter);
//            }
//
//            log("User '" + account.getEmail() + "' pulled generated genbank sequence: '" + entryId + "'");
//        } catch (PermissionException e) {
//            throw new ServiceException(e);
//        } catch (Exception e) {
//            Logger.error(e);
//            throw new ServiceException("Registry Service Internal Error!");
//        }
//
//        return genbankSequence;
//    }

//    /**
//     * Fasta formatted {@link Sequence} for the specified part
//     *
//     * @param sessionId Session key.
//     * @param entryId   RecordId of the desired Entry.
//     * @return Fasta formatted sequence.
//     * @throws SessionException
//     * @throws ServiceException
//     */
//    @Override
//    public String getFastaSequence(@WebParam(name = "sessionId") String sessionId,
//            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException {
//        log(sessionId, "getFastaSequence: " + entryId);
//        String fastaSequence = "";
//        Account account = validateAccount(sessionId);
//
//        try {
//            SequenceController sequenceController = ControllerFactory.getSequenceController();
//            EntryController entryController = ControllerFactory.getEntryController();
//            Entry entry = entryController.getByRecordId(account, entryId);
//            Sequence sequence = sequenceController.getByEntry(entry);
//
//            if (sequence != null) {
//                fastaSequence = sequenceController.compose(sequence, new FastaFormatter(entry.getName()));
//            }
//
//            log("User '" + account.getEmail() + "' pulled generated fasta sequence: '" + entryId + "'");
//        } catch (PermissionException e) {
//            throw new ServiceException(e);
//        } catch (Exception e) {
//            Logger.error(e);
//            throw new ServiceException("Registry Service Internal Error!");
//        }
//
//        return fastaSequence;
//    }

    /**
     * Retrieve all the {@link Sample}s of the specified part.
     *
     * @param sessionId Session key.
     * @param entryId   RecordId of the Entry.
     * @return List of Samples.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public ArrayList<Sample> retrieveEntrySamples(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "entryId") String entryId) throws SessionException, ServiceException {
        log(sessionId, "retrieveEntrySamples: " + entryId);
        Account account = validateAccount(sessionId);
        SampleController sampleController = ControllerFactory.getSampleController();
        EntryController entryController = ControllerFactory.getEntryController();

//        try {
        return null; //sampleController.getSamples(entryController.getByRecordId(account, entryId));
//        } catch (ControllerException e) {
//            Logger.error(e);
//
//            throw new ServiceException("Registry Service Internal Error!");
//        } catch (PermissionException e) {
//            throw new ServiceException("No permissions to view entry");
//        }
    }

    /**
     * Retrieve the {@link Sample} object associated with a barcode.
     *
     * @param sessionId Session key.
     * @param barcode   Barcode string.
     * @return List of Samples.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public ArrayList<Sample> retrieveSamplesByBarcode(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "barcode") String barcode) throws SessionException, ServiceException {
        log(sessionId, "retrieveSamplesByBarcode: " + barcode);
        SampleController sampleController = ControllerFactory.getSampleController();
        StorageController storageController = ControllerFactory.getStorageController();

        try {
            Storage storage = storageController.retrieveStorageTube(barcode.trim());
            if (storage == null) {
                return null;
            }
            return sampleController.getSamplesByStorage(storage);
        } catch (ControllerException e) {
            Logger.error(e);
            throw new ServiceException(e);
        }
    }

    @Override
    public StrainData retrieveStrainForSampleBarcode(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "barcode") String barcode) throws SessionException, ServiceException {
        log(sessionId, "retrieveEntryForSampleBarcode: " + barcode);
        SampleController sampleController = ControllerFactory.getSampleController();
        StorageController storageController = ControllerFactory.getStorageController();

        try {
            Storage storage = storageController.retrieveStorageTube(barcode.trim());
            if (storage == null) {
                Logger.info("Could not find storage for tube with barcode " + barcode);
                return null;
            }

            List<Sample> samples = sampleController.getSamplesByStorage(storage);
            for (Sample sample : samples) {
                Storage sampleStorage = sample.getStorage();
                if (sampleStorage == null)
                    continue;
                if (sampleStorage.getStorageType() != StorageType.TUBE)
                    continue;

                return (StrainData) ModelToInfoFactory.getInfo(sample.getEntry());
            }
            return null;
        } catch (ControllerException e) {
            Logger.error(e);
            throw new ServiceException(e);
        }
    }

    /**
     * Checks if all samples have a common plate. If not, it determines which plate
     * is the most likely.
     *
     * @param sessionId id for session
     * @param samples   samples containing tube storage location with well parent
     * @return null if samples have a common plate, if not plate id (storage index) of most likely
     *         plate is returned
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public String samplePlate(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "samples") Sample[] samples) throws SessionException, ServiceException {
        log(sessionId, "samplePlate");
        StorageController storageController = ControllerFactory.getStorageController();
        HashMap<String, Integer> plateIndex = new HashMap<>();

        Sample initial = samples[0];
        Storage tube = initial.getStorage();

        // get tube by barcode and parent
        try {
            tube = storageController.retrieveStorageTube(tube.getIndex());
        } catch (ControllerException e) {
            Logger.error(e.getMessage());
            throw new ServiceException("Error retrieving storage location for tube " + tube.getIndex());
        }
        if (tube == null) {
            throw new ServiceException("Error retrieving storage location for tube");
        }

        Storage plate = tube.getParent().getParent();
        String highestFreqPlate = plate.getIndex();
        int highestFreqCount = 1;
        plateIndex.put(highestFreqPlate, highestFreqCount);

        for (int i = 1; i < samples.length; i += 1) {
            Sample sample = samples[i];
            String barcode = sample.getStorage().getIndex();
            if ("No Tube".equals(barcode) || "No Read".equals(barcode)) {
                continue;
            }

            try {
                tube = storageController.retrieveStorageTube(barcode);
            } catch (ControllerException e) {
                Logger.error(e.getMessage());
                throw new ServiceException("Error retrieving storage location for tube");
            }
            plate = tube.getParent().getParent();

            // check if this is new (not in plates map)
            Integer value = plateIndex.get(plate.getIndex());
            if (value == null) {
                // new
                plateIndex.put(plate.getIndex(), 1);
            } else {
                // update count
                value += 1;
                plateIndex.put(plate.getIndex(), value);
                if (value > highestFreqCount) {
                    highestFreqCount = value;
                    highestFreqPlate = plate.getIndex();
                }
            }
        }

        if (plateIndex.keySet().size() == 1) {
            return null;
        }

        return highestFreqPlate;
    }

    // Need moderator privileges to run this

    /**
     * Create a {@link Sample} object for the specified strain.
     * <p/>
     * This assumes a plate->well->tube storage scheme.
     *
     * @param sessionId Session key.
     * @param recordId  RecordId of the Strain.
     * @param rack      -Rack number.
     * @param location  Location number.
     * @param barcode   Barcode number.
     * @param label     Label.
     * @throws ServiceException
     * @throws PermissionException
     * @throws SessionException
     */
    @Override
    public void createStrainSample(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "recordId") String recordId, @WebParam(name = "rack") String rack,
            @WebParam(name = "location") String location,
            @WebParam(name = "barcode") String barcode, @WebParam(name = "label") String label)
            throws ServiceException, PermissionException, SessionException {
        log(sessionId, "createStrainsample: " + recordId + "," + location + "," + barcode);
        Account account;
        AccountController controller = ControllerFactory.getAccountController();

        try {
            account = controller.getAccountBySessionKey(sessionId);
        } catch (ControllerException e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }

        try {
            if (!controller.isAdministrator(account)) {
                log("Account " + account.getEmail() + " attempting to access createStrainSample()");
                throw new PermissionException("Account does not have permissions");
            }
        } catch (ControllerException e) {
            log(e.getMessage());
            throw new ServiceException("Registry Service Internal Error!");
        }

        // check if there is an existing sample with barcode
        StorageController storageController = ControllerFactory.getStorageController();
        SampleController sampleController = ControllerFactory.getSampleController();

        try {
            Storage storage = storageController.retrieveStorageTube(barcode.trim());
            if (storage != null) {
                ArrayList<Sample> samples = sampleController.getSamplesByStorage(storage);
                if (samples != null && !samples.isEmpty()) {
                    log("Barcode \"" + barcode + "\" already has a sample associated with it");
                    return;
                }
            }
        } catch (ControllerException e) {
            Logger.error(e);
            throw new ServiceException(e);
        }

        log("Creating new strain sample for entry \"" + recordId + "\" and label \"" + label + "\"");
        // TODO : this is a hack till we migrate to a single strain default
        Storage strainScheme = null;
        try {
            List<Storage> schemes = storageController.retrieveAllStorageSchemes();
            for (Storage storage : schemes) {
                if (storage.getStorageType() == StorageType.SCHEME
                        && "Strain Storage Matrix Tubes".equals(storage.getName())) {
                    strainScheme = storage;
                    break;
                }
            }
            if (strainScheme == null) {
                log("Could not locate default strain scheme (Strain Storage Matrix Tubes[Plate, Well, Tube])");
                throw new ServiceException("Registry Service Internal Error!");
            }

            Storage newLocation = storageController.getLocation(strainScheme, new String[]{rack,
                    location, barcode
            });

            Sample sample = sampleController.createSample(label, account.getEmail(), "");
            sample.setEntry(ControllerFactory.getEntryController().getByRecordId(account, recordId));
            sample.setStorage(newLocation);
            Sample saved = sampleController.saveSample(account, sample);
            if (saved == null) {
                throw new ServiceException("Unable to create sample");
            }
        } catch (ControllerException ce) {
            log(ce.getMessage());
            throw new ServiceException(ce.getMessage());
        }
    }

    /**
     * Check and automagically update sample storage for the given
     *
     * @param sessionId Session key.
     * @param samples   Samples
     * @param plateId   plateId
     * @return List of {@link Sample}s.
     * @throws SessionException
     * @throws ServiceException
     */
    @Override
    public List<Sample> checkAndUpdateSamplesStorage(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "samples") Sample[] samples, @WebParam(name = "plateId") String plateId)
            throws SessionException, ServiceException {
        log(sessionId, "checkAndUpdateSamplesStorage: " + plateId);
        StorageController storageController = ControllerFactory.getStorageController();
        SampleController sampleController = ControllerFactory.getSampleController();

        // count of plates seen so far
        List<Sample> retSamples = new LinkedList<>();

        // for each sample (unique elements are the barcode (tube index))
        for (Sample sample : samples) {
            // sent storage location. note that barcode can be no read or no tube
            String barcode = sample.getStorage().getIndex();
            if ("No Tube".equals(barcode) || "No Read".equals(barcode)) {
                retSamples.add(sample);
                continue;
            }

            String location = sample.getStorage().getParent().getIndex();
            Storage recordedTube;

            try {
                // stored storage location
                recordedTube = storageController.retrieveStorageTube(barcode);
                Storage recordedWell = recordedTube.getParent();
                Storage recordedPlate = recordedWell.getParent();
                Storage parentScheme = recordedPlate.getParent();

                boolean samePlate = (plateId == null);
                boolean sameWell = recordedWell.getIndex().equals(location);

                if (samePlate) {
                    if (sameWell) {
                        ArrayList<Sample> ret = sampleController.getSamplesByStorage(recordedTube);
                        if (ret != null && !ret.isEmpty()) {
                            retSamples.add(ret.get(0));
                        }

                        continue; // no changes needed
                    } else {
                        // same plate but different well                        
                        Storage well = storageController.retrieveStorageBy("Well", location,
                                                                           StorageType.WELL, recordedPlate.getId());
                        if (well == null) {
                            throw new ServiceException(
                                    "Could not retrieve new location for storage");
                        }
                        recordedTube.setParent(well);
                        storageController.update(recordedTube);
                    }
                } else {
                    // different plate (update using the passed parameter)
                    Storage newPlate = storageController.retrieveStorageBy("Plate", plateId,
                                                                           StorageType.PLATE96, parentScheme.getId());
                    if (sameWell) {
                        // update plate only
                        recordedWell.setParent(newPlate);
                        storageController.update(recordedWell);

                    } else {
                        // update plate and well
                        Storage well = storageController.retrieveStorageBy("Well", location,
                                                                           StorageType.WELL, newPlate.getId());
                        recordedTube.setParent(well);
                        storageController.update(recordedTube);
                    }
                }
            } catch (ControllerException e) {
                Logger.error(e);
                throw new ServiceException("Error retrieving/updating some records!");
            }

            ArrayList<Sample> ret;
            try {
                ret = sampleController.getSamplesByStorage(recordedTube);
                if (ret != null && !ret.isEmpty()) {
                    retSamples.add(ret.get(0));
                }
            } catch (ControllerException e) {

                Logger.error(e);
                sample.setStorage(recordedTube);
                retSamples.add(sample);
            }

        }
        return retSamples;
    }

    /**
     * Retrieve a list of {@link TraceSequence}s associated with the specified entry.
     *
     * @param sessionId Session key.
     * @param recordId  RecordId of the desired Entry.
     * @return List of TraceSequences.
     * @throws ServiceException
     * @throws SessionException
     */
    @Override
    public List<TraceSequence> listTraceSequenceFiles(
            @WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "recordId") String recordId) throws ServiceException, SessionException {
        log(sessionId, "listTraceSequenceFiles: " + recordId);
        Account account = validateAccount(sessionId);
        List<TraceSequence> result = new ArrayList<>();
        SequenceAnalysisController sequenceAnalysisController = ControllerFactory.getSequenceAnalysisController();
        EntryController entryController = ControllerFactory.getEntryController();

        List<TraceSequence> traces;
        try {
            traces = sequenceAnalysisController.getTraceSequences(entryController.getByRecordId(account, recordId));
            if (traces == null) {
                return result;
            }
        } catch (ControllerException | PermissionException e) {
            throw new ServiceException("Could not retrieve traces: " + e.getMessage());
        }
        for (TraceSequence trace : traces) {
            //null out entry to reduce output.
            trace.setEntry(null);
            // null out traceSequenceAlignment.traceSequence, as it causes infinite nesting  in xml for some reason.
            if (trace.getTraceSequenceAlignment() != null)
                trace.getTraceSequenceAlignment().setTraceSequence(null);
            result.add(trace);
        }

        return result;
    }

    /**
     * Upload a sequence trace (abi) file.
     *
     * @param sessionId      Session key.
     * @param recordId       RecordId of the desired entry
     * @param fileName       Name of the trace file.
     * @param base64FileData Base64 encoded content of the file.
     * @return File ID as saved on the server.
     * @throws ServiceException
     * @throws SessionException
     */
    @Override
    public String uploadTraceSequenceFile(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "recordId") String recordId,
            @WebParam(name = "fileName") String fileName,
            @WebParam(name = "base64FileData") String base64FileData) throws ServiceException, SessionException {
        log(sessionId, "uploadTraceSequenceFile: " + recordId + "," + fileName);

        SequenceAnalysisController sequenceAnalysisController = ControllerFactory.getSequenceAnalysisController();
        EntryController entryController = ControllerFactory.getEntryController();
        byte[] bytes = SerializationUtils.deserializeBase64StringToBytes(base64FileData);
        if (bytes == null) {
            throw new ServiceException("Invalid File Data!");
        }
        Account account = validateAccount(sessionId);
        String depositor = account.getEmail();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        String sequence;
        try {
            IDNASequence temp = sequenceAnalysisController.parse(bytes);

            if (temp == null) {
                throw new ServiceException("Could not parse trace file!");
            } else {
                sequence = temp.getSequence();
            }
        } catch (ControllerException e) {
            log(e.getMessage());
            throw new ServiceException("Could not parse trace file!: " + e.getMessage());
        }

        TraceSequence result;
        try {
            result = sequenceAnalysisController.uploadTraceSequence(entryController.getByRecordId(account, recordId),
                                                                    fileName, depositor, sequence, inputStream);
            sequenceAnalysisController.rebuildAllAlignments(entryController.getByRecordId(account, recordId));
        } catch (ControllerException | PermissionException e) {
            log(e.getMessage());
            throw new ServiceException("Could not upload trace sequence!: " + e.getMessage());
        }

        return result.getFileId();
    }

    /**
     * Retrieve the specified trace sequence file.
     *
     * @param sessionId Session Key.
     * @param fileId    File ID to retrieve.
     * @return {@link SequenceTraceFile} object.
     * @throws ServiceException
     * @throws SessionException
     */
    @Override
    public SequenceTraceFile getTraceSequenceFile(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "fileId") String fileId) throws ServiceException, SessionException {
        log(sessionId, "getTraceSequenceFile: " + fileId);
        SequenceAnalysisController sequenceAnalysisController = ControllerFactory.getSequenceAnalysisController();
        SequenceTraceFile traceFile;
        try {
            TraceSequence traceSequence = sequenceAnalysisController.getTraceSequenceByFileId(fileId);
            if (traceSequence == null) {
                throw new ServiceException("Could not retrieve Trace Sequence");
            }
            traceFile = sequenceAnalysisController.getSequenceTraceFile(traceSequence);

        } catch (ControllerException e) {
            log(e.getMessage());
            throw new ServiceException(e.getMessage());
        }

        return traceFile;
    }

    /**
     * Delete the specified trace sequence file.
     *
     * @param sessionId Session key.
     * @param fileId    ID of the file to delete.
     * @throws ServiceException
     * @throws SessionException
     */
    @Override
    public void deleteTraceSequenceFile(@WebParam(name = "sessionId") String sessionId,
            @WebParam(name = "fileId") String fileId) throws ServiceException, SessionException {
        log(sessionId, "deleteTraceSequenceFile: " + fileId);
        Account account = validateAccount(sessionId);
        SequenceAnalysisController sequenceAnalysisController = ControllerFactory.getSequenceAnalysisController();

        TraceSequence traceSequence;
        try {
            traceSequence = sequenceAnalysisController.getTraceSequenceByFileId(fileId);
            if (traceSequence == null) {
                throw new ServiceException("No such fileId found");
            }
            sequenceAnalysisController.removeTraceSequence(account, traceSequence);
            sequenceAnalysisController.rebuildAllAlignments(traceSequence.getEntry());
        } catch (ControllerException e) {
            log(e.getMessage());
            throw new ServiceException("Could not delete TraceSequence: " + e.getMessage());
        } catch (PermissionException e) {
            log(e.getMessage());
            throw new ServiceException("Deletion of this trace is not permitted");
        }
    }

    @Override
    public SearchResults runSearch(@WebParam(name = "uri") String uri, @WebParam(name = "apiKey") String apiKey,
            @WebParam(name = "searchQuery") SearchQuery query) throws ServiceException {
        try {
            if (!ControllerFactory.getWebController().isValidApiKey(uri, apiKey))
                return null;

            Logger.info("Registry API: web search");
            SearchController controller = ControllerFactory.getSearchController();

            return controller.runLocalSearch(null, query, true);
        } catch (ControllerException e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public WebOfRegistries setRegistryPartnerAdd(@WebParam(name = "uri") String uri,
            @WebParam(name = "name") String name, @WebParam(name = "add") boolean add) throws ServiceException {
        try {
            if (add) {
                Logger.info("API: adding web of registry partner " + name + "(" + uri + ")");
                return ControllerFactory.getWebController().addWebPartner(uri, name);
            } else {
                Logger.info("API: removing web of registry partner " + name + "(" + uri + ")");
                ControllerFactory.getWebController().removeWebPartner(uri);
                return null;
            }
        } catch (ControllerException ce) {
            throw new ServiceException(ce);
        }
    }

    @Override
    public boolean uploadParts(@WebParam(name = "partnerId") String partnerId,
            @WebParam(name = "parts") ArrayList<PartTransfer> parts) throws ServiceException {
        WoRController controller = ControllerFactory.getWebController();
        try {
            if (!controller.isWebEnabled() || !controller.isValidWebPartner(partnerId))
                return false;
        } catch (ControllerException e) {
            throw new ServiceException("Could not validate");
        }

        Logger.info("Registry API: transmit entries from ");
        EntryController entryController = ControllerFactory.getEntryController();
        return entryController.recordParts(parts);
    }

    @Override
    public String requestAPIKey(@WebParam(name = "url") String url, @WebParam(name = "name") String name,
            @WebParam(name = "authenticationKey") String authenticationKey) throws ServiceException {
        IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);

        // check the authentication key just received before sending one in kind
        String myUrl = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);
        boolean isValid = api.isValidApiKey(myUrl, authenticationKey);
        if (!isValid) {
            Logger.error("authentication key (" + authenticationKey + ") received from (" + url
                                 + ") could not be verified");
            return null;
        }

        // is valid. create a record if one does not exist , save authKey as apiKey and generate authentication token
        try {
            return ControllerFactory.getWebController().requestApiKeyForNewPartner(url, name, authenticationKey);
        } catch (ControllerException e) {
            throw new ServiceException("Could not generate api key");
        }
    }

    @Override
    public boolean isValidApiKey(@WebParam(name = "url") String url, @WebParam(name = "apiKey") String apiKey)
            throws ServiceException {
        try {
            return ControllerFactory.getWebController().isValidApiKey(url, apiKey);
        } catch (ControllerException e) {
            throw new ServiceException("Could not verify api key");
        }
    }

    /**
     * Retrieve the user {@link Account} associated with the given session key, if user is logged
     * in.
     *
     * @param sessionId Session key.
     * @return {@link Account} if user is logged in.
     * @throws ServiceException
     * @throws SessionException
     */
    protected Account validateAccount(@WebParam(name = "sessionId") String sessionId)
            throws ServiceException, SessionException {
        if (!isAuthenticated(sessionId)) {
            throw new SessionException("Unauthorized access! Authorize first!");
        }

        Account account;
        AccountController controller = ControllerFactory.getAccountController();

        try {
            account = controller.getAccountBySessionKey(sessionId);
        } catch (ControllerException e) {
            Logger.error(e);
            throw new ServiceException("Registry Service Internal Error!");
        }

        if (account == null) {
            Logger.error("Failed to lookup account!");
            throw new ServiceException("Registry Service Internal Error!");
        }

        return account;
    }

    /**
     * Write into the log at the INFO level, using the RegistryAPI prefix.
     *
     * @param message Log message.
     */
    private void log(String message) {
        Logger.info("RegistryAPI: " + message);
    }

    /**
     * Write into the log at the INFO level, using the RegistryAPI prefix and the account email
     * associated with the given session key.
     *
     * @param sessionId Session key.
     * @param message   Log message.
     */
    private void log(String sessionId, String message) {
        Account account;
        try {
            account = validateAccount(sessionId);
            if (account != null) {
                message = account.getEmail() + "\t" + message;
            }
        } catch (ServiceException e) {
            // it's ok, session expired.
            message = "invalid account\t" + message;
        } catch (SessionException e) {
            // It's ok, session expired.
            message = "invalid account\t" + message;
        }
        Logger.info("RegistryAPI: " + message);
    }
}
