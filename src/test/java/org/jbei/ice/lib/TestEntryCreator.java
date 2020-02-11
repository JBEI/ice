package org.jbei.ice.lib;

import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PlasmidData;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Plasmid;
import org.jbei.ice.storage.model.Strain;

/**
 * Helper class for creating entries to be used in test.
 * Needs to be wrapped in a transaction
 *
 * @author Hector Plahar
 */
public class TestEntryCreator {

    public static Strain createTestStrain(Account account) {
        PartData partData = new PartData(EntryType.STRAIN);
        partData.setName("sTrain");
        partData = new Entries(account.getEmail()).create(partData);
        return (Strain) DAOFactory.getEntryDAO().get(partData.getId());
    }

    public static Plasmid createTestPlasmid(Account account) {
        PartData plasmid = new PartData(EntryType.PLASMID);
        PlasmidData plasmidData = new PlasmidData();
        plasmidData.setBackbone("plasmid backone");
        plasmidData.setOriginOfReplication("None");
        plasmid.setPlasmidData(plasmidData);
        plasmid.setBioSafetyLevel(1);
        plasmid.setShortDescription("plasmid description");
        plasmid.setName("pLasmid");
        plasmid = new Entries(account.getEmail()).create(plasmid);
        return (Plasmid) DAOFactory.getEntryDAO().get(plasmid.getId());
    }

    public static long createTestPart(String userId) {
        PartData data = new PartData(EntryType.PART);
        data.setShortDescription("summary for test");
        data.setName("pTest " + userId);
        data.setBioSafetyLevel(1);
        return new Entries(userId).create(data).getId();
    }
}
