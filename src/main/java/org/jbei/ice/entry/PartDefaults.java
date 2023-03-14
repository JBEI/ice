package org.jbei.ice.entry;

import org.jbei.ice.account.PreferencesController;
import org.jbei.ice.dto.entry.EntryFieldLabel;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.dto.user.PreferenceKey;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.AccountModel;

/**
 * Defaults for a specified biological part type
 *
 * @author Hector Plahar
 */
public class PartDefaults {

    private final String userId;
    private final AccountDAO accountDAO;
    private final PreferencesController preferencesController;
    private final AccountModel account;

    public PartDefaults(String userId) {
        this.userId = userId;
        this.accountDAO = DAOFactory.getAccountDAO();
        this.preferencesController = new PreferencesController();
        this.account = this.accountDAO.getByEmail(userId);
    }

    /**
     * Retrieves and sets the default values for the entry. Some of these values (e.g. PI, and Funding Source)
     * are set by individual users as part of their personal preferences. Others are inherent. e,g, the owner
     * and creator fields
     *
     * @param type entry type
     * @return PartData object with the retrieve part defaults
     */
    public PartData get(EntryType type) {
        PartData partData = new PartData(type);

        // owner and creator details
        if (account != null) {
            partData.setOwner(account.getFullName());
            partData.setOwnerEmail(account.getEmail());
        }

        // set the entry type defaults
        return partData;
    }

    public String getForLabel(EntryFieldLabel label) {
        if (label == EntryFieldLabel.STATUS)
            return "Complete";

        if (label == EntryFieldLabel.CREATOR) {
            return account.getFullName();
        }

        if (label == EntryFieldLabel.CREATOR_EMAIL) {
            return account.getEmail();
        }

        if (label == EntryFieldLabel.PI) {
            return preferencesController.getPreferenceValue(userId, PreferenceKey.PRINCIPAL_INVESTIGATOR.name());
        }

        if (label == EntryFieldLabel.FUNDING_SOURCE) {
            return preferencesController.getPreferenceValue(userId, PreferenceKey.FUNDING_SOURCE.name());
        }

        if (label == EntryFieldLabel.BIO_SAFETY_LEVEL) {
            return "1";
        }

        return "";
    }
}
