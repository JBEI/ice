package org.jbei.ice.lib;

import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Plasmid;
import org.jbei.ice.storage.model.Strain;
import org.junit.Assert;

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

    public static Plasmid createTestPlasmid(Account account) throws Exception {
        Plasmid plasmid = new Plasmid();
        plasmid.setBackbone("plasmid backone");
        plasmid.setOriginOfReplication("None");
        plasmid.setBioSafetyLevel(1);
        plasmid.setShortDescription("plasmid description");
        plasmid.setName("pLasmid");
        plasmid = (Plasmid) new EntryCreator().createEntry(account, plasmid, null);
        return plasmid;
    }

    public static Strain createTestAccountAndStrain(String userId) throws Exception {
        Account account = AccountCreator.createTestAccount(userId, false);
        Assert.assertNotNull(account);
        Strain strain = new Strain();
        strain.setName("sTrain");
        strain.setBioSafetyLevel(BioSafetyOption.LEVEL_ONE.ordinal());
        strain.setShortDescription("test strain");
        strain = (Strain) new EntryCreator().createEntry(account, strain, null);
        return strain;
    }

    public static long createTestPart(String userId) throws Exception {
        PartData data = new PartData(EntryType.PART);
        data.setShortDescription("summary for test");
        data.setName("pTest " + userId);
        data.setBioSafetyLevel(1);
        return new EntryCreator().createPart(userId, data).getId();
    }
}
