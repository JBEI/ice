package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.ShotgunSequenceDAO;
import org.jbei.ice.storage.model.Entry;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Hector Plahar
 */
public class TraceSequences {

    protected final String userId;

    public TraceSequences(String userId) {
        this.userId = userId;
    }

    /**
     * Bulk update from zip file. Associates data contained in the zip file with multiple
     * entries. This method requires administrator privileges
     *
     * @param inputStream input stream of zip file
     */
    public List<String> bulkUpdate(InputStream inputStream) {
        new EntryAuthorization().expectAdmin(userId);

        List<String> errors = new ArrayList<>();
        try (ZipInputStream stream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            String fileName = null;

            while ((zipEntry = stream.getNextEntry()) != null) {
                if (zipEntry.getName().startsWith("__MACOSX"))
                    continue;

                if (zipEntry.isDirectory()) {
                    fileName = zipEntry.getName();
                    continue;
                }

                if (fileName == null)
                    continue;

                // filename should be a part number
                String[] split = fileName.split(File.separator);
                String partNumber = split[split.length - 1];
                Entry entry = DAOFactory.getEntryDAO().getByPartNumber(partNumber);
                if (entry == null) {
                    errors.add("Part number \"" + partNumber + "\" generated from \"" + fileName + "\" not a valid entry");
                    break;
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int c;
                while ((c = stream.read()) != -1) {
                    byteArrayOutputStream.write(c);
                }

                String zipFilename = Paths.get(zipEntry.getName()).getFileName().toString();
                add(entry, zipFilename, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
            }
        } catch (IOException e) {
            Logger.error(e);
        }

        return errors;
    }

    protected boolean add(Entry entry, String fileName, InputStream fileInputStream) {
        ShotgunSequenceDAO dao = DAOFactory.getShotgunSequenceDAO();
        String storageName = Utils.generateUUID();
        try {
            dao.writeSequenceFileToDisk(storageName, fileInputStream);
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
        return dao.create(fileName, userId, entry, storageName, new Date()) != null;
    }
}
