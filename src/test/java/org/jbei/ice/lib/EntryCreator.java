package org.jbei.ice.lib;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Strain;

/**
 * Helper class for creating entries to be used in test.
 * Needs to be wrapped in a transaction
 *
 * @author Hector Plahar
 */
public class EntryCreator {

    public static Strain createTestStrain(Account account) throws Exception {
        EntryController controller = new EntryController();
        Strain strain = new Strain();
        strain.setName("sTrain");
        strain = (Strain) controller.createEntry(account, strain, null);
        return strain;
    }
}
