package org.jbei.ice.lib.utils;

import au.com.bytecode.opencsv.CSVWriter;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.bulkupload.BulkCSVUploadHeaders;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dao.DAOFactory;
import org.jbei.ice.lib.dao.hibernate.EntryDAO;
import org.jbei.ice.lib.dao.hibernate.PermissionDAO;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.bulkupload.EntryField;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;
import org.jbei.ice.lib.group.GroupController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

        // ad-hoc entry selection takes precedence over other types of selection
        if (!selection.getEntries().isEmpty()) {
            PermissionDAO permissionDAO = DAOFactory.getPermissionDAO();
            GroupController controller = new GroupController();
            Set<Group> accountGroups = controller.getAllGroups(account);
            List<Long> entries = selection.getEntries();
            EntryAuthorization authorization = new EntryAuthorization();

            if (!authorization.isAdmin(userId))
                entries = permissionDAO.getCanReadEntries(account, accountGroups, entries);

            return writeList(entries);
        }

        // other selection types not supported yet
        return false;
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
                entryTypes.add(type);
                switch (type) {
                    case ARABIDOPSIS:
                        BulkCSVUploadHeaders.addArabidopsisSeedHeaders(fields);
                        break;

                    case STRAIN:
                        BulkCSVUploadHeaders.addStrainHeaders(fields);
                        break;

                    case PLASMID:
                        BulkCSVUploadHeaders.addPlasmidHeaders(fields);
                        break;
                }
            }

            final int fieldSize = fields.size();
            String[] line = new String[fieldSize + 1];
            line[0] = entry.getPartNumber();
            for (int i = 0; i < fieldSize; i += 1) {
                EntryField field = fields.get(i);
                line[i + 1] = EntryUtil.entryFieldToValue(entry, field);
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

        try (CSVWriter writer = new CSVWriter(fileWriter)) {
            // get headers
            String[] headers = new String[fields.size() + 1];
            headers[0] = "Part ID";
            for (int i = 0; i < fields.size(); i += 1) {
                headers[i + 1] = fields.get(i).getLabel();
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
