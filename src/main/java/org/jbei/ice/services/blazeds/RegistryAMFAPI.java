package org.jbei.ice.services.blazeds;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jbei.ice.bio.enzymes.RestrictionEnzyme;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManager;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManagerException;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.ProjectController;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.composers.SequenceComposerException;
import org.jbei.ice.lib.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.Attachment;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Project;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.AssemblyHelper;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.lib.utils.SerializationUtils.SerializationUtilsException;
import org.jbei.ice.lib.utils.TraceAlignmentHelper;
import org.jbei.ice.lib.vo.AssemblyProject;
import org.jbei.ice.lib.vo.AssemblyTable;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;
import org.jbei.ice.lib.vo.PermutationSet;
import org.jbei.ice.lib.vo.SequenceCheckerData;
import org.jbei.ice.lib.vo.SequenceCheckerProject;
import org.jbei.ice.lib.vo.TraceData;
import org.jbei.ice.lib.vo.VectorEditorProject;
import org.jbei.ice.services.blazeds.vo.UserPreferences;
import org.jbei.ice.services.blazeds.vo.UserRestrictionEnzymes;

public class RegistryAMFAPI extends BaseService {
    public Entry getEntry(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController(account);

        Entry entry = null;
        try {
            entry = entryController.getByRecordId(entryId);
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

    public boolean hasWritablePermissions(String sessionId, String entryId) {
        boolean result = false;

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return result;
        }

        EntryController entryController = new EntryController(account);

        try {
            Entry entry = entryController.getByRecordId(entryId);

            if (entry != null) {
                result = entryController.hasWritePermission(entry);
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

    public FeaturedDNASequence getSequence(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController(account);

        Entry entry = null;
        try {
            entry = entryController.getByRecordId(entryId);
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

    public boolean saveSequence(String sessionId, String entryId,
            FeaturedDNASequence featuredDNASequence) {
        boolean result = false;

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return result;
        }

        EntryController entryController = new EntryController(account);
        SequenceController sequenceController = new SequenceController(account);

        try {
            Entry entry = entryController.getByRecordId(entryId);

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

    public ArrayList<TraceSequence> getTraces(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController(account);
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                account);

        Entry entry;
        List<TraceSequence> traces;
        try {
            entry = entryController.getByRecordId(entryId);

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
                            .deserializeFromString(accountPreferences.getPreferences());
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
                serializedPreferences = SerializationUtils.serializeToString(preferences);
            } catch (SerializationUtils.SerializationUtilsException e) {
                Logger.error(getLoggerPrefix(), e);

                return false;
            }

            if (accountPreferences != null) {
                accountPreferences.setPreferences(serializedPreferences);

                AccountController.saveAccountPreferences(accountPreferences);
            } else {
                AccountController.saveAccountPreferences(new AccountPreferences(account,
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
                        .deserializeFromString(accountPreferences.getRestrictionEnzymes());
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
                    .serializeToString(userRestrictionEnzymes);

            if (accountPreferences != null) {
                accountPreferences.setRestrictionEnzymes(serializedUserRestrictionEnzymes);

                AccountController.saveAccountPreferences(accountPreferences);
            } else {
                AccountController.saveAccountPreferences(new AccountPreferences(account, "",
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

    public AssemblyProject createAssemblyProject(String sessionId, AssemblyProject assemblyProject) {
        if (assemblyProject == null || sessionId == null) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        String serializedAssemblyTable = "";

        try {
            serializedAssemblyTable = SerializationUtils.serializeToString(assemblyProject
                    .getAssemblyTable());
        } catch (SerializationUtilsException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        ProjectController projectController = new ProjectController(account);

        Project project = projectController.createProject(account, assemblyProject.getName(),
            assemblyProject.getDescription(), serializedAssemblyTable, assemblyProject.typeName(),
            new Date(), new Date());

        try {
            Project savedProject = projectController.save(project);

            assemblyProject.setName(savedProject.getName());
            assemblyProject.setDescription(savedProject.getDescription());
            assemblyProject.setUuid(savedProject.getUuid());
            assemblyProject.setOwnerEmail(savedProject.getAccount().getEmail());
            assemblyProject.setOwnerName(savedProject.getAccount().getFullName());
            assemblyProject.setCreationTime(savedProject.getCreationTime());
            assemblyProject.setModificationTime(savedProject.getModificationTime());
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (PermissionException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return assemblyProject;
    }

    public AssemblyProject getAssemblyProject(String sessionId, String projectId) {
        if (projectId == null || sessionId == null || sessionId.isEmpty() || projectId.isEmpty()) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        AssemblyProject assemblyProject = null;

        ProjectController projectController = new ProjectController(account);
        try {
            Project project = projectController.getProjectByUUID(projectId);

            AssemblyTable assemblyTable = (AssemblyTable) SerializationUtils
                    .deserializeFromString(project.getData());

            assemblyProject = new AssemblyProject(project.getName(), project.getDescription(),
                    project.getUuid(), account.getEmail(), account.getFullName(),
                    project.getCreationTime(), project.getModificationTime(), assemblyTable);
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        } catch (SerializationUtilsException e) {
            Logger.error(getLoggerPrefix(), e);

            return null;
        }

        return assemblyProject;
    }

    public AssemblyProject saveAssemblyProject(String sessionId, AssemblyProject assemblyProject) {
        if (sessionId == null || sessionId.isEmpty() || assemblyProject == null
                || assemblyProject.getUuid() == null) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        AssemblyProject resultAssemblyProject = null;

        ProjectController projectController = new ProjectController(account);
        try {
            Project project = projectController.getProjectByUUID(assemblyProject.getUuid());

            project.setName(assemblyProject.getName());
            project.setDescription(assemblyProject.getDescription());
            project.setModificationTime(new Date());
            project.setData(SerializationUtils.serializeToString(assemblyProject.getAssemblyTable()));

            Project savedProject = projectController.save(project);

            resultAssemblyProject = new AssemblyProject(savedProject.getName(),
                    savedProject.getDescription(), savedProject.getUuid(), savedProject
                            .getAccount().getEmail(), savedProject.getAccount().getFullName(),
                    savedProject.getCreationTime(), savedProject.getModificationTime(),
                    assemblyProject.getAssemblyTable());
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

        return resultAssemblyProject;
    }

    public PermutationSet assembleAssemblyProject(String sessionId, AssemblyProject assemblyProject) {
        if (sessionId == null || sessionId.isEmpty() || assemblyProject == null) {
            return null;
        }

        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        PermutationSet permutationSet = AssemblyHelper.buildPermutationSet(assemblyProject
                .getAssemblyTable());

        return permutationSet;
    }

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
                    .serializeToString(sequenceCheckerProject.getSequenceCheckerData());
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
            project.setData(SerializationUtils.serializeToString(sequenceCheckerProject
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
                    .deserializeFromString(project.getData());

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
            serializedVectorEditorData = SerializationUtils.serializeToString(vectorEditorProject
                    .getFeaturedDNASequence());
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
            project.setData(SerializationUtils.serializeToString(vectorEditorProject
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
                    .deserializeFromString(project.getData());

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

    private Account sessionToAccount(String sessionId) {
        if (sessionId == null || sessionId.isEmpty())
            return null;

        return getAccountBySessionId(sessionId);
    }

    private ZipFile createZipFile(Byte[] byteArray, String filename) throws IOException {
        byte[] input = new byte[byteArray.length];
        for (int i = 0; i < byteArray.length; i += 1) {
            input[i] = byteArray[i].byteValue();
        }

        FileOutputStream outputStream = new FileOutputStream(System.getProperty("java.io.tmpdir")
                + File.separatorChar + filename);
        outputStream.write(input);
        outputStream.close();

        File file = new File(System.getProperty("java.io.tmpdir"), filename);
        return new ZipFile(file);
    }

    public List<Attachment> saveAttachments(String sessionId, List<Attachment> attachments,
            Byte[] byteArray) {

        Account account = this.sessionToAccount(sessionId);
        if (account == null || byteArray == null) {
            return null;
        }
        List<Attachment> saved = new LinkedList<Attachment>();

        // restore attachment zip file
        try {
            ZipFile zipFile = this.createZipFile(byteArray, "attachment.zip");

            // extract
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            AttachmentController controller = new AttachmentController(account);

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.isDirectory())
                    continue;

                for (Attachment attachment : attachments) {
                    if (zipEntry.getName().endsWith(attachment.getFileName())) {
                        InputStream in = zipFile.getInputStream(zipEntry);
                        try {
                            if (attachment.getDescription() == null)
                                attachment.setDescription("");
                            Attachment savedAtt = controller.save(attachment, in, false);
                            saved.add(savedAtt);
                        } catch (ControllerException e) {
                            Logger.error(getLoggerPrefix(), e);
                        } catch (PermissionException e) {
                            Logger.error(getLoggerPrefix(), e);
                        }
                    }
                }

                break;
            }
            zipFile.close();
        } catch (IOException ioe) {
            Logger.error(getLoggerPrefix(), ioe);
            return saved;
        }

        return saved;
    }

    /**
     * Saves "Entry"s and sequence files if any
     * 
     * @param sessionId
     * @param parts
     * @param sequences
     * @param byteArray
     * @param filename
     * @return
     */
    public List<Entry> saveEntries(String sessionId, List<Entry> parts, List<Sequence> sequences,
            Byte[] byteArray, String filename) {

        Account account = this.sessionToAccount(sessionId);
        if (account == null) {
            return null;
        }

        EntryController entryController = new EntryController(account);
        ZipFile zip = null;

        try {
            if (sequences != null && sequences.size() > 0) {
                zip = this.createZipFile(byteArray, filename);
            }

            List<Entry> savedParts = new LinkedList<Entry>();

            for (int i = 0; i < parts.size(); i += 1) {
                Entry part = parts.get(i);

                part.setCreatorEmail(account.getEmail());
                part.setCreator(account.getFullName());

                part.setOwner(account.getFullName());
                part.setOwnerEmail(account.getEmail());
                if (Entry.PART_ENTRY_TYPE.equals(part.getRecordType()))
                    ((Part) part).setPackageFormat(Part.AssemblyStandard.RAW);

                Entry newEntry = entryController.createEntry(part);
                savedParts.add(newEntry);

                if (zip != null) {
                    Sequence sequence = (sequences.size() > i) ? sequences.get(i) : null;
                    this.createSequence(account, part, sequence, zip);
                }
            }
            return savedParts;
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);
            return null;
        } catch (IOException e) {
            Logger.error(getLoggerPrefix(), e);
            return null;
        }
    }

    private void createSequence(Account account, Entry entry, Sequence sequence, ZipFile zip)
            throws IOException {
        if (sequence == null)
            return;

        String sequenceFileName = sequence.getSequenceUser();

        if (zip == null || sequenceFileName == null || "".equals(sequenceFileName))
            return;

        // create sequence entry
        Enumeration<? extends ZipEntry> entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.isDirectory() || !zipEntry.getName().endsWith(sequenceFileName))
                continue;

            InputStream in = zip.getInputStream(zipEntry);

            int count;
            byte data[] = new byte[1000];
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
                    System.getProperty("java.io.tmpdir") + File.separatorChar + sequenceFileName),
                    1000);
            while ((count = in.read(data, 0, 1000)) != -1) {
                out.write(data, 0, count);
            }
            out.flush();
            out.close();

            break;
        }
        zip.close();

        File seqFile = new File(System.getProperty("java.io.tmpdir"), sequenceFileName);
        if (!seqFile.exists()) {
            Logger.info("Sequence file not found. Looked in \"" + seqFile.getAbsolutePath() + "\"");
            return;
        }

        // set sequence
        SequenceController sequenceController = new SequenceController(account);

        String sequenceUser = this.readSequenceFile(seqFile).toString();
        IDNASequence dnaSequence = null;
        if (sequenceUser != null) {
            dnaSequence = SequenceController.parse(sequenceUser);
        }

        if (dnaSequence == null) {
            Logger.info("Could not parse sequence file. Perhaps file is not supported");
        } else {
            try {
                sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
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
                if (br != null)
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
            }
        }

        return sequenceStringBuilder;
    }

    public TreeSet<String> getUniqueOriginOfReplications() {
        return UtilsManager.getUniqueOriginOfReplications();
    }

    public TreeSet<String> getUniqueSelectionMarkers() {
        try {
            return UtilsManager.getUniqueSelectionMarkers();
        } catch (ManagerException e) {
            Logger.error(getLoggerPrefix(), e);
            return null;
        }
    }

    public TreeSet<String> getUniquePromoters() {
        return UtilsManager.getUniquePromoters();
    }
}