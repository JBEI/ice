package org.jbei.ice.lib.manuscript;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.entry.EntriesAsCSV;
import org.jbei.ice.lib.folder.FolderPermissions;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.hibernate.dao.ManuscriptModelDAO;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.ManuscriptModel;

import java.util.Date;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class Manuscripts {

    private final String userId;
    private final ManuscriptModelDAO dao;
    private final FolderDAO folderDAO;

    public Manuscripts(String userId) {
        this.userId = userId;
        this.dao = DAOFactory.getManuscriptModelDAO();
        this.folderDAO = DAOFactory.getFolderDAO();
        if (!new AccountController().isAdministrator(userId))
            throw new PermissionException("Admin feature");
    }

    public boolean delete(long id) {
        ManuscriptModel manuscriptModel = this.dao.get(id);
        if (manuscriptModel == null)
            return false;

        this.dao.delete(manuscriptModel);
        return true;
    }

    public Manuscript add(Manuscript manuscript) {
        // todo : validation
        ManuscriptModel model = new ManuscriptModel();
        model.setCreationTime(new Date());
        model.setStatus(manuscript.getStatus());
        model.setParagonUrl(manuscript.getParagonUrl());
        model.setTitle(manuscript.getTitle());
        model.setAuthorFirstName(manuscript.getAuthorFirstName());
        model.setAuthorLastName(manuscript.getAuthorLastName());
        FolderDetails details = manuscript.getFolder();
        Folder folder = DAOFactory.getFolderDAO().get(details.getId());
        model.setFolder(folder);
        return dao.create(model).toDataTransferObject();
    }

    public Results<Manuscript> get(String sort, boolean asc, int offset, int size, String filter) {
        Results<Manuscript> results = new Results<>();
        results.setResultCount(dao.getTotalCount(filter));
        List<ManuscriptModel> list = dao.list(sort, asc, offset, size, filter);
        if (!list.isEmpty()) {
            for (ManuscriptModel manuscriptModel : list)
                results.getData().add(manuscriptModel.toDataTransferObject());
        }
        return results;
    }

    public Manuscript update(long id, Manuscript manuscript) {
        ManuscriptModel model = dao.get(id);
        if (model == null)
            return null;

        if (!StringUtils.isEmpty(manuscript.getTitle()))
            model.setTitle(manuscript.getTitle());

        if (!StringUtils.isEmpty(manuscript.getAuthorFirstName()))
            model.setAuthorFirstName(manuscript.getAuthorFirstName());

        if (!StringUtils.isEmpty(manuscript.getAuthorLastName()))
            model.setAuthorLastName(manuscript.getAuthorLastName());

        if (!StringUtils.isEmpty(manuscript.getParagonUrl()))
            model.setParagonUrl(manuscript.getParagonUrl());

        if (manuscript.getStatus() != null && manuscript.getStatus() != model.getStatus()) {
            // update status
            model.setStatus(manuscript.getStatus());
            FolderPermissions folderPermissions = new FolderPermissions(this.userId, model.getFolder().getId());

            if (model.getStatus() == ManuscriptStatus.ACCEPTED) {
                // make public
                setFolderType(model.getFolder(), FolderType.PUBLIC);
                folderPermissions.enablePublicReadAccess();
            } else {
                // remove public
                setFolderType(model.getFolder(), FolderType.PRIVATE);
                folderPermissions.disablePublicReadAccess();
            }
        }

        return dao.update(model).toDataTransferObject();
    }

    public Manuscript generateZip(long id) {
        ManuscriptModel model = dao.get(id);
        if (model == null)
            return null;

        // get folder
        List<Long> entryIds = this.folderDAO.getFolderContentIds(model.getFolder().getId(), null, true);
        EntriesAsCSV entriesAsCSV = new EntriesAsCSV("GENBANK", "SBOL2");
        entriesAsCSV.setEntries(this.userId, entryIds);
        Manuscript manuscript = model.toDataTransferObject();
        manuscript.setZipFileName(entriesAsCSV.getFilePath().getFileName().toString());
        return manuscript;
    }

    // makes folder featured
    protected void setFolderType(Folder folder, FolderType type) {
        if (folder == null)
            return;
        folder.setType(type);
        folder.setModificationTime(new Date());
        DAOFactory.getFolderDAO().update(folder);
    }
}
