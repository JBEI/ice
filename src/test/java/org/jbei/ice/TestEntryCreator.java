package org.jbei.ice;

import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.entry.Entries;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.AccountModel;
import org.jbei.ice.storage.model.Entry;

/**
 * Helper class for creating entries to be used in test.
 * Needs to be wrapped in a transaction
 *
 * @author Hector Plahar
 */
public class TestEntryCreator {

    public static Entry createTestStrain(AccountModel account) {
        PartData partData = new PartData(EntryType.STRAIN);
        partData.setName("sTrain");
        partData = new Entries(account.getEmail()).create(partData);
        return DAOFactory.getEntryDAO().get(partData.getId());
    }

    public static Entry createTestPlasmid(AccountModel account) {
        PartData plasmid = new PartData(EntryType.PLASMID);
        plasmid.setName("pLasmid");
        plasmid = new Entries(account.getEmail()).create(plasmid);
        return DAOFactory.getEntryDAO().get(plasmid.getId());
    }

    public static long createTestPart(String userId) {
        PartData data = new PartData(EntryType.PART);
        data.setName("pTest " + userId);
        return new Entries(userId).create(data).getId();
    }
}
