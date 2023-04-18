package org.jbei.ice.account;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.access.Authorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.AccountModel;

public class AccountAuthorization extends Authorization<AccountModel> {

    public AccountAuthorization() {
        super(DAOFactory.getAccountDAO());
    }

    public boolean isAdministrator(String userId) {
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        return super.isAdmin(userId);

//        final AccountModel account = this.dao.getByEmail(userId);
//        return account != null && account.getType() == AccountType.ADMIN;
    }
}
