package org.jbei.ice.lib.entry;

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.entry.sequence.InputStreamWrapper;
import org.jbei.ice.lib.entry.sequence.PartSequence;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
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

    private final String userId;
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
    public EntriesAsCSV(String userId, String... formats) {
        this.userId = userId;
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
     * @param selection selection indicating source of entries
     * @param fields    optional list of fields used to filter the data
     * @return true if extraction happened successfully and can be retrieved with a call to <code>getFilePath</code>
     * false otherwise
     */
    public boolean setSelectedEntries(EntrySelection selection, EntryField... fields) {
        Entries retriever = new Entries(userId);
        this.entries = retriever.getEntriesFromSelectionContext(selection);
        try {
            writeList(fields);
            return true;
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
    }

    /**
     * Directly set the list of entries whose fields and (optionally) sequences are to be extracted
     *
     * @param entries list of entry ids
     * @param fields  optional list of fields used to filter the data
     */
    public void setEntries(List<Long> entries, EntryField... fields) {
        this.entries = entries;
        try {
            writeList(fields);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    protected String[] getCSVHeaders(EntryField[] fields) {

        // get headers
        String[] headers = new String[fields.length + 4];
        headers[0] = "Created";
        headers[1] = "Part ID";

        int i = 1;
        for (EntryField field : fields) {
            i += 1;
            headers[i] = field.getLabel();
        }
        headers[i + 1] = "Sequence File";
        headers[i + 2] = "Parent IDs";
        return headers;
    }

    /**
     * Iterate through list of entries and extract values
     *
     * @throws IOException on Exception write values to file
     */
    private void writeList(EntryField... fields) throws IOException {

        // filter entries based on what the user is allowed to see if the user is not an admin
        Account account = this.accountDAO.getByEmail(userId);
        if (account.getType() != AccountType.ADMIN) {
            List<Group> accountGroups = new GroupController().getAllGroups(account);
            entries = permissionDAO.getCanReadEntries(account, accountGroups, entries);
        }

        if (entries == null) {
            Logger.warn("No entries to convert to csv format");
            return;
        }

        // write headers
        Path tmpPath = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        File tmpFile = File.createTempFile("ice-", ".csv", tmpPath.toFile());
        csvPath = tmpFile.toPath();
        FileWriter fileWriter = new FileWriter(tmpFile);

        if (fields == null || fields.length == 0)
            fields = getEntryFields();
        String[] headers = getCSVHeaders(fields);
        Set<Long> sequenceSet = new HashSet<>();

        try (CSVWriter writer = new CSVWriter(fileWriter)) {

            writer.writeNext(headers);

            // write entry fields
            for (long entryId : entries) {
                Entry entry = dao.get(entryId);

                //  get contents and write data out
                String[] line = new String[fields.length + 4];
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

                // get parents for entry
                EntryLinks links = new EntryLinks(this.userId, entry.getPartNumber());
                StringBuilder parents = new StringBuilder();
                for (PartData data : links.getParents()) {
                    parents.append(data.getPartId()).append(",");
                }
                if (!StringUtils.isEmpty(parents.toString()))
                    parents = new StringBuilder(parents.substring(0, parents.length() - 1));
                line[i + 2] = ("\"" + parents + "\"");
                writer.writeNext(line);
            }
        }

        if (!sequenceSet.isEmpty())
            writeZip(sequenceSet);
    }

    private String getSequenceName(Entry entry) {
        String format;
        if (formats == null || formats.length == 0) {
            format = "original";
        } else {
            format = formats[0].toLowerCase();
        }

        switch (SequenceFormat.fromString(format)) {
            case ORIGINAL:
                Sequence sequence = sequenceDAO.getByEntry(entry);
                if (sequence == null)
                    return "";
                return sequence.getFileName();

            case GENBANK:
            default:
                return entry.getPartNumber() + ".gb";

            case FASTA:
                return entry.getPartNumber() + ".fasta";

            case SBOL1:
            case SBOL2:
                return entry.getPartNumber() + ".xml";
        }
    }

    private void writeZip(Set<Long> sequenceSet) {
        Path tmpPath = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        try {
            File tmpZip = File.createTempFile("zip-", ".zip", tmpPath.toFile());

            // out
            FileOutputStream fos = new FileOutputStream(tmpZip);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // get sequence formats
            for (long entryId : sequenceSet) {
                for (String format : formats) {
                    InputStreamWrapper wrapper = new PartSequence(userId, Long.toString(entryId)).toFile(SequenceFormat.fromString(format));
                    putZipEntry(wrapper, zos);
                }
            }

            // write the csv file
            FileInputStream fis = new FileInputStream(csvPath.toFile());
            InputStreamWrapper wrapper = new InputStreamWrapper(fis, "entries.csv");
            putZipEntry(wrapper, zos);
            zos.close();
            csvPath = tmpZip.toPath();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void putZipEntry(InputStreamWrapper wrapper, ZipOutputStream zos) {
        try {
            byte[] buffer = new byte[1024];
            zos.putNextEntry(new ZipEntry(wrapper.getName()));
            InputStream bis = wrapper.getInputStream();
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

    private EntryField[] getEntryFields() {
        Set<String> recordTypes = new HashSet<>(dao.getRecordTypes(entries));
        List<EntryField> fields = EntryFields.getCommonFields();

        for (String recordType : recordTypes) {
            EntryType type = EntryType.nameToType(recordType);
            if (type == null) {
                Logger.error("Could not convert entry type " + recordType);
                continue;
            }

            switch (type) {
                case SEED:
                    EntryFields.addArabidopsisSeedHeaders(fields);
                    break;

                case STRAIN:
                    EntryFields.addStrainHeaders(fields);
                    break;

                case PLASMID:
                    EntryFields.addPlasmidHeaders(fields);
                    break;

                case PROTEIN:
                    EntryFields.addProteinHeaders(fields);
                    break;
            }
        }

        return fields.toArray(new EntryField[0]);
    }

    public ByteArrayOutputStream customize(EntrySelection selection, SequenceFormat format) throws IOException {
        Entries retriever = new Entries(this.userId);
        this.entries = retriever.getEntriesFromSelectionContext(selection);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        EntryAuthorization entryAuthorization = new EntryAuthorization();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (long entryId : this.entries) {
                // get the entry
                Entry entry = DAOFactory.getEntryDAO().get(entryId);
                if (entry == null) {
                    Logger.error("ERROR : no entry " + entryId);  // write to csv file
                    continue;
                }

                Sequence sequence = sequenceDAO.getByEntry(entry);
                if (sequence == null) {
                    continue;
                }

                // get the sequence
                InputStreamWrapper wrapper = new PartSequence(userId, Long.toString(entryId)).toFile(format);
                if (wrapper == null) {
                    Logger.error("ERROR : no sequence " + entryId);
                    continue;
                }

                wrapper.setName(entry.getPartNumber() + File.separatorChar + wrapper.getName());
                putZipEntry(wrapper, zos);
            }
            this.includeSequences = false;
            writeList(selection.getFields().toArray(new EntryField[0]));

            // write the csv file to zip file
            FileInputStream fis = new FileInputStream(csvPath.toFile());
            InputStreamWrapper wrapper = new InputStreamWrapper(fis, "entries.csv");
            putZipEntry(wrapper, zos);
        }
        return baos;
    }

    public Path getFilePath() {
        return csvPath;
    }
}
