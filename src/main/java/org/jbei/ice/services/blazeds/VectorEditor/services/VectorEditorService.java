package org.jbei.ice.services.blazeds.VectorEditor.services;

import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountPreferences;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.SerializationUtils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.services.blazeds.VectorEditor.vo.UserPreferences;
import org.jbei.ice.services.blazeds.VectorEditor.vo.UserRestrictionEnzymes;
import org.jbei.ice.services.blazeds.common.BaseService;

public class VectorEditorService extends BaseService {
    public final static String VECTOR_EDITOR_SERVICE_NAME = "VectorEditorService";

    public UserRestrictionEnzymes getUserRestrictionEnzymes(String authToken) {
        UserRestrictionEnzymes userRestrictionEnzymes = null;

        try {
            Account account = getAccountByToken(authToken);

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

    public void saveUserRestrictionEnzymes(String authToken,
            UserRestrictionEnzymes userRestrictionEnzymes) {
        try {
            Account account = getAccountByToken(authToken);

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

    public UserPreferences getUserPreferences(String authToken) {
        UserPreferences userPreferences = null;
        try {
            Account account = getAccountByToken(authToken);

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

    public void saveUserPreferences(String authToken, UserPreferences preferences) {
        try {
            Account account = getAccountByToken(authToken);

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

    public boolean saveFeaturedDNASequence(String authToken, String entryId,
            FeaturedDNASequence featuredDNASequence) {
        boolean result = false;

        Account account = getAccountByToken(authToken);

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

            Sequence sequence = sequenceController
                    .featuredDNASequenceToSequence(featuredDNASequence);

            sequence.setEntry(entry);
            sequenceController.update(sequence);

            logInfo(account.getEmail() + " saveFeaturedDNASequence: " + entryId);

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

    @Override
    protected String getServiceName() {
        return VECTOR_EDITOR_SERVICE_NAME;
    }
}
