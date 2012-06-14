package org.jbei.ice.services.blazeds;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.jbei.ice.bio.enzymes.RestrictionEnzyme;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManager;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManagerException;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.ProjectController;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.bulkimport.BulkImportController;
import org.jbei.ice.lib.composers.SequenceComposerException;
import org.jbei.ice.lib.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.BulkImport;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Project;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.BulkImportEntryData;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.lib.utils.SerializationUtils.SerializationUtilsException;
import org.jbei.ice.lib.utils.TraceAlignmentHelper;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.lib.vo.SequenceCheckerData;
import org.jbei.ice.lib.vo.SequenceCheckerProject;
import org.jbei.ice.lib.vo.TraceData;
import org.jbei.ice.lib.vo.VectorEditorProject;
import org.jbei.ice.services.blazeds.vo.UserPreferences;
import org.jbei.ice.services.blazeds.vo.UserRestrictionEnzymes;

import flex.messaging.io.ArrayCollection;
import flex.messaging.io.amf.ASObject;
import flex.messaging.io.amf.translator.ASTranslator;

/**
 * BlazeDS service calls for Flex applications.
 * 
 * @author Zinovii Dmytriv, Hector Plahar, Joanna Chen, Timothy Ham
 * 
 */
public class RegistryAMFAPI extends BaseService {
    /**
     * Retrieve {@link Entry} by its recordId.
     * 
     * @param sessionId
     *            session key.
     * @param entryId
     *            recordId.
     * @return Entry object.
     */
    public Entry getEntry(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController();

        Entry entry = null;
        try {
            entry = entryController.getByRecordId(account, entryId);
        } catch (ControllerException e) {
            Logger.error("Failed to get entry!", e);
            return null;
        } catch (PermissionException e) {
            Logger.warn(getLoggerPrefix() + "User " + account.getFullName()
                    + " tried to access entry without permissions.");

            return null;
        }

        return entry;
    }

    /**
     * Determine if the session has write permission to the specified {@link Entry}.
     * 
     * @param sessionId
     *            session key.
     * @param entryId
     *            recordId.
     * @return True if session has write permission to the Entry.
     */
    public boolean hasWritablePermissions(String sessionId, String entryId) {
        boolean result = false;

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return result;
        }

        EntryController entryController = new EntryController();

        try {
            Entry entry = entryController.getByRecordId(account, entryId);

            if (entry != null) {
                result = entryController.hasWritePermission(account, entry);
            }
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        } catch (PermissionException e) {
            Logger.warn(getLoggerPrefix() + "User " + account.getFullName()
                    + " tried to access entry without permissions.");

            return false;
        }

        return result;
    }

    /**
     * Retrieve the {@link FeaturedDNASequence} of the specified {@link Entry}.
     * 
     * @param sessionId
     *            session key.
     * @param entryId
     *            recordIf of the desired Entry.
     * @return FeaturedDNASequence object.
     */
    public FeaturedDNASequence getSequence(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController();

        Entry entry = null;
        try {
            entry = entryController.getByRecordId(account, entryId);
        } catch (ControllerException e) {
            Logger.error("Failed to get entry!", e);

            return null;
        } catch (PermissionException e) {
            Logger.warn(getLoggerPrefix() + "User " + account.getFullName()
                    + " tried to access entry without permissions.");

            return null;
        }

        FeaturedDNASequence featuredDNASequence = null;
        SequenceController sequenceController = new SequenceController(account);
        try {
            Sequence sequence = sequenceController.getByEntry(entry);

            featuredDNASequence = SequenceController.sequenceToDNASequence(sequence);
        } catch (ControllerException e) {
            Logger.error("Failed to get entry!", e);

            return null;
        }

        return featuredDNASequence;
    }

    /**
     * Save the given {@link FeaturedDNASequence} with the specified {@link Entry}.
     * 
     * @param sessionId
     *            session key.
     * @param entryId
     *            recordId of the desired Entry.
     * @param featuredDNASequence
     *            featuredDNASequence object to save.
     * @return True if successful.
     */
    public boolean saveSequence(String sessionId, String entryId,
            FeaturedDNASequence featuredDNASequence) {
        boolean result = false;

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return result;
        }

        EntryController entryController = new EntryController();
        SequenceController sequenceController = new SequenceController(account);

        try {
            Entry entry = entryController.getByRecordId(account, entryId);

            if (entry == null) {
                return false;
            }

            Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);

            sequence.setEntry(entry);
            sequenceController.update(sequence);

            logInfo(account.getEmail() + " saveSequence: " + entryId);

            result = true;
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        }

        return result;
    }

    /**
     * Retrieve {@link TraceSequence}s of the specified {@link Entry}.
     * 
     * @param sessionId
     *            session key.
     * @param entryId
     *            recordId of the desired Entry.
     * @return - List of TraceSequence objects.
     */
    public ArrayList<TraceSequence> getTraces(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController();
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                account);

        Entry entry;
        List<TraceSequence> traces;
        try {
            entry = entryController.getByRecordId(account, entryId);

            if (entry == null) {
                return null;
            }

            traces = sequenceAnalysisController.getTraceSequences(entry);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (PermissionException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return (ArrayList<TraceSequence>) traces;
    }

    /**
     * Generate a Genbank file from the given {@link FeaturedDNASequence}.
     * 
     * @param sessionId
     *            session key.
     * @param featuredDNASequence
     *            FeaturedDNASequence object.
     * @param name
     *            Locus name to be used.
     * @param isCircular
     *            True if circular.
     * @return Generated Genbank file as a String.
     */
    public String generateGenBank(String sessionId, FeaturedDNASequence featuredDNASequence,
            String name, boolean isCircular) {
        String result = "";

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return result;
        }

        if (featuredDNASequence == null) {
            return result;
        }

        Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);

        GenbankFormatter genbankFormatter = new GenbankFormatter(name);
        genbankFormatter.setCircular(isCircular);

        try {
            result = SequenceController.compose(sequence, genbankFormatter);

            logInfo(account.getEmail() + " generated and fetched genbank sequence");
        } catch (SequenceComposerException e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        }

        return result;
    }

    /**
     * Retrieve {@link UserPreferences} for the given session.
     * 
     * @param sessionId
     *            session key.
     * @return UserPreferences object for the specified user.
     */
    public UserPreferences getUserPreferences(String sessionId) {
        UserPreferences userPreferences = null;
        try {
            Account account = getAccountBySessionId(sessionId);

            if (account == null) {
                return null;
            }

            AccountPreferences accountPreferences = AccountController
                    .getAccountPreferences(account);

            if (accountPreferences != null && accountPreferences.getPreferences() != null
                    && !accountPreferences.getPreferences().isEmpty()) {
                try {
                    userPreferences = (UserPreferences) SerializationUtils
                            .deserializeStringToObject(accountPreferences.getPreferences());
                } catch (SerializationUtils.SerializationUtilsException e) {
                    Logger.error(getLoggerPrefix(), e);

                    return null;
                }
            } else {
                userPreferences = new UserPreferences();
            }
        } catch (ControllerException e) {
            Logger.error(getServiceName(), e);

            return null;
        } catch (Exception e) {
            Logger.error(getServiceName(), e);

            return null;
        }

        return userPreferences;
    }

    /**
     * Save {@link UserPreferences} object to the server.
     * 
     * @param sessionId
     *            session key.
     * @param preferences
     *            UserPreferences obect.
     * @return True if successful.
     */
    public boolean saveUserPreferences(String sessionId, UserPreferences preferences) {
        try {
            Account account = getAccountBySessionId(sessionId);

            if (account == null) {
                return false;
            }

            AccountPreferences accountPreferences = AccountController
                    .getAccountPreferences(account);

            String serializedPreferences = "";
            try {
                serializedPreferences = SerializationUtils.serializeObjectToString(preferences);
            } catch (SerializationUtils.SerializationUtilsException e) {
                Logger.error(getLoggerPrefix(), e);

                return false;
            }

            if (accountPreferences != null) {
                accountPreferences.setPreferences(serializedPreferences);

                accountController.saveAccountPreferences(accountPreferences);
            } else {
                accountController.saveAccountPreferences(new AccountPreferences(account,
                        serializedPreferences, ""));
            }

            logInfo(account.getEmail() + " saveUserPreferences");

            return true;
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);
        }

        return false;
    }

    /**
     * Retrieve {@link UserRestrictionEnzymes} for the given session.
     * 
     * @param sessionId
     *            session key.
     * @return UserRestrictionEnzymes object for the current user.
     */
    public UserRestrictionEnzymes getUserRestrictionEnzymes(String sessionId) {
        UserRestrictionEnzymes userRestrictionEnzymes = null;

        try {
            Account account = getAccountBySessionId(sessionId);

            if (account == null) {
                return null;
            }

            AccountPreferences accountPreferences = AccountController
                    .getAccountPreferences(account);

            if (accountPreferences != null && accountPreferences.getRestrictionEnzymes() != null
                    && !accountPreferences.getRestrictionEnzymes().isEmpty()) {
                userRestrictionEnzymes = (UserRestrictionEnzymes) SerializationUtils
                        .deserializeStringToObject(accountPreferences.getRestrictionEnzymes());
            } else {
                userRestrictionEnzymes = new UserRestrictionEnzymes();
            }
        } catch (SerializationUtils.SerializationUtilsException e) {
            Logger.error(getServiceName(), e);

            return null;
        } catch (ControllerException e) {
            Logger.error(getServiceName(), e);

            return null;
        } catch (Exception e) {
            Logger.error(getServiceName(), e);

            return null;
        }

        return userRestrictionEnzymes;
    }

    /**
     * Save the given {@link UserRestrictionEnzymes} to the database.
     * 
     * @param sessionId
     *            session key.
     * @param userRestrictionEnzymes
     *            UserRestrictionEnzymes object to save.
     */
    public void saveUserRestrictionEnzymes(String sessionId,
            UserRestrictionEnzymes userRestrictionEnzymes) {
        try {
            Account account = getAccountBySessionId(sessionId);

            if (account == null) {
                return;
            }

            AccountPreferences accountPreferences = AccountController
                    .getAccountPreferences(account);

            String serializedUserRestrictionEnzymes = SerializationUtils
                    .serializeObjectToString(userRestrictionEnzymes);

            if (accountPreferences != null) {
                accountPreferences.setRestrictionEnzymes(serializedUserRestrictionEnzymes);
                accountController.saveAccountPreferences(accountPreferences);
            } else {
                accountController.saveAccountPreferences(new AccountPreferences(account, "",
                        serializedUserRestrictionEnzymes));
            }

            logInfo(account.getEmail() + " saveUserRestrictionEnzymes");
        } catch (SerializationUtils.SerializationUtilsException e) {
            Logger.error(getServiceName(), e);
        } catch (ControllerException e) {
            Logger.error(getServiceName(), e);
        } catch (Exception e) {
            Logger.error(getServiceName(), e);
        }
    }

    /**
     * Retrieve {@link RestrictionEnzyme}s for the given user.
     * 
     * @param sessionId
     *            session key.
     * @return Collection of RestrictionEnzymes for the specified user.
     */
    public Collection<RestrictionEnzyme> getRestrictionEnzymes(String sessionId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        Collection<RestrictionEnzyme> enzymes = null;

        try {
            enzymes = RestrictionEnzymesManager.getInstance().getEnzymes();

            logInfo(account.getEmail() + " pulled restriction enzymes database");
        } catch (RestrictionEnzymesManagerException e) {
            Logger.error(getServiceName(), e);
        } catch (Exception e) {
            Logger.error(getServiceName(), e);
        }

        return enzymes;
    }

    /**
     * Parse the given string data into a {@link FeaturedDNASequence} object.
     * 
     * @param data
     *            data to parse.
     * @return FeaturedDNASequence object.
     */
    public FeaturedDNASequence parseSequenceFile(String data) {
        FeaturedDNASequence featuredDNASequence = null;

        try {
            featuredDNASequence = (FeaturedDNASequence) GeneralParser.getInstance().parse(data);

            if (featuredDNASequence == null) {
                logInfo("Failed to parse sequence file!");
            } else {
                logInfo("Successfully parsed DNA sequence");
            }
        } catch (Exception e) {
            Logger.error(getServiceName(), e);
        }

        return featuredDNASequence;
    }

    /**
     * Generate a new Genbank file from the given {@link FeaturedDNASequence}.
     * 
     * @param featuredDNASequence
     *            FeaturedDNASequence object to convert to genbank.
     * @return Genbank file as string.
     */
    public String generateSequenceFile(FeaturedDNASequence featuredDNASequence) {
        String result = "";

        Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);

        GenbankFormatter genbankFormatter = new GenbankFormatter("NewSequence");
        genbankFormatter.setCircular(true);

        try {
            result = SequenceController.compose(sequence, genbankFormatter);

            logInfo("Generated and fetched sequence");
        } catch (SequenceComposerException e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);

            return result;
        }

        return result;
    }

    /**
     * Create a new {@link SequenceCheckerProject} in the database.
     * 
     * @param sessionId
     *            session key.
     * @param sequenceCheckerProject
     *            SequenceCheckerProject to save.
     * @return SequenceCheckerProject that was saved in the database.
     */
    public SequenceCheckerProject createSequenceCheckerProject(String sessionId,
            SequenceCheckerProject sequenceCheckerProject) {
        if (sequenceCheckerProject == null || sessionId == null) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        String serializedSequenceCheckerData = "";

        try {
            serializedSequenceCheckerData = SerializationUtils
                    .serializeObjectToString(sequenceCheckerProject.getSequenceCheckerData());
        } catch (SerializationUtilsException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        ProjectController projectController = new ProjectController(account);

        Project project = projectController.createProject(account,
            sequenceCheckerProject.getName(), sequenceCheckerProject.getDescription(),
            serializedSequenceCheckerData, sequenceCheckerProject.typeName(), new Date(),
            new Date());

        try {
            Project savedProject = projectController.save(project);

            sequenceCheckerProject.setName(savedProject.getName());
            sequenceCheckerProject.setDescription(savedProject.getDescription());
            sequenceCheckerProject.setUuid(savedProject.getUuid());
            sequenceCheckerProject.setOwnerEmail(savedProject.getAccount().getEmail());
            sequenceCheckerProject.setOwnerName(savedProject.getAccount().getFullName());
            sequenceCheckerProject.setCreationTime(savedProject.getCreationTime());
            sequenceCheckerProject.setModificationTime(savedProject.getModificationTime());
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (PermissionException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return sequenceCheckerProject;
    }

    /**
     * Save the given {@link SequenceCheckerProject} in the database.
     * 
     * 
     * @param sessionId
     *            session key.
     * @param sequenceCheckerProject
     *            SequenceCheckerProject to save.
     * @return SequenceCheckerProject that was saved in the database.
     */
    public SequenceCheckerProject saveSequenceCheckerProject(String sessionId,
            SequenceCheckerProject sequenceCheckerProject) {
        if (sessionId == null || sessionId.isEmpty() || sequenceCheckerProject == null
                || sequenceCheckerProject.getUuid() == null) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        SequenceCheckerProject resultSequenceCheckerProject = null;

        ProjectController projectController = new ProjectController(account);
        try {
            Project project = projectController.getProjectByUUID(sequenceCheckerProject.getUuid());

            project.setName(sequenceCheckerProject.getName());
            project.setDescription(sequenceCheckerProject.getDescription());
            project.setModificationTime(new Date());
            project.setData(SerializationUtils.serializeObjectToString(sequenceCheckerProject
                    .getSequenceCheckerData()));

            Project savedProject = projectController.save(project);

            resultSequenceCheckerProject = new SequenceCheckerProject(savedProject.getName(),
                    savedProject.getDescription(), savedProject.getUuid(), savedProject
                            .getAccount().getEmail(), savedProject.getAccount().getFullName(),
                    savedProject.getCreationTime(), savedProject.getModificationTime(),
                    sequenceCheckerProject.getSequenceCheckerData());
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (SerializationUtilsException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (PermissionException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return resultSequenceCheckerProject;
    }

    /**
     * Retrieve the specified {@link SequenceCheckerProject} in the database.
     * 
     * @param sessionId
     *            session key.
     * @param projectId
     *            uuid of the SequenceCheckerProject to retrieve.
     * @return SequenceCheckerProject object.
     */
    public SequenceCheckerProject getSequenceCheckerProject(String sessionId, String projectId) {
        if (projectId == null || sessionId == null || sessionId.isEmpty() || projectId.isEmpty()) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        SequenceCheckerProject sequenceCheckerProject = null;

        ProjectController projectController = new ProjectController(account);
        try {
            Project project = projectController.getProjectByUUID(projectId);

            SequenceCheckerData sequenceCheckerData = (SequenceCheckerData) SerializationUtils
                    .deserializeStringToObject(project.getData());

            sequenceCheckerProject = new SequenceCheckerProject(project.getName(),
                    project.getDescription(), project.getUuid(), account.getEmail(),
                    account.getFullName(), project.getCreationTime(),
                    project.getModificationTime(), sequenceCheckerData);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (SerializationUtilsException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return sequenceCheckerProject;
    }

    /**
     * Perform sequence trace alignment on the given {@link SequenceCheckerProject}.
     * 
     * @param sessionId
     *            session key.
     * @param sequenceCheckerProject
     *            SequenceCheckerProject
     * @return SequenceCheckerPorject with updated alignment information.
     */
    public SequenceCheckerProject alignSequenceCheckerProject(String sessionId,
            SequenceCheckerProject sequenceCheckerProject) {
        if (sessionId == null || sessionId.isEmpty() || sequenceCheckerProject == null) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        if (sequenceCheckerProject.getSequenceCheckerData() == null) {
            return sequenceCheckerProject;
        }

        if (sequenceCheckerProject.getSequenceCheckerData().getSequence() == null
                || sequenceCheckerProject.getSequenceCheckerData().getSequence().getSequence() == null
                || sequenceCheckerProject.getSequenceCheckerData().getSequence().getSequence()
                        .isEmpty()) { // no sequence available => nullify all traceData objects 
            if (sequenceCheckerProject.getSequenceCheckerData().getTraces() != null
                    && sequenceCheckerProject.getSequenceCheckerData().getTraces().size() > 0) {
                for (int i = 0; i < sequenceCheckerProject.getSequenceCheckerData().getTraces()
                        .size(); i++) {
                    TraceData traceData = sequenceCheckerProject.getSequenceCheckerData()
                            .getTraces().get(i);

                    traceData.setScore(-1);
                    traceData.setStrand(-1);
                    traceData.setQueryStart(-1);
                    traceData.setQueryEnd(-1);
                    traceData.setSubjectStart(-1);
                    traceData.setSubjectEnd(-1);
                    traceData.setQueryAlignment("");
                    traceData.setSubjectAlignment("");
                }
            }

            return sequenceCheckerProject;
        }

        // trying to align
        if (sequenceCheckerProject.getSequenceCheckerData().getTraces() != null
                && sequenceCheckerProject.getSequenceCheckerData().getTraces().size() > 0) {
            for (int i = 0; i < sequenceCheckerProject.getSequenceCheckerData().getTraces().size(); i++) {
                TraceData traceData = sequenceCheckerProject.getSequenceCheckerData().getTraces()
                        .get(i);

                TraceData alignedTraceData = TraceAlignmentHelper.alignSequences(
                    sequenceCheckerProject.getSequenceCheckerData().getSequence().getSequence(),
                    traceData.getSequence(), traceData.getFilename(), sequenceCheckerProject
                            .getSequenceCheckerData().getSequence().getIsCircular());

                if (alignedTraceData == null) {
                    traceData.setScore(-1);
                    traceData.setStrand(-1);
                    traceData.setQueryStart(-1);
                    traceData.setQueryEnd(-1);
                    traceData.setSubjectStart(-1);
                    traceData.setSubjectEnd(-1);
                    traceData.setQueryAlignment("");
                    traceData.setSubjectAlignment("");
                } else {
                    traceData.setScore(alignedTraceData.getScore());
                    traceData.setStrand(alignedTraceData.getStrand());
                    traceData.setQueryStart(alignedTraceData.getQueryStart());
                    traceData.setQueryEnd(alignedTraceData.getQueryEnd());
                    traceData.setSubjectStart(alignedTraceData.getSubjectStart());
                    traceData.setSubjectEnd(alignedTraceData.getSubjectEnd());
                    traceData.setQueryAlignment(alignedTraceData.getQueryAlignment());
                    traceData.setSubjectAlignment(alignedTraceData.getSubjectAlignment());
                }
            }
        }

        return sequenceCheckerProject;
    }

    /**
     * Parse the given byte array data into {@link TraceData}.
     * 
     * @param traceFileName
     *            name of the trace file.
     * @param data
     *            byte array data.
     * @return TraceData object.
     */
    public TraceData parseTraceFile(String traceFileName, byte[] data) {
        TraceData traceData = null;

        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(null);

        try {
            IDNASequence dnaSequence = sequenceAnalysisController.parse(data);

            if (dnaSequence == null) {
                logInfo("Failed to parse trace file!");
            } else {
                traceData = new TraceData(traceFileName, dnaSequence.getSequence(), -1, -1, -1, -1,
                        -1, -1, "", "");

                logInfo("Successfully parsed trace file");
            }
        } catch (Exception e) {
            Logger.error(getServiceName(), e);
        }

        return traceData;
    }

    /**
     * Create a new {@link VectorEditorProject} in the database.
     * 
     * @param sessionId
     *            session key.
     * @param vectorEditorProject
     *            VectorEditorProject to create.
     * @return Saved VectorEditorProject.
     */
    public VectorEditorProject createVectorEditorProject(String sessionId,
            VectorEditorProject vectorEditorProject) {
        if (vectorEditorProject == null || sessionId == null) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        String serializedVectorEditorData = "";

        try {
            serializedVectorEditorData = SerializationUtils
                    .serializeObjectToString(vectorEditorProject.getFeaturedDNASequence());
        } catch (SerializationUtilsException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        ProjectController projectController = new ProjectController(account);

        Project project = projectController.createProject(account, vectorEditorProject.getName(),
            vectorEditorProject.getDescription(), serializedVectorEditorData,
            vectorEditorProject.typeName(), new Date(), new Date());

        try {
            Project savedProject = projectController.save(project);

            vectorEditorProject.setName(savedProject.getName());
            vectorEditorProject.setDescription(savedProject.getDescription());
            vectorEditorProject.setUuid(savedProject.getUuid());
            vectorEditorProject.setOwnerEmail(savedProject.getAccount().getEmail());
            vectorEditorProject.setOwnerName(savedProject.getAccount().getFullName());
            vectorEditorProject.setCreationTime(savedProject.getCreationTime());
            vectorEditorProject.setModificationTime(savedProject.getModificationTime());
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (PermissionException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return vectorEditorProject;
    }

    /**
     * Save the given {@link VectorEditorProject} into the database.
     * 
     * @param sessionId
     *            session key.
     * @param vectorEditorProject
     *            VectorEditorProject to save.
     * @return Saved VectorEditorProject.
     */
    public VectorEditorProject saveVectorEditorProject(String sessionId,
            VectorEditorProject vectorEditorProject) {
        if (sessionId == null || sessionId.isEmpty() || vectorEditorProject == null
                || vectorEditorProject.getUuid() == null) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        VectorEditorProject resultVectorEditorProject = null;

        ProjectController projectController = new ProjectController(account);
        try {
            Project project = projectController.getProjectByUUID(vectorEditorProject.getUuid());

            project.setName(vectorEditorProject.getName());
            project.setDescription(vectorEditorProject.getDescription());
            project.setModificationTime(new Date());
            project.setData(SerializationUtils.serializeObjectToString(vectorEditorProject
                    .getFeaturedDNASequence()));

            Project savedProject = projectController.save(project);

            resultVectorEditorProject = new VectorEditorProject(savedProject.getName(),
                    savedProject.getDescription(), savedProject.getUuid(), savedProject
                            .getAccount().getEmail(), savedProject.getAccount().getFullName(),
                    savedProject.getCreationTime(), savedProject.getModificationTime(),
                    vectorEditorProject.getFeaturedDNASequence());
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (SerializationUtilsException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (PermissionException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return resultVectorEditorProject;
    }

    /**
     * Retrieve the specified {@link VectorEditorProject}.
     * 
     * @param sessionId
     *            session key.
     * @param projectId
     *            UUID of the desired VectorEditorProject.
     * @return VectorEditorProject.
     */
    public VectorEditorProject getVectorEditorProject(String sessionId, String projectId) {
        if (projectId == null || sessionId == null || sessionId.isEmpty() || projectId.isEmpty()) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        VectorEditorProject vectorEditorProject = null;

        ProjectController projectController = new ProjectController(account);
        try {
            Project project = projectController.getProjectByUUID(projectId);

            FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) SerializationUtils
                    .deserializeStringToObject(project.getData());

            vectorEditorProject = new VectorEditorProject(project.getName(),
                    project.getDescription(), project.getUuid(), account.getEmail(),
                    account.getFullName(), project.getCreationTime(),
                    project.getModificationTime(), featuredDNASequence);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (SerializationUtilsException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return vectorEditorProject;
    }

    /**
     * Retrieve the {@link Account} associated with the given session key.
     * 
     * @param sessionId
     *            session key.
     * @return Account object for the session key.
     */
    private Account sessionToAccount(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        return getAccountBySessionId(sessionId);
    }

    /**
     * Retrieve the BulkImportEntry type.
     * 
     * @param sessionId
     *            session key.
     * @param importId
     *            id of the BulkImport object.
     * @return String representation of BulkImport.
     */
    // TODO : the following need to be folded into a single call
    public String retrieveBulkImportEntryType(String sessionId, String importId) {

        Account account = sessionToAccount(sessionId);
        if (account == null) {
            System.out.println("Session is invalid");
            return null;
        }

        Logger.info("RetrieveBulkImportEntryType: " + importId);
        long id = Long.decode(importId);
        try {
            BulkImportController controller = new BulkImportController(account);
            return controller.retrieveType(id);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);
            return null;
        }
    }

    /**
     * Retrieve the BulkImport object by its id.
     * 
     * @param sessionId
     *            session key.
     * @param importId
     *            id of the BulkImport object.
     * @return BulkImport object.
     */
    @SuppressWarnings("unchecked")
    public ASObject retrieveImportData(String sessionId, String importId) {
        Account account = sessionToAccount(sessionId);
        if (account == null) {
            Logger.info("Session is invalid");
            return null;
        }

        Logger.info("RetrieveImportData: " + importId);

        long id = Long.decode(importId);

        try {
            ASObject results = new ASObject();
            BulkImportController controller = new BulkImportController(account);
            BulkImport bi = controller.retrieveById(id);
            String ownerEmail = bi.getAccount().getEmail();
            results.put("type", bi.getType());
            results.put("sequenceZipfile", bi.getSequenceFile());
            results.put("attachmentZipfile", bi.getAttachmentFile());
            results.put("ownerEmail", bi.getAccount().getEmail());

            // primary data
            ArrayCollection primaryData = new ArrayCollection();
            List<BulkImportEntryData> data = bi.getPrimaryData();
            for (BulkImportEntryData datum : data) {
                ASObject obj = new ASObject();
                Entry entry = datum.getEntry();
                entry.setOwnerEmail(ownerEmail);
                obj.put("entry", entry);
                obj.put("attachmentFilename", datum.getAttachmentFilename());
                obj.put("sequenceFilename", datum.getSequenceFilename());
                primaryData.add(obj);
            }
            results.put("primaryData", primaryData);

            // secondary data (if any)
            List<BulkImportEntryData> data2 = bi.getSecondaryData();
            if (data2 != null && !data2.isEmpty()) {
                ArrayCollection secondaryData = new ArrayCollection();
                for (BulkImportEntryData datum : data2) {
                    ASObject obj = new ASObject();
                    Entry entry2 = datum.getEntry();
                    entry2.setOwnerEmail(ownerEmail);
                    obj.put("entry", entry2);
                    obj.put("attachmentFilename", datum.getAttachmentFilename());
                    obj.put("sequenceFilename", datum.getSequenceFilename());
                    secondaryData.add(obj);
                }
                results.put("secondaryData", secondaryData);
            }

            return results;
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);
            return null;
        }
    }

    // end TODO 

    /**
     * Save entries using the bulk import mechanism.
     * 
     * @param sessionId
     *            session key.
     * @param primaryData
     *            BulkImport data
     * @param secondaryData
     *            More BulkImport data
     * @param sequenceZipFile
     *            Zip file containing sequences.
     * @param attachmentZipFile
     *            Zip file containing attachments.
     * @param sequenceFilename
     *            Sequence file name.
     * @param attachmentFilename
     *            Attachment file name.
     * @return Number of entries saved.
     */
    public int saveEntries(String sessionId, ArrayCollection primaryData,
            ArrayCollection secondaryData, Byte[] sequenceZipFile, Byte[] attachmentZipFile,
            String sequenceFilename, String attachmentFilename) {

        Account account = sessionToAccount(sessionId);
        if (account == null) {
            Logger.error("Invalid session");
            return 0;
        }

        BulkImport bulkImport = new BulkImport();
        bulkImport.setAccount(account);
        bulkImport.setAttachmentFile(attachmentZipFile);
        bulkImport.setAttachmentFilename(attachmentFilename);
        bulkImport.setSequenceFilename(sequenceFilename);
        bulkImport.setSequenceFile(sequenceZipFile);

        try {
            // to account for blaze ds issues. change if a solution is found
            ArrayList<BulkImportEntryData> data = new ArrayList<BulkImportEntryData>(
                    primaryData.size());
            ArrayList<BulkImportEntryData> data2 = new ArrayList<BulkImportEntryData>(
                    secondaryData.size());
            ASTranslator ast = new ASTranslator();
            BulkImportEntryData importData;
            ASObject aso;
            String type = "";

            for (int i = 0; i < primaryData.size(); i++) {
                aso = (ASObject) primaryData.get(i);
                aso.setType("org.jbei.ice.lib.utils.BulkImportEntryData");
                importData = (BulkImportEntryData) ast.convert(aso, BulkImportEntryData.class);
                data.add(importData);
                Entry entry = importData.getEntry();
                type = entry.getRecordType();
                entry.setOwnerEmail(account.getEmail());
                entry.setOwner(account.getFullName());
            }

            bulkImport.setPrimaryData(data);

            // secondary data
            if (secondaryData != null && !secondaryData.isEmpty()) {
                for (int i = 0; i < secondaryData.size(); i += 1) {
                    aso = (ASObject) secondaryData.get(i);
                    aso.setType("org.jbei.ice.lib.utils.BulkImportEntryData");
                    importData = (BulkImportEntryData) ast.convert(aso, BulkImportEntryData.class);
                    data2.add(importData);
                    Entry entry = importData.getEntry();
                    entry.setOwnerEmail(account.getEmail());
                    entry.setOwner(account.getFullName());
                }

                bulkImport.setSecondaryData(data2);
                bulkImport.setType("strain w/ plasmid");
            } else {
                bulkImport.setType(type);
            }

            BulkImportController controller = new BulkImportController(account);
            BulkImport savedImport = controller.createBulkImportRecord(bulkImport);
            return savedImport.getPrimaryData().size();
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);
            return 0;
        }
    }

    /**
     * Save the specified {@link Entry} into the database.
     * 
     * @param sessionId
     *            session key.
     * @param importId
     *            ImportId if using bulk import
     * @param entry
     *            Entry to be saved.
     * @param attachmentFile
     *            Attachment file contents as bytes.
     * @param attachmentFilename
     *            Attachment file name.
     * @param sequenceFile
     *            Sequence file as bytes.
     * @param sequenceFilename
     *            Sequence file name.
     * @return saved Entry.
     */
    public Entry saveEntry(String sessionId, String importId, Entry entry, Byte[] attachmentFile,
            String attachmentFilename, Byte[] sequenceFile, String sequenceFilename) {

        Account account = sessionToAccount(sessionId);
        if (account == null) {
            return null;
        }

        try {
            BulkImportController controller = new BulkImportController(account);
            BulkImport bi = controller.retrieveById(Integer.decode(importId));
            String email = bi.getAccount().getEmail();
            entry.setOwnerEmail(email);
            entry.setOwner(bi.getAccount().getFullName());
            entry.setCreatorEmail(bi.getAccount().getEmail());
            entry.setCreator(bi.getAccount().getFullName());
        } catch (Exception e1) {
        }

        if ("part".equalsIgnoreCase(entry.getRecordType())) {
            ((Part) entry).setPackageFormat(Part.AssemblyStandard.RAW);
        }

        if (entry.getLongDescriptionType() == null) {
            entry.setLongDescriptionType(Entry.MarkupType.text.name());
        }

        EntryController entryController = new EntryController();
        Entry saved = null;
        try {
            saved = entryController.createEntry(entry);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);
            return null;
        }

        // save sequence
        saveEntrySequence(account, entry, sequenceFile, sequenceFilename);

        // save attachment
        saveEntryAttachment(account, entry, attachmentFile, attachmentFilename);

        return saved;
    }

    /**
     * Save a {@link Strain} with a single {@link Plasmid}.
     * 
     * @param sessionId
     *            session key.
     * @param importId
     *            BulkImport id, if any.
     * @param strain
     *            Strain object.
     * @param plasmid
     *            Plasmid object.
     * @param strainSequenceFile
     *            Sequence file content for Strain.
     * @param strainSequenceFilename
     *            Sequence file name for Strain.
     * @param strainAttachmentFile
     *            Attachment file content for Strain.
     * @param strainAttachmentFilename
     *            Attachment file name for Strain.
     * @param plasmidSequenceFile
     *            Sequence file content for Plasmid.
     * @param plasmidSequenceFilename
     *            Sequence file name for Plasmid.
     * @param plasmidAttachmentFile
     *            Attachment file content for Plasmid.
     * @param plasmidAttachmentFilename
     *            Attachment file name for Plasmid.
     * @return List of saved Entries.
     */
    public List<Entry> saveStrainWithPlasmid(String sessionId, String importId, Strain strain,
            Plasmid plasmid, Byte[] strainSequenceFile, String strainSequenceFilename,
            Byte[] strainAttachmentFile, String strainAttachmentFilename,
            Byte[] plasmidSequenceFile, String plasmidSequenceFilename,
            Byte[] plasmidAttachmentFile, String plasmidAttachmentFilename) {

        Account account = sessionToAccount(sessionId);
        if (account == null) {
            return null;
        }

        // set accounts
        try {
            BulkImportController controller = new BulkImportController(account);
            BulkImport bi = controller.retrieveById(Integer.decode(importId));
            String email = bi.getAccount().getEmail();
            String owner = bi.getAccount().getFullName();

            strain.setOwnerEmail(email);
            strain.setOwner(owner);
            plasmid.setOwnerEmail(email);
            plasmid.setOwner(owner);

            strain.setCreatorEmail(email);
            strain.setCreator(owner);
            plasmid.setCreatorEmail(email);
            plasmid.setCreator(owner);
        } catch (Exception e1) {
        }

        // strain
        if (strain.getLongDescriptionType() == null) {
            strain.setLongDescriptionType(Entry.MarkupType.text.name());
        }

        // plasmid
        if (plasmid.getLongDescriptionType() == null) {
            plasmid.setLongDescriptionType(Entry.MarkupType.text.name());
        }

        EntryController entryController = new EntryController();

        // save plasmid
        Plasmid newPlasmid = null;
        Strain newStrain = null;

        try {
            newPlasmid = (Plasmid) entryController.createEntry(plasmid);
            String plasmidPartNumberString = "[[" + JbeirSettings.getSetting("WIKILINK_PREFIX")
                    + ":" + newPlasmid.getOnePartNumber().getPartNumber() + "|"
                    + newPlasmid.getOneName().getName() + "]]";
            strain.setPlasmids(plasmidPartNumberString);
            newStrain = (Strain) entryController.createEntry(strain);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);
            return null;
        }

        List<Entry> saved = new LinkedList<Entry>();
        if (newPlasmid != null) {
            saved.add(newPlasmid);
        }
        if (newStrain != null) {
            saved.add(newStrain);
        }

        // save sequences
        saveEntrySequence(account, newPlasmid, plasmidSequenceFile, plasmidSequenceFilename);
        saveEntrySequence(account, newStrain, strainSequenceFile, strainSequenceFilename);

        // save attachments
        saveEntryAttachment(account, newPlasmid, plasmidAttachmentFile, plasmidAttachmentFilename);
        saveEntryAttachment(account, newStrain, strainAttachmentFile, strainAttachmentFilename);

        return saved;
    }

    /**
     * Save the given sequence file with the given {@link Entry}.
     * 
     * @param account
     * @{link Account} to save as.
     * @param entry
     *            Entry to associate with.
     * @param fileBytes
     *            Sequence file content.
     * @param filename
     *            Sequence file name.
     */
    private void saveEntrySequence(Account account, Entry entry, Byte[] fileBytes, String filename) {
        if (fileBytes == null || entry == null) {
            return;
        }
        byte[] input = new byte[fileBytes.length];
        for (int i = 0; i < fileBytes.length; i += 1) {
            input[i] = fileBytes[i].byteValue();
        }

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(System.getProperty("java.io.tmpdir")
                    + File.separatorChar + filename);
            outputStream.write(input);
            outputStream.close();

            File file = new File(System.getProperty("java.io.tmpdir"), filename);
            createSequence(account, entry, file);
        } catch (FileNotFoundException e) {
            Logger.error(getLoggerPrefix(), e);
        } catch (IOException e) {
            Logger.error(getLoggerPrefix(), e);
        }
    }

    /**
     * Save the given attachment to the server.
     * 
     * @param account
     *            {@link Account} to save as.
     * @param entry
     *            {@link Entry} to associate attachment with.
     * @param fileBytes
     *            Attachment file content.
     * @param filename
     *            Attachment file name.
     */
    private void saveEntryAttachment(Account account, Entry entry, Byte[] fileBytes, String filename) {
        if (fileBytes == null) {
            return;
        }

        byte[] inputBytes = new byte[fileBytes.length];
        for (int i = 0; i < fileBytes.length; i += 1) {
            inputBytes[i] = fileBytes[i].byteValue();
        }

        AttachmentController controller = new AttachmentController(account);
        ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
        Attachment attachment = new Attachment();
        attachment.setFileName(filename);
        attachment.setDescription("");
        attachment.setEntry(entry);

        try {
            controller.save(attachment, bais);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);
        } catch (PermissionException e) {
            Logger.error(getLoggerPrefix(), e);
        }
    }

    /**
     * Save the given sequence file with the given {@link Entry}.
     * 
     * @param account
     *            {@link Account} to save as.
     * @param entry
     *            Entry to associate with.
     * @param seqFile
     *            -Sequence file.
     * @throws IOException
     */
    private void createSequence(Account account, Entry entry, File seqFile) throws IOException {
        // set sequence
        SequenceController sequenceController = new SequenceController(account);

        String sequenceUser = readSequenceFile(seqFile).toString();
        IDNASequence dnaSequence = null;
        if (sequenceUser != null) {
            dnaSequence = SequenceController.parse(sequenceUser);
        }

        if (dnaSequence == null) {
            Logger.info("Could not parse sequence file. Perhaps file is not supported");
        } else {
            try {
                Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
                sequence.setSequenceUser(sequenceUser);
                sequence.setEntry(entry);
                sequenceController.save(sequence);
            } catch (ControllerException e) {
                Logger.error(getLoggerPrefix(), e);
            } catch (PermissionException e) {
                Logger.error(getLoggerPrefix(), e);
            }
        }
    }

    /**
     * Convert the given file to StringBuilder.
     * 
     * @param seqFile
     *            file to convert.
     * @return StringBuilder object with the content of the file.
     */
    private StringBuilder readSequenceFile(File seqFile) {
        StringBuilder sequenceStringBuilder = new StringBuilder();
        if (seqFile.canRead()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(seqFile));

                while (true) {
                    try {
                        String temp = br.readLine();
                        if (temp != null) {
                            sequenceStringBuilder.append(temp + '\n');
                        } else {
                            break;
                        }

                    } catch (IOException e) {
                        return null;
                    }
                }
            } catch (FileNotFoundException e1) {
                Logger.error(getLoggerPrefix(), e1);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return sequenceStringBuilder;
    }

    /**
     * Retrieve unique origin of replications from the database.
     * 
     * @return Set of unique origins.
     */
    public TreeSet<String> getUniqueOriginOfReplications() {
        return UtilsManager.getUniqueOriginOfReplications();
    }

    /**
     * Retrieve all the unique {@link SelectionMarker}s as collection of Strings.
     * 
     * @return Set of unique SelectionMarkers.
     */
    public TreeSet<String> getUniqueSelectionMarkers() {
        try {
            return UtilsManager.getUniqueSelectionMarkers();
        } catch (ManagerException e) {
            Logger.error(getLoggerPrefix(), e);
            return null;
        }
    }

    /**
     * Retrieve unique promoters from the database.
     * 
     * @return Set of unique promoters.
     */
    public TreeSet<String> getUniquePromoters() {
        return UtilsManager.getUniquePromoters();
    }
}