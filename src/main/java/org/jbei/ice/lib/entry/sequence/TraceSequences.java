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

        List<String> results = new ArrayList<>();
        try (ZipInputStream stream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;

            while ((zipEntry = stream.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                if (fileName.startsWith("__MACOSX"))
                    continue;

                // filename should be a part number
                String[] split = fileName.split(File.separator);
                if (split.length < 1)
                    continue;

                String partNumber = split[split.length - 1];
                if (partNumber.startsWith(".") || partNumber.startsWith("_"))   // todo or get the settings from
                    continue;

                Entry entry = DAOFactory.getEntryDAO().getByPartNumber(partNumber);
                if (entry == null) {
                    Logger.error("Part number \"" + partNumber + "\" generated from \"" + fileName + "\" not a valid entry");
                    continue;
                }

                results.add(entry.getPartNumber());
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

        return results;
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
