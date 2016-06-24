package org.jbei.ice.lib.entry;

import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.sequence.ByteArrayWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.PermissionDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Group;
import org.jbei.ice.storage.model.Sequence;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Representation of a list of entries as a CSV file with option to include associated sequences
 *
 * @author Hector Plahar
 */
public class EntriesAsCSV {

    private Path csvPath;
    private List<Long> entries;
    private boolean includeSequences;
    private String[] formats;
    private EntryDAO dao;
    private SequenceDAO sequenceDAO;
    private AccountDAO accountDAO;
    private PermissionDAO permissionDAO;

    /**
     * @param formats optional list of formats of sequences to include
     */
    public EntriesAsCSV(String... formats) {
        this.includeSequences = formats.length > 0;
        this.formats = formats;
        this.dao = DAOFactory.getEntryDAO();
        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.permissionDAO = DAOFactory.getPermissionDAO();
    }

    /**
     * Set source of entries, extract to csv
     *
     * @param userId    identifier of user making request
     * @param selection selection indicating source of entries
     * @return true if extraction happened successfully and can be retrieved with a call to <code>getFilePath</code>
     * false otherwise
     */
    public boolean setSelectedEntries(String userId, EntrySelection selection) {
        Entries retriever = new Entries(userId);
        this.entries = retriever.getEntriesFromSelectionContext(selection);
        try {
            writeList(userId);
            return true;
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
    }

    /**
     * Directly set the list of entries whose fields and (optionally) sequences are to be extracted
     *
     * @param userId  identifier of user making request
     * @param entries list of entry ids
     * @return true if extraction happened successfully and can be retrieved with a call to <code>getFilePath</code>
     * false otherwise
     */
    public boolean setEntries(String userId, List<Long> entries) {
        this.entries = entries;
        try {
            writeList(userId);
            return true;
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
    }

    protected String[] getCSVHeaders(List<EntryField> fields) {

        // get headers
        String[] headers = new String[fields.size() + 3];
        headers[0] = "Created";
        headers[1] = "Part ID";

        int i = 1;
        for (EntryField field : fields) {
            i += 1;
            headers[i] = field.getLabel();
        }
        headers[i + 1] = "Sequence File";
        return headers;
    }

    /**
     * Iterate through list of entries and extract values
     *
     * @param userId identifier of user making request
     * @throws IOException on Exception write values to file
     */
    private void writeList(String userId) throws IOException {

        // filter entries based on what the user is allowed to see if the user is not an admin
        Account account = this.accountDAO.getByEmail(userId);
        Set<Group> accountGroups = new GroupController().getAllGroups(account);
        if (account.getType() != AccountType.ADMIN)
            entries = permissionDAO.getCanReadEntries(account, accountGroups, entries);

        if (entries == null) {
            Logger.warn("No entries to convert to csv format");
            return;
        }

        // write headers
        Path tmpPath = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        File tmpFile = File.createTempFile("ice-", ".csv", tmpPath.toFile());
        csvPath = tmpFile.toPath();
        FileWriter fileWriter = new FileWriter(tmpFile);

        List<EntryField> fields = getEntryFields();
        String[] headers = getCSVHeaders(fields);

        try (CSVWriter writer = new CSVWriter(fileWriter)) {

            writer.writeNext(headers);

            Set<Long> sequenceSet = new HashSet<>();

            // write entry fields
            for (long entryId : entries) {
                Entry entry = dao.get(entryId);

                //  get contents and write data out
                String[] line = new String[fields.size() + 3];
                line[0] = entry.getCreationTime().toString();
                line[1] = entry.getPartNumber();
                int i = 1;
                for (EntryField field : fields) {
                    line[i + 1] = EntryUtil.entryFieldToValue(entry, field);
                    i += 1;
                }

                if (this.includeSequences && sequenceDAO.hasSequence(entryId)) {
                    line[i + 1] = getSequenceName(entry);
                    sequenceSet.add(entryId);
                } else {
                    line[i + 1] = "";
                }

                writer.writeNext(line);
            }

            writer.close();
            writeZip(userId, sequenceSet);
        }
    }

    private String getSequenceName(Entry entry) {
        String format;
        if (formats == null || formats.length == 0) {
            format = "original";
        } else {
            format = formats[0].toLowerCase();
        }

        switch (format.toLowerCase()) {
            case "original":
                Sequence sequence = sequenceDAO.getByEntry(entry);
                if (sequence == null)
                    return "";
                return sequence.getFileName();

            case "genbank":
            default:
                return entry.getPartNumber() + ".gb";

            case "fasta":
                return entry.getPartNumber() + ".fasta";

            case "sbol1":
            case "sbol2":
                return entry.getPartNumber() + ".xml";
        }
    }

    private boolean writeZip(String userId, Set<Long> sequenceSet) {
        SequenceController sequenceController = new SequenceController();
        Path tmpPath = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        try {
            File tmpZip = File.createTempFile("zip-", ".zip", tmpPath.toFile());

            // out
            FileOutputStream fos = new FileOutputStream(tmpZip);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // get sequence formats
            for (long entryId : sequenceSet) {
                for (String format : formats) {
                    ByteArrayWrapper wrapper = sequenceController.getSequenceFile(userId, entryId, format);
                    putZipEntry(wrapper, zos);
                }
            }

            // write the csv file
            FileInputStream fis = new FileInputStream(csvPath.toFile());
            ByteArrayWrapper wrapper = new ByteArrayWrapper(IOUtils.toByteArray(fis), "entries.csv");
            putZipEntry(wrapper, zos);
            zos.close();
            csvPath = tmpZip.toPath();
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

    protected List<EntryField> getEntryFields() {
        Set<String> recordTypes = new HashSet<>(dao.getRecordTypes(entries));
        List<EntryField> fields = EntryFields.getCommonFields();

        for (String recordType : recordTypes) {
            EntryType type = EntryType.nameToType(recordType);
            if (type == null) {
                Logger.error("Could not convert entry type " + recordType);
                continue;
            }

            switch (type) {
                case ARABIDOPSIS:
                    EntryFields.addArabidopsisSeedHeaders(fields);
                    break;

                case STRAIN:
                    EntryFields.addStrainHeaders(fields);
                    break;

                case PLASMID:
                    EntryFields.addPlasmidHeaders(fields);
                    break;
            }
        }

        return fields;
    }

    public Path getFilePath() {
        return csvPath;
    }
}
