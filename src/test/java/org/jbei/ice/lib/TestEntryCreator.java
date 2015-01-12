package org.jbei.ice.lib;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.shared.BioSafetyOption;

/**
 * Helper class for creating entries to be used in test.
 * Needs to be wrapped in a transaction
 *
 * @author Hector Plahar
 */
public class TestEntryCreator {

    public static Strain createTestStrain(Account account) throws Exception {
        Strain strain = new Strain();
        strain.setName("sTrain");
        strain = (Strain) new EntryCreator().createEntry(account, strain, null);
        return strain;
    }

    public static Strain createTestAccountAndStrain(String userId) throws Exception {
        Account account = AccountCreator.createTestAccount(userId, false);
        Strain strain = new Strain();
        strain.setName("sTrain");
        strain.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
        strain.setShortDescription("test strain");
        strain = (Strain) new EntryCreator().createEntry(account, strain, null);
        return strain;
    }
}
