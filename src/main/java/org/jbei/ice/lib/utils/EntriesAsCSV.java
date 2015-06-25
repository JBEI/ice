package org.jbei.ice.lib.utils;

import com.opencsv.CSVWriter;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.bulkupload.BulkCSVUploadHeaders;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.EntryRetriever;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.model.Entry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Representation of a list of entries as a CSV file
 *
 * @author Hector Plahar
 */
public class EntriesAsCSV {

    private Set<EntryType> entryTypes;
    private Path csvPath;

    public EntriesAsCSV() {
        entryTypes = new HashSet<>();
    }

    public final boolean setSelectedEntries(String userId, EntrySelection selection) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        EntryRetriever retriever = new EntryRetriever();
        List<Long> entries = retriever.getEntriesFromSelectionContext(account.getEmail(), selection);
        return writeList(entries);
    }

    private boolean writeList(List<Long> entries) {
        EntryDAO dao = DAOFactory.getEntryDAO();
        List<EntryField> fields = BulkCSVUploadHeaders.getCommonFields();
        List<String[]> lines = new LinkedList<>();  // lines in the csv file

        for (long entryId : entries) {
            Entry entry = dao.get(entryId);

            // get contents
            EntryType type = EntryType.nameToType(entry.getRecordType());

            // get the headers for the final output
            if (type != null && !entryTypes.contains(type)) {
                List<EntryField> newFields = new ArrayList<>();
                entryTypes.add(type);
                switch (type) {
                    case ARABIDOPSIS:
                        BulkCSVUploadHeaders.addArabidopsisSeedHeaders(newFields);
                        break;

                    case STRAIN:
                        BulkCSVUploadHeaders.addStrainHeaders(newFields);
                        break;

                    case PLASMID:
                        BulkCSVUploadHeaders.addPlasmidHeaders(newFields);
                        break;
                }

                for (EntryField newField : newFields) {
                    if (!fields.contains(newField))
                        fields.add(newField);
                }
            }

            final int fieldSize = fields.size();
            String[] line = new String[fieldSize + 1];
            line[0] = entry.getPartNumber();
            int i = 0;
            for (EntryField field : fields) {
                line[i + 1] = EntryUtil.entryFieldToValue(entry, field);
                i += 1;
            }

            lines.add(line);
        }

        // write to csv
        File tmpDir = new File(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        FileWriter fileWriter;

        try {
            File tmpFile = File.createTempFile("ice-", ".csv", tmpDir);
            fileWriter = new FileWriter(tmpFile);
            csvPath = tmpFile.toPath();
        } catch (IOException io) {
            Logger.error(io);
            return false;
        }

        final int fieldSize = fields.size();

        try (CSVWriter writer = new CSVWriter(fileWriter)) {
            // get headers
            String[] headers = new String[fieldSize + 1];
            headers[0] = "Part ID";

            int i = 0;
            for (EntryField field : fields) {
                headers[i + 1] = field.getLabel();
                i += 1;
            }

            writer.writeNext(headers);

            // write contents
            for (String[] line : lines)
                writer.writeNext(line);

            return true;
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
    }

    public Path getFilePath() {
        return csvPath;
    }
}
