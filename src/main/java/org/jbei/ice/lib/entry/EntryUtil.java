package org.jbei.ice.lib.entry;


import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.model.ArabidopsisSeed;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Part;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.utils.Utils;

/**
 * Utility class for operating on entries
 *
 * @author Hector Plahar
 */
public class EntryUtil {

    public static Entry createEntryFromType(EntryType type, String name, String email) {
        Entry entry;

        switch (type) {
            case PLASMID:
                entry = new Plasmid();
                break;
            case STRAIN:
                entry = new Strain();
                break;
            case PART:
                entry = new Part();
                break;
            case ARABIDOPSIS:
                entry = new ArabidopsisSeed();
                break;

            default:
                return null;
        }

        entry.setOwner(name);
        entry.setOwnerEmail(email);
        entry.setCreator(name);
        entry.setCreatorEmail(email);
        return entry;
    }

    public static String getNextPartNumber() {
        return DAOFactory.getEntryDAO().generateNextPartNumber(Utils.getConfigValue(
                ConfigurationKey.PART_NUMBER_PREFIX),
                                                               Utils.getConfigValue(
                                                                       ConfigurationKey.PART_NUMBER_DELIMITER),
                                                               Utils.getConfigValue(
                                                                       ConfigurationKey.PART_NUMBER_DIGITAL_SUFFIX));
    }
}
