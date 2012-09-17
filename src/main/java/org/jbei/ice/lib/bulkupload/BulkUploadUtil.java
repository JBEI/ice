package org.jbei.ice.lib.bulkupload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.attachment.Attachment;
import org.jbei.ice.lib.entry.attachment.AttachmentController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.server.ModelToInfoFactory;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.EntryInfo;
import org.jbei.ice.shared.dto.GroupInfo;

/**
 * Utility class for Bulk Import
 *
 * @author Hector Plahar
 */
public class BulkUploadUtil {
    private final static String TMP_DIR = JbeirSettings.getSetting("TEMPORARY_DIRECTORY");

    public static BulkUploadInfo modelToInfo(BulkUpload model) {

        BulkUploadInfo info = new BulkUploadInfo();
        Account draftAccount = model.getAccount();
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());
        info.setAccount(accountInfo);

        info.setId(model.getId());
        info.setLastUpdate(model.getLastUpdateTime());
        info.setCount(model.getContents().size());
        info.setType(EntryAddType.stringToType(model.getImportType()));
        info.setCreated(model.getCreationTime());
        info.setName(model.getName());
        Group group = model.getReadGroup();

        if (group != null) {
            GroupInfo groupInfo = new GroupInfo();
            groupInfo.setUuid(group.getUuid());
            groupInfo.setLabel(group.getLabel());
            groupInfo.setId(group.getId());
            info.setGroupInfo(groupInfo);
        }
        return info;
    }

    public static EntryInfo toEntryInfo(AttachmentController attachmentController,
            SequenceController sequenceController, Account account, Entry entry, Entry enclosed) {

        ArrayList<Attachment> attachments = null;
        try {
            attachments = attachmentController.getByEntry(account, entry);
        } catch (ControllerException e) {
            Logger.error(e);
        }

        boolean hasSequence = false;
        try {
            hasSequence = sequenceController.getByEntry(entry) != null;
        } catch (ControllerException e) {
            Logger.error(e);
        }

        // convert to info object (no samples or trace sequences since bulk import does not have the ui for
        // it yet)
        EntryInfo entryInfo = ModelToInfoFactory.getInfo(account, entry, attachments, null, null, hasSequence);
        if (entryInfo != null && enclosed != null) {
            attachments = null;
            try {
                attachments = attachmentController.getByEntry(account, entry);
            } catch (ControllerException e) {
                Logger.error(e);
            }

            hasSequence = false;
            try {
                hasSequence = sequenceController.getByEntry(entry) != null;
            } catch (ControllerException e) {
                Logger.error(e);
            }

            // convert to info object (no samples or trace sequences since bulk import does not have the ui for
            // it yet)
            EntryInfo enclosedInfo = ModelToInfoFactory.getInfo(account, enclosed, attachments, null, null,
                                                                hasSequence);
            entryInfo.setInfo(enclosedInfo);
        }
        return entryInfo;
    }

    public static Entry getPartNumberForStrainPlasmid(Account account, EntryController controller, String text) {

        Pattern basicWikiLinkPattern = Pattern.compile("\\[\\[" + JbeirSettings.getSetting(
                "WIKILINK_PREFIX") + ":.*?\\]\\]");
        Pattern partNumberPattern = Pattern.compile("\\[\\[" + JbeirSettings.getSetting(
                "WIKILINK_PREFIX") + ":(.*)\\]\\]");
        Pattern descriptivePattern = Pattern.compile("\\[\\[" + JbeirSettings.getSetting(
                "WIKILINK_PREFIX") + ":(.*)\\|(.*)\\]\\]");

        if (text == null) {
            return null;
        }

        Matcher basicWikiLinkMatcher = basicWikiLinkPattern.matcher(text);

        while (basicWikiLinkMatcher.find()) {
            String partNumber = null;

            Matcher partNumberMatcher = partNumberPattern.matcher(basicWikiLinkMatcher.group());
            Matcher descriptivePatternMatcher = descriptivePattern.matcher(basicWikiLinkMatcher.group());

            if (descriptivePatternMatcher.find()) {
                partNumber = descriptivePatternMatcher.group(1).trim();
            } else if (partNumberMatcher.find()) {
                partNumber = partNumberMatcher.group(1).trim();
            }

            if (partNumber != null) {
                try {
                    return controller.getByPartNumber(account, partNumber);
                } catch (ControllerException e) {
                    Logger.error(e);
                } catch (PermissionException e) {
                    Logger.error(e);
                }
            }
        }
        return null;
    }

    public static InputStream getFileInputStream(String fileName) {

        File file = new File(TMP_DIR + File.separator + fileName);
        if (!file.exists())
            return null;

        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Logger.error(e);
            return null;
        }
    }
}
