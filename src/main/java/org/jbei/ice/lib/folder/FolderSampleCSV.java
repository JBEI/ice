package org.jbei.ice.lib.folder;

import com.opencsv.CSVWriter;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FolderDAO;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Folder contents represented in CSV for samples
 * Only 96 Well Plates are currently supported and this will also only export <code>Strains</code> only
 *
 * @author Hector Plahar
 */
public class FolderSampleCSV {

    private final FolderDAO dao;
    private Folder folder;

    public FolderSampleCSV(String userId, long folderId) {
        dao = DAOFactory.getFolderDAO();

        folder = dao.get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("Could not retrieve folder by id " + folderId);

        new FolderAuthorization().expectRead(userId, folder);
    }

    public ByteArrayOutputStream write() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        List<Long> contents = dao.getFolderContentIds(folder.getId(), null, true);
        StringWriter stringWriter = new StringWriter();

        try (CSVWriter writer = new CSVWriter(stringWriter)) {
            while (!contents.isEmpty()) {
                writePlate(contents.iterator(), writer);
                writer.writeNext(new String[]{""});
            }
            stream.write(stringWriter.toString().getBytes());
        }
        return stream;
    }

    private void writePlate(Iterator<Long> iterator, CSVWriter writer) {
        // write row headers
        String[] rowHeader = new String[]{"", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        writer.writeNext(rowHeader);
        int i = 0;
        List<String> row = new ArrayList<>();
        char plateXter = 'A';

        while (i < 96) {
            i += 1;
            if (row.isEmpty())
                row.add(String.valueOf(plateXter));

            row.add(getValue(iterator));

            if (i % 12 == 0) {
                plateXter += 1;
                writer.writeNext(row.toArray(new String[0]));
                row.clear();
            }
        }
    }

    /**
     * Checks if there are still entries available as indicated by the iterator.
     * If so it iterates through the list (removing entries as it encounters them)
     * until an entry of type <code>STRAIN</code> is available
     * If no entries are available, it returns an empty string
     *
     * @param iterator Iterator to list of part ids
     * @return part number of first strain encountered, otherwise an empty string
     */
    private String getValue(Iterator<Long> iterator) {
        while (iterator.hasNext()) {
            long partId = iterator.next();
            iterator.remove();
            Entry entry = DAOFactory.getEntryDAO().get(partId);
            if (entry.getRecordType().equalsIgnoreCase(EntryType.STRAIN.getDisplay()))
                return entry.getPartNumber();
        }

        return "";
    }
}
