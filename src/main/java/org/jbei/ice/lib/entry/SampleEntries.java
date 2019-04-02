package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Entries contained in folders of type <code>SAMPLE</code>
 *
 * @author Hector Plahar
 */
public class SampleEntries {

    private final Account account;
    private final FolderDAO dao;

    public SampleEntries(String userId) {
        this.account = DAOFactory.getAccountDAO().getByEmail(userId);
        if (this.account == null)
            throw new IllegalArgumentException("Cannot retrieve account for \"" + userId + "\"");
        this.dao = DAOFactory.getFolderDAO();
    }

    public long getCount(String filter) {
        return this.dao.getEntryCountByFolderType(FolderType.SAMPLE, filter);
    }

    public List<PartData> get(ColumnField field, boolean asc, int start, int limit, String filter) {
        List<Entry> entries = this.dao.getEntrysByFolderType(FolderType.SAMPLE, field, asc, start, limit, filter);

        ArrayList<PartData> data = new ArrayList<>();
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(account.getEmail(), entry, false);
            data.add(info);
        }
        return data;
    }
}
