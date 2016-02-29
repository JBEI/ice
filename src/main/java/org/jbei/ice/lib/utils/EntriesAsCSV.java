package org.jbei.ice.lib.utils;

import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.jbei.ice.lib.bulkupload.BulkCSVUploadHeaders;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.sequence.ByteArrayWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Representation of a list of entries as a CSV file
 *
 * @author Hector Plahar
 */
public class EntriesAsCSV {

    private Set<EntryType> entryTypes;
    private Path csvPath;
    private List<Long> entries;
    private boolean includeSequences;
    private String fileName;
    private String[] formats;

    public EntriesAsCSV() {
        entryTypes = new HashSet<>();
    }

    public EntriesAsCSV(boolean includeSequences, String fileName, String... formats) {
        this.includeSequences = includeSequences;
        if (fileName == null)
            this.fileName = UUID.randomUUID().toString() + ".zip";
        else
            this.fileName = fileName;
        this.formats = formats;
        entryTypes = new HashSet<>();
    }

    public final boolean setSelectedEntries(String userId, EntrySelection selection) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        Entries retriever = new Entries();
        this.entries = retriever.getEntriesFromSelectionContext(account.getEmail(), selection);
        if (includeSequences)
            return writeZip(userId);
        return writeList();
    }

    public boolean setEntries(String userId, List<Long> entries) {
        this.entries = entries;
        if (includeSequences)
            return writeZip(userId);
        return writeList();
    }

    private boolean writeZip(String userId) {
        SequenceController sequenceController = new SequenceController();
        File tmpDir = new File(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        csvPath = Paths.get(tmpDir.getAbsolutePath(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(csvPath.toFile());
            ZipOutputStream zos = new ZipOutputStream(fos);

            // get sbol and genbank formats
            for (long entryId : this.entries) {
                if (!DAOFactory.getSequenceDAO().hasSequence(entryId))
                    continue;

                for (String format : formats) {
                    ByteArrayWrapper wrapper = sequenceController.getSequenceFile(userId, entryId, format);
                    putZipEntry(wrapper, zos);
                }
            }

            FileInputStream fis = new FileInputStream(csvPath.toFile());
            ByteArrayWrapper wrapper = new ByteArrayWrapper(IOUtils.toByteArray(fis), "entries.csv");
            putZipEntry(wrapper, zos);
            zos.close();
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
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

    private boolean writeList() {
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
