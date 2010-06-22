package org.jbei.ice.services.blazeds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbei.ice.bio.enzymes.RestrictionEnzyme;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManager;
import org.jbei.ice.bio.enzymes.RestrictionEnzymesManagerException;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.composers.SequenceComposerException;
import org.jbei.ice.lib.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
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

    public void saveUserPreferences(String sessionId, UserPreferences preferences) {
        try {
            Account account = getAccountBySessionId(sessionId);

            if (account == null) {
                return;
            }

            AccountPreferences accountPreferences = AccountController
                    .getAccountPreferences(account);

            String serializedPreferences = "";
            try {
                serializedPreferences = SerializationUtils.serializeToString(preferences);
            } catch (SerializationUtils.SerializationUtilsException e) {
                Logger.error(getLoggerPrefix(), e);
            }

            if (accountPreferences != null) {
                accountPreferences.setPreferences(serializedPreferences);

                AccountController.saveAccountPreferences(accountPreferences);
            } else {
                AccountController.saveAccountPreferences(new AccountPreferences(account,
                        serializedPreferences, ""));
            }

            logInfo(account.getEmail() + " saveUserPreferences");
        } catch (ControllerException e) {
            Logger.error(getLoggerPrefix(), e);
        } catch (Exception e) {
            Logger.error(getLoggerPrefix(), e);
        }
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
}
