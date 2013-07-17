package org.jbei.ice.server.servlet;

import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.EntryAddType;
import org.jbei.ice.lib.shared.dto.ConfigurationKey;
import org.jbei.ice.lib.shared.dto.bulkupload.BulkUploadAutoUpdate;
import org.jbei.ice.lib.shared.dto.bulkupload.EntryField;
import org.jbei.ice.lib.shared.dto.entry.EntryType;
import org.jbei.ice.lib.utils.Utils;

import au.com.bytecode.opencsv.CSVReader;
import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import org.apache.commons.fileupload.FileItem;

/**
 * @author Hector Plahar
 */
public class CSVUploadServlet extends UploadAction {
    private static final long serialVersionUID = 1L;

    private Account isLoggedIn(Cookie[] cookies) throws ControllerException {
        final String COOKIE_NAME = Utils.getConfigValue(ConfigurationKey.COOKIE_NAME);

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String sid = cookie.getValue();
                if (sid == null || sid.isEmpty())
                    return null;

                if (!AccountController.isAuthenticated(sid))
                    return null;

                return ControllerFactory.getAccountController().getAccountBySessionKey(sid);
            }
        }
        return null;
    }

    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
        if (sessionFiles.isEmpty())
            return "No files";

        Account account;
        try {
            account = isLoggedIn(request.getCookies());
            if (account == null)
                return "Error: Could not validate user account";
        } catch (ControllerException ce) {
            Logger.error(ce);
        }

        String entryAddTypeString = request.getParameter("type");
        EntryAddType addType = EntryAddType.valueOf(entryAddTypeString);
        FileItem fileItem = sessionFiles.get(0);

        try {
            InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
            List<EntryField> fields = new LinkedList<>();
            List<BulkUploadAutoUpdate> updates = new LinkedList<>();
            try (CSVReader csvReader = new CSVReader(inputStreamReader)) {
                String[] lines;
                while ((lines = csvReader.readNext()) != null) {
                    if (fields.isEmpty()) {
                        for (int i = 0; i < lines.length; i += 1) {
                            String line = lines[i];
                            EntryField field = EntryField.fromString(line);
                            if (field == null)
                                return "Error: Unrecognized field " + line;

                            fields.add(i, field);
                        }
                    } else {
                        // process values
                        for (int i = 0; i < lines.length; i += 1) {
                            EntryField field = fields.get(i);
                            EntryType type = toEntryType(addType, field);
                            BulkUploadAutoUpdate autoUpdate = new BulkUploadAutoUpdate(type);
                            autoUpdate.getKeyValue().put(field, lines[i]);
                            updates.add(autoUpdate);
                        }
                    }
                }
            }
        } catch (Exception c) {
            Logger.error(c);
            return c.getMessage();
        }

        removeSessionFileItems(request);
        return "";
    }

    // TODO : combine with StrainWithPlasmidHeaders::isPlasmidHeader()
    private EntryType toEntryType(EntryAddType type, EntryField field) {
        EntryType entryType;
        if (type == EntryAddType.STRAIN_WITH_PLASMID) {
            boolean isPlasmid = isPlasmidHeader(field);

            if (isPlasmid) {
                // if updating plasmid portion of strain with one plasmid
                entryType = EntryType.PLASMID;
            } else {
                entryType = EntryType.STRAIN;
            }
        } else {
            entryType = EntryAddType.addTypeToType(type);
        }
        return entryType;
    }

    protected boolean isPlasmidHeader(EntryField entryField) {
        return (entryField == EntryField.PLASMID_NAME
                || entryField == EntryField.PLASMID_ALIAS
                || entryField == EntryField.PLASMID_LINKS
                || entryField == EntryField.PLASMID_SELECTION_MARKERS
                || entryField == EntryField.CIRCULAR
                || entryField == EntryField.PLASMID_BACKBONE
                || entryField == EntryField.PLASMID_PROMOTERS
                || entryField == EntryField.PLASMID_REPLICATES_IN
                || entryField == EntryField.PLASMID_ORIGIN_OF_REPLICATION
                || entryField == EntryField.PLASMID_KEYWORDS
                || entryField == EntryField.PLASMID_SUMMARY
                || entryField == EntryField.PLASMID_NOTES
                || entryField == EntryField.PLASMID_REFERENCES
                || entryField == EntryField.PLASMID_SEQ_FILENAME
                || entryField == EntryField.PLASMID_ATT_FILENAME);
    }
}
