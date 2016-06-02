package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.account.PreferencesController;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.user.PreferenceKey;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.Account;

/**
 * Defaults for a specified biological part type
 *
 * @author Hector Plahar
 */
public class PartDefaults {

    private final String userId;
    private final AccountDAO accountDAO;

    public PartDefaults(String userId) {
        this.userId = userId;
        this.accountDAO = DAOFactory.getAccountDAO();
    }

    /**
     * Retrieves and sets the default values for the entry. Some of these values (e.g. PI, and Funding Source)
     * are set by individual users as part of their personal preferences
     *
     * @param type entry type
     * @return PartData object with the retrieve part defaults
     */
    public PartData get(EntryType type) {
        PartData partData = new PartData(type);
        PreferencesController preferencesController = new PreferencesController();

        // pi defaults
        String value = preferencesController.getPreferenceValue(userId, PreferenceKey.PRINCIPAL_INVESTIGATOR.name());
        if (value != null) {
            Account piAccount = this.accountDAO.getByEmail(value);
            if (piAccount == null) {
                partData.setPrincipalInvestigator(value);
            } else {
                partData.setPrincipalInvestigator(piAccount.getFullName());
                partData.setPrincipalInvestigatorEmail(piAccount.getEmail());
                partData.setPrincipalInvestigatorId(piAccount.getId());
            }
        }

        // funding source defaults
        value = preferencesController.getPreferenceValue(userId, PreferenceKey.FUNDING_SOURCE.name());
        if (value != null) {
            partData.setFundingSource(value);
        }

        // owner and creator details
        Account account = this.accountDAO.getByEmail(userId);
        if (account != null) {
            partData.setOwner(account.getFullName());
            partData.setOwnerEmail(account.getEmail());
            partData.setCreator(partData.getOwner());
            partData.setCreatorEmail(partData.getOwnerEmail());
        }

        // set the entry type defaults
        return EntryUtil.setPartDefaults(partData);
    }
}
