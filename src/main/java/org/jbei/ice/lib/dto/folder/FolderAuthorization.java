package org.jbei.ice.lib.dto.folder;

import org.jbei.ice.lib.access.Authorization;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.folder.Folder;

/**
 * @author Hector Plahar
 */
public class FolderAuthorization extends Authorization<Folder> {

    public FolderAuthorization() {
        super(DAOFactory.getFolderDAO());
    }

    @Override
    public String getOwner(Folder folder) {
        return folder.getOwnerEmail();
    }
}
