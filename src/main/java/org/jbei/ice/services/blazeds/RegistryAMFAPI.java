package org.jbei.ice.services.blazeds;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbei.ice.ControllerException;
import org.jbei.ice.bio.enzymes.RestrictionEnzyme;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManager;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManagerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.account.model.AccountPreferences;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntryRetriever;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceAnalysisController;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.lib.utils.TraceAlignmentHelper;
import org.jbei.ice.lib.vo.DNASequence;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.SequenceCheckerProject;
import org.jbei.ice.lib.vo.TraceData;
import org.jbei.ice.services.blazeds.vo.UserPreferences;
import org.jbei.ice.services.blazeds.vo.UserRestrictionEnzymes;

/**
 * BlazeDS service calls for Flex applications.
 *
 * @author Zinovii Dmytriv, Hector Plahar, Joanna Chen, Timothy Ham
 */
public class RegistryAMFAPI extends BaseService {

    /**
     * Determine if the session has write permission to the specified {@link Entry}.
     *
     * @param sessionId session key.
     * @param entryId   recordId.
     * @return True if session has write permission to the Entry.
     */
    public boolean hasWritablePermissions(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);
        if (account == null) {
            return false;
        }

        EntryAuthorization authorization = new EntryAuthorization();
        return authorization.canWrite(account.getEmail(), DAOFactory.getEntryDAO().getByRecordId(entryId));
    }

    /**
     * Retrieve the {@link FeaturedDNASequence} of the specified {@link Entry}.
     *
     * @param sessionId session key.
     * @param entryId   recordIf of the desired Entry.
     * @return FeaturedDNASequence object.
     */
    public FeaturedDNASequence getSequence(String sessionId, String entryId) {
        Account account = getAccountBySessionId(sessionId);

        if (account == null) {
            return null;
        }

        return new SequenceController().retrievePartSequence(account.getEmail(), entryId);
    }

    /**
     * Save the given {@link FeaturedDNASequence} with the specified {@link Entry}.
     *
     * @param sessionId           session key.
     * @param entryId             recordId of the desired Entry.
     * @param featuredDNASequence featuredDNASequence object to save.
     * @return True if successful.
     */
    public boolean saveSequence(String sessionId, String entryId, FeaturedDNASequence featuredDNASequence) {
        Account account = getAccountBySessionId(sessionId);
        if (account == null) {
            return false;
        }

        EntryRetriever retriever = new EntryRetriever();
        SequenceController sequenceController = new SequenceController();

        try {
            Entry entry = retriever.getByRecordId(account.getEmail(), entryId);
            if (entry == null) {
                return false;
            }

            Sequence existing = DAOFactory.getSequenceDAO().getByEntry(entry);
            if (existing != null) {
                Files.deleteIfExists(Paths.get(existing.getFwdHash() + ".png"));
            }
            Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);
            sequence.setEntry(entry);
            sequenceController.update(account, sequence);

            logInfo(account.getEmail() + " saveSequence: " + entryId);
            return true;
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);
            return false;
        }
    }

    /**
     * Retrieve {@link TraceSequence}s of the specified {@link Entry}.
     *
     * @param sessionId session key.
     * @param recordId  recordId of the desired Entry.
     * @return - List of TraceSequence objects.
     */
    public ArrayList<TraceSequence> getTraces(String sessionId, String recordId) {
        String userId = SessionHandler.getUserIdBySession(sessionId);
        if (userId == null) {
            return null;
        }

        EntryRetriever retriever = new EntryRetriever();
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();

        Entry entry;
        List<TraceSequence> traces;
        try {
            entry = retriever.getByRecordId(userId, recordId);
            if (entry == null) {
                return null;
            }

            traces = sequenceAnalysisController.getTraceSequences(entry);
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
            result = new SequenceController().compose(sequence, genbankFormatter);
            logInfo(account.getEmail() + " generated and fetched genbank sequence");
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
                new AccountController().saveAccountPreferences(accountPreferences);
            } else {
                new AccountController().saveAccountPreferences(
                        new AccountPreferences(account, serializedPreferences, ""));
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
        } catch (SerializationUtils.SerializationUtilsException | ControllerException e) {
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
    public void saveUserRestrictionEnzymes(String sessionId, UserRestrictionEnzymes userRestrictionEnzymes) {
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
                new AccountController().saveAccountPreferences(accountPreferences);
            } else {
                new AccountController().saveAccountPreferences(
                        new AccountPreferences(account, "", serializedUserRestrictionEnzymes));
            }

            logInfo(account.getEmail() + " saveUserRestrictionEnzymes");
        } catch (SerializationUtils.SerializationUtilsException | ControllerException e) {
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
            result = new SequenceController().compose(sequence, genbankFormatter);

            logInfo("Generated and fetched sequence");
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
                for (int i = 0; i < sequenceCheckerProject.getSequenceCheckerData().getTraces().size(); i++) {
                    TraceData traceData = sequenceCheckerProject.getSequenceCheckerData().getTraces().get(i);
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
                TraceData traceData = sequenceCheckerProject.getSequenceCheckerData().getTraces().get(i);

                TraceData alignedTraceData = TraceAlignmentHelper.alignSequences(
                        sequenceCheckerProject.getSequenceCheckerData().getSequence().getSequence(),
                        traceData.getSequence(), traceData.getFilename(), sequenceCheckerProject
                                .getSequenceCheckerData().getSequence().getIsCircular()
                                                                                );

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

        try {
            DNASequence dnaSequence = new SequenceAnalysisController().parse(data);

            if (dnaSequence == null) {
                logInfo("Failed to parse trace file!");
            } else {
                traceData = new TraceData(traceFileName, dnaSequence.getSequence(), -1, -1, -1, -1, -1, -1, "", "");
                logInfo("Successfully parsed trace file");
            }
        } catch (Exception e) {
            Logger.error(getServiceName(), e);
        }

        return traceData;
    }
}