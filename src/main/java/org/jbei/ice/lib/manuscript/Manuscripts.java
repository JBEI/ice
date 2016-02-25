package org.jbei.ice.lib.manuscript;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.folder.FolderDetails;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.sequence.ByteArrayWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.folder.FolderController;
import org.jbei.ice.lib.utils.EntriesAsCSV;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.hibernate.dao.ManuscriptModelDAO;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.ManuscriptModel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

            FolderController folderController = new FolderController();
            if (model.getStatus() == ManuscriptStatus.ACCEPTED) {
                // make public
                setFolderType(model.getFolder(), FolderType.PUBLIC);
                folderController.enablePublicReadAccess(this.userId, model.getFolder().getId());
            } else {
                // remove public
                setFolderType(model.getFolder(), FolderType.PRIVATE);
                folderController.disablePublicReadAccess(this.userId, model.getFolder().getId());
            }
        }

        return dao.update(model).toDataTransferObject();
    }

    public Path generateZip(long id) {
        ManuscriptModel model = dao.get(id);
        if (model == null)
            return null;

        // get folder
        List<Long> entryIds = this.folderDAO.getFolderContentIds(model.getFolder().getId(), null, true);
        EntrySelection entrySelection = new EntrySelection();
        entrySelection.getEntries().addAll(entryIds);

        EntriesAsCSV entriesAsCSV = new EntriesAsCSV();
        entriesAsCSV.setSelectedEntries(this.userId, entrySelection);
        entryIds = entriesAsCSV.getEntryIds();
        SequenceController sequenceController = new SequenceController();

        File tmpDir = new File(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        String prefix = model.getAuthorFirstName() + "_" + model.getAuthorLastName();
        Path zipPath = Paths.get(tmpDir.getAbsolutePath(), prefix + "_collection.zip");

        try {
            FileOutputStream fos = new FileOutputStream(zipPath.toFile());
            ZipOutputStream zos = new ZipOutputStream(fos);

            // get sbol and genbank formats
            for (long entryId : entryIds) {

                // get genbank
                ByteArrayWrapper wrapper = sequenceController.getSequenceFile(this.userId, entryId, "genbank");
                putZipEntry(wrapper, zos);

                // get sbol
                wrapper = sequenceController.getSequenceFile(this.userId, entryId, "sbol");
                putZipEntry(wrapper, zos);
            }

            Path csvPath = entriesAsCSV.getFilePath();
            FileInputStream fis = new FileInputStream(csvPath.toFile());
            ByteArrayWrapper wrapper = new ByteArrayWrapper(IOUtils.toByteArray(fis), "entries.csv");
            putZipEntry(wrapper, zos);
            zos.close();
            return zipPath;
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }
    }

    protected void putZipEntry(ByteArrayWrapper wrapper, ZipOutputStream zos) {
        try {
            byte[] buffer = new byte[1024];

            zos.putNextEntry(new ZipEntry(wrapper.getName()));

            ByteArrayInputStream bis = new ByteArrayInputStream(wrapper.getBytes());
            int length;
            while ((length = bis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            bis.close();
            zos.closeEntry();
        } catch (Exception e) {
            Logger.error(e);
        }
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
