package org.jbei.ice.lib.bulkupload;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.AccountInfo;
import org.jbei.ice.shared.dto.BulkUploadInfo;
import org.jbei.ice.shared.dto.ConfigurationKey;

/**
 * Utility class for Bulk Import
 *
 * @author Hector Plahar
 */
public class BulkUploadUtil {

    public static BulkUploadInfo modelToInfo(BulkUpload model, int size) {
        BulkUploadInfo info = new BulkUploadInfo();
        Account draftAccount = model.getAccount();
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setEmail(draftAccount.getEmail());
        accountInfo.setFirstName(draftAccount.getFirstName());
        accountInfo.setLastName(draftAccount.getLastName());
        info.setAccount(accountInfo);

        info.setId(model.getId());
        info.setLastUpdate(model.getLastUpdateTime());
        info.setCount(size);
        info.setType(EntryAddType.stringToType(model.getImportType()));
        info.setCreated(model.getCreationTime());
        info.setName(model.getName());
        return info;
    }

    public static Entry getPartNumberForStrainPlasmid(Account account, EntryController controller, String text) {
        String wikiLinkPrefix = Utils.getConfigValue(ConfigurationKey.WIKILINK_PREFIX);
        Pattern basicWikiLinkPattern = Pattern.compile("\\[\\[" + wikiLinkPrefix + ":.*?\\]\\]");
        Pattern partNumberPattern = Pattern.compile("\\[\\[" + wikiLinkPrefix + ":(.*)\\]\\]");
        Pattern descriptivePattern = Pattern.compile("\\[\\[" + wikiLinkPrefix + ":(.*)\\|(.*)\\]\\]");

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
}
