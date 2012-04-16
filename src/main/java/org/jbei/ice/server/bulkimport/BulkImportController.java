package org.jbei.ice.server.bulkimport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jbei.ice.controllers.common.Controller;
import org.jbei.ice.controllers.permissionVerifiers.EntryPermissionVerifier;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.BulkImport;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.BulkImportEntryData;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.server.InfoToModelFactory;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AttachmentInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

public class BulkImportController extends Controller {

    public BulkImportController(Account account) {
        super(account, new EntryPermissionVerifier());
    }

    public BulkImport createBulkImport(Account account, ArrayList<EntryInfo> primary,
            ArrayList<EntryInfo> secondary, String email) {
        String tmpDir = JbeirSettings.getSetting("TEMPORARY_DIRECTORY");

        // submit bulk import for verification
        ArrayList<BulkImportEntryData> primaryDataList = new ArrayList<BulkImportEntryData>(
                primary.size());

        EntryAddType type = null;
        HashMap<String, File> attachmentFiles = new HashMap<String, File>();
        HashMap<String, File> sequenceFiles = new HashMap<String, File>();
        BulkImport bulkImport = new BulkImport();

        for (EntryInfo info : primary) {
            BulkImportEntryData data = new BulkImportEntryData();

            Entry entry = InfoToModelFactory.infoToEntry(info, null);
            entry.setOwnerEmail(account.getEmail());
            entry.setOwner(account.getFullName());
            data.setEntry(entry);

            // deal with files
            if (info.getAttachments() != null && !info.getAttachments().isEmpty()) {
                // deal with attachment files
                AttachmentInfo attachmentInfo = info.getAttachments().get(0);
                File file = new File(tmpDir + File.separator + attachmentInfo.getFileId());
                if (file.exists())
                    attachmentFiles.put(attachmentInfo.getFilename(), file);
            }

            if (info.getSequenceAnalysis() != null && !info.getSequenceAnalysis().isEmpty()) {
                // deal with sequence files
                SequenceAnalysisInfo sequenceInfo = info.getSequenceAnalysis().get(0);
                File file = new File(tmpDir + File.separator + sequenceInfo.getFileId());
                if (file.exists())
                    sequenceFiles.put(sequenceInfo.getName(), file);
            }

            // type 
            type = EntryAddType.valueOf(info.getType().name());
            primaryDataList.add(data);
        }

        // save primary data
        bulkImport.setPrimaryData(primaryDataList);

        // secondary data
        ArrayList<BulkImportEntryData> secondaryDataList = new ArrayList<BulkImportEntryData>(
                secondary.size());

        if (secondary != null && !secondary.isEmpty()) {
            for (EntryInfo info : secondary) {
                BulkImportEntryData data = new BulkImportEntryData();

                Entry entry = InfoToModelFactory.infoToEntry(info, null);
                entry.setOwnerEmail(account.getEmail());
                entry.setOwner(account.getFullName());
                data.setEntry(entry);

                // deal with files
                if (!info.getAttachments().isEmpty()) {
                    // deal with attachment files
                    AttachmentInfo attachmentInfo = info.getAttachments().get(0);
                    File file = new File(tmpDir + File.separator + attachmentInfo.getFileId());
                    if (file.exists())
                        attachmentFiles.put(attachmentInfo.getFilename(), file);
                }

                if (!info.getSequenceAnalysis().isEmpty()) {
                    // deal with sequence files
                    SequenceAnalysisInfo sequenceInfo = info.getSequenceAnalysis().get(0);
                    File file = new File(tmpDir + File.separator + sequenceInfo.getFileId());
                    if (file.exists())
                        sequenceFiles.put(sequenceInfo.getName(), file);
                }

                secondaryDataList.add(data);
            }
            bulkImport.setSecondaryData(secondaryDataList);
        }

        // set primary data and attachments and sequence files if any
        if (!attachmentFiles.isEmpty()) {
            try {
                byte[] bytes = createZip(attachmentFiles);
                bulkImport.setAttachmentFile(ArrayUtils.toObject(bytes));
            } catch (IOException ioe) {
                Logger.error(ioe);
            }
        }

        if (!sequenceFiles.isEmpty()) {
            try {
                byte[] bytes = createZip(sequenceFiles);
                bulkImport.setSequenceFile(ArrayUtils.toObject(bytes));
            } catch (IOException ioe) {
                Logger.error(ioe);
            }
        }

        bulkImport.setType(type.toString());
        Account emailAccount;
        try {
            emailAccount = AccountManager.getByEmail(email);
            bulkImport.setAccount(emailAccount);
        } catch (ManagerException e) {
            Logger.error(e);
            return null;
        }

        return bulkImport;
    }

    private static byte[] createZip(HashMap<String, File> files) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zipfile = new ZipOutputStream(bos);
        String fileName = null;
        ZipEntry zipentry = null;
        Iterator<String> iter = files.keySet().iterator();
        while (iter.hasNext()) {
            fileName = iter.next();
            zipentry = new ZipEntry(fileName);
            zipfile.putNextEntry(zipentry);
            File file = files.get(fileName);
            FileInputStream input = new FileInputStream(file);
            byte[] bytes = IOUtils.toByteArray(input);
            zipfile.write(bytes);
        }
        zipfile.close();
        return bos.toByteArray();
    }
}
