package org.jbei.ice.services.blazeds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jbei.ice.bio.enzymes.RestrictionEnzyme;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManager;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManagerException;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.AccountPreferences;
import org.jbei.ice.lib.composers.SequenceComposerException;
import org.jbei.ice.lib.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.dao.hibernate.HibernateHelper;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Project;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionsController;
import org.jbei.ice.lib.project.ProjectController;
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

/**
 * BlazeDS service calls for Flex applications.
 *
 * @author Zinovii Dmytriv, Hector Plahar, Joanna Chen, Timothy Ham
 */
public class RegistryAMFAPI extends BaseService {
    /**
     * Retrieve {@link Entry} by its recordId.
     *
     * @param sessionId session key.
     * @param entryId   recordId.
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
     * @param sessionId session key.
     * @param entryId   recordId.
     * @return True if session has write permission to the Entry.
     */
    public boolean hasWritablePermissions(String sessionId, String entryId) {
        boolean result = false;

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return result;
        }

        EntryController entryController = new EntryController();
        PermissionsController permissionsController = new PermissionsController();

        try {
            Entry entry = entryController.getByRecordId(account, entryId);

            if (entry != null) {
                result = permissionsController.hasWritePermission(account, entry);
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
     * @param sessionId session key.
     * @param entryId   recordIf of the desired Entry.
     * @return FeaturedDNASequence object.
     */
    public FeaturedDNASequence getSequence(String sessionId, String entryId) {
        HibernateHelper.beginTransaction();
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
        SequenceController sequenceController = new SequenceController();
        try {
            Sequence sequence = sequenceController.getByEntry(entry);

            featuredDNASequence = SequenceController.sequenceToDNASequence(sequence);
        } catch (ControllerException e) {
            Logger.error("Failed to get entry!", e);

            return null;
        }

        HibernateHelper.commitTransaction();
        return featuredDNASequence;
    }

    /**
     * Save the given {@link FeaturedDNASequence} with the specified {@link Entry}.
     *
     * @param sessionId           session key.
     * @param entryId             recordId of the desired Entry.
     * @param featuredDNASequence featuredDNASequence object to save.
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
        SequenceController sequenceController = new SequenceController();

        try {
            Entry entry = entryController.getByRecordId(account, entryId);

            if (entry == null) {
                return false;
            }

            Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);

            sequence.setEntry(entry);
            sequenceController.update(account, sequence);

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
     * @param sessionId session key.
     * @param entryId   recordId of the desired Entry.
     * @return - List of TraceSequence objects.
     */
    public ArrayList<TraceSequence> getTraces(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController();
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();

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
     * @param sessionId           session key.
     * @param featuredDNASequence FeaturedDNASequence object.
     * @param name                Locus name to be used.
     * @param isCircular          True if circular.
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
     * @param sessionId session key.
     * @return UserPreferences object for the specified user.
     */
    public UserPreferences getUserPreferences(String sessionId) {
        AccountController controller = new AccountController();
        UserPreferences userPreferences = null;
        try {
            Account account = getAccountBySessionId(sessionId);

            if (account == null) {
                return null;
            }

            AccountPreferences accountPreferences = controller.getAccountPreferences(account);

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
     * @param sessionId   session key.
     * @param preferences UserPreferences obect.
     * @return True if successful.
     */
    public boolean saveUserPreferences(String sessionId, UserPreferences preferences) {
        try {
            Account account = getAccountBySessionId(sessionId);
            AccountController controller = new AccountController();
            if (account == null) {
                return false;
            }

            AccountPreferences accountPreferences = controller.getAccountPreferences(account);

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
     * @param sessionId session key.
     * @return UserRestrictionEnzymes object for the current user.
     */
    public UserRestrictionEnzymes getUserRestrictionEnzymes(String sessionId) {
        UserRestrictionEnzymes userRestrictionEnzymes = null;
        AccountController controller = new AccountController();

        try {
            Account account = getAccountBySessionId(sessionId);

            if (account == null) {
                return null;
            }

            AccountPreferences accountPreferences = controller.getAccountPreferences(account);

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
     * @param sessionId              session key.
     * @param userRestrictionEnzymes UserRestrictionEnzymes object to save.
     */
    public void saveUserRestrictionEnzymes(String sessionId,
            UserRestrictionEnzymes userRestrictionEnzymes) {
        try {
            AccountController controller = new AccountController();
            Account account = getAccountBySessionId(sessionId);

            if (account == null) {
                return;
            }

            AccountPreferences accountPreferences = controller.getAccountPreferences(account);

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
     * @param sessionId session key.
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
     * @param data data to parse.
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
     * @param featuredDNASequence FeaturedDNASequence object to convert to genbank.
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
     * @param sessionId              session key.
     * @param sequenceCheckerProject SequenceCheckerProject to save.
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

        ProjectController projectController = new ProjectController();

        Project project = projectController.createProject(account,
                                                          sequenceCheckerProject.getName(),
                                                          sequenceCheckerProject.getDescription(),
                                                          serializedSequenceCheckerData,
                                                          sequenceCheckerProject.typeName(), new Date(),
                                                          new Date());

        try {
            Project savedProject = projectController.save(account, project);

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
     * @param sessionId              session key.
     * @param sequenceCheckerProject SequenceCheckerProject to save.
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

        ProjectController projectController = new ProjectController();
        try {
            Project project = projectController.getProjectByUUID(sequenceCheckerProject.getUuid());

            project.setName(sequenceCheckerProject.getName());
            project.setDescription(sequenceCheckerProject.getDescription());
            project.setModificationTime(new Date());
            project.setData(SerializationUtils.serializeObjectToString(sequenceCheckerProject
                                                                               .getSequenceCheckerData()));

            Project savedProject = projectController.save(account, project);

            resultSequenceCheckerProject = new SequenceCheckerProject(savedProject.getName(),
                                                                      savedProject.getDescription(),
                                                                      savedProject.getUuid(), savedProject
                    .getAccount().getEmail(), savedProject.getAccount().getFullName(),
                                                                      savedProject.getCreationTime(),
                                                                      savedProject.getModificationTime(),
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
     * @param sessionId session key.
     * @param projectId uuid of the SequenceCheckerProject to retrieve.
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

        ProjectController projectController = new ProjectController();
        try {
            Project project = projectController.getProjectByUUID(projectId);

            SequenceCheckerData sequenceCheckerData = (SequenceCheckerData) SerializationUtils
                    .deserializeStringToObject(project.getData());

            sequenceCheckerProject = new SequenceCheckerProject(project.getName(),
                                                                project.getDescription(), project.getUuid(),
                                                                account.getEmail(),
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
     * @param sessionId              session key.
     * @param sequenceCheckerProject SequenceCheckerProject
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
     * @param traceFileName name of the trace file.
     * @param data          byte array data.
     * @return TraceData object.
     */
    public TraceData parseTraceFile(String traceFileName, byte[] data) {
        TraceData traceData = null;

        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();

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
     * @param sessionId           session key.
     * @param vectorEditorProject VectorEditorProject to create.
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

        ProjectController projectController = new ProjectController();

        Project project = projectController.createProject(account, vectorEditorProject.getName(),
                                                          vectorEditorProject.getDescription(),
                                                          serializedVectorEditorData,
                                                          vectorEditorProject.typeName(), new Date(), new Date());

        try {
            Project savedProject = projectController.save(account, project);

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
     * @param sessionId           session key.
     * @param vectorEditorProject VectorEditorProject to save.
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

        ProjectController projectController = new ProjectController();
        try {
            Project project = projectController.getProjectByUUID(vectorEditorProject.getUuid());

            project.setName(vectorEditorProject.getName());
            project.setDescription(vectorEditorProject.getDescription());
            project.setModificationTime(new Date());
            project.setData(SerializationUtils.serializeObjectToString(vectorEditorProject
                                                                               .getFeaturedDNASequence()));

            Project savedProject = projectController.save(account, project);

            resultVectorEditorProject = new VectorEditorProject(savedProject.getName(),
                                                                savedProject.getDescription(), savedProject.getUuid(),
                                                                savedProject
                                                                        .getAccount().getEmail(),
                                                                savedProject.getAccount().getFullName(),
                                                                savedProject.getCreationTime(),
                                                                savedProject.getModificationTime(),
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
     * @param sessionId session key.
     * @param projectId UUID of the desired VectorEditorProject.
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

        ProjectController projectController = new ProjectController();
        try {
            Project project = projectController.getProjectByUUID(projectId);

            FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) SerializationUtils
                    .deserializeStringToObject(project.getData());

            vectorEditorProject = new VectorEditorProject(project.getName(),
                                                          project.getDescription(), project.getUuid(),
                                                          account.getEmail(),
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
}