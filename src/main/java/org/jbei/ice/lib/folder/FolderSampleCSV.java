package org.jbei.ice.lib.folder;

import com.opencsv.CSVWriter;
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
 * Only 96 Well Plates are currently supported
 *
 * @author Hector Plahar
 */
public class FolderSampleCSV {

    private final FolderDAO dao;
    private final FolderAuthorization authorization;
    private Folder folder;

    public FolderSampleCSV(String userId, long folderId) {
        dao = DAOFactory.getFolderDAO();
        authorization = new FolderAuthorization();

        folder = dao.get(folderId);
        if (folder == null)
            throw new IllegalArgumentException("Could not retrieve folder by id " + folderId);

        authorization.expectRead(userId, folder);
    }

    public ByteArrayOutputStream write() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        List<Long> contents = dao.getFolderContentIds(folder.getId(), null, true);
        StringWriter stringWriter = new StringWriter();

        try (CSVWriter writer = new CSVWriter(stringWriter)) {
            writePlate(contents.iterator(), writer);
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

    // returns next part number if available or an empty string
    private String getValue(Iterator<Long> iterator) {
        if (iterator.hasNext()) {
            long partId = iterator.next();
            iterator.remove();
            Entry entry = DAOFactory.getEntryDAO().get(partId);
            return entry.getPartNumber();
        }

        return "";
    }

    public static void main(String[] args) {
        System.out.println((char) (((int) 'A' + 1)));
    }
}
