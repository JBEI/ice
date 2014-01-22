package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Strain;

/**
 * @author Hector Plahar
 */
public class EntryEditor {

    private final EntryDAO dao;

    public EntryEditor() {
        this.dao = new EntryDAO();
    }

    public void updateWithNextStrainName(String prefix, Entry entry) {
        dao.generateNextStrainNameForEntry(entry, prefix);
    }


    public void setStrainPlasmids(Account account, Strain strain, String plasmids) {
        strain.getLinkedEntries().clear();
        if (plasmids != null && !plasmids.isEmpty()) {
            for (String plasmid : plasmids.split(",")) {
                Entry linked = dao.getByPartNumber(plasmid.trim());
                if (linked == null)
                    continue;

//                if (!authorization.canRead(account.getEmail(), linked)) {
//                    continue;
//                }

                strain.getLinkedEntries().add(linked);
            }

            dao.update(strain);
        }
    }

}
