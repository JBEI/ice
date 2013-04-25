package org.jbei.ice.lib.bulkupload;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.BioSafetyOption;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.StatusType;
import org.jbei.ice.shared.dto.ConfigurationKey;
import org.jbei.ice.shared.dto.entry.EntryType;

/**
 * Utility class for Bulk Import
 *
 * @author Hector Plahar
 */
public class BulkUploadUtil {

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

    /**
     * Validates the contents of a bulk upload. This is intended for use when it is being submitted for approval.
     * Validation is required on the business logic side as a result of the ability to save drafts
     *
     * @param bulkUpload bulk upload to validate
     * @return true if the entries associated with the bulk upload validate. false otherwise
     */
    public static boolean validate(BulkUpload bulkUpload) {
        for (Entry entry : bulkUpload.getContents()) {
            EntryType type = EntryType.nameToType(entry.getRecordType());
            switch (type) {
                case STRAIN:
                    return validateStrain((Strain) entry);

                case PLASMID:
                    return validatePlasmid((Plasmid) entry);

                case PART:
                    return validateCommonFields(entry);

                case ARABIDOPSIS:
                    return validateCommonFields(entry);

                // unknown type
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * Creates new BulkUpload object populated with default values and of type specified in the param
     * Note that this is not saved in the database, It is the responsibility of the callee to save it.
     *
     * @return BulkUpload object
     */
    public static BulkUpload createNewBulkUpload(EntryAddType addType) {
        BulkUpload upload = new BulkUpload();
        upload.setName("Untitled");
        upload.setStatus(BulkUploadStatus.IN_PROGRESS);
        upload.setImportType(addType.toString());
        upload.setCreationTime(new Date(System.currentTimeMillis()));
        upload.setLastUpdateTime(upload.getCreationTime());
        return upload;
    }

    /**
     * Validates the fields of a strain to ensure that the required properties are valid. If a strain has a plasmid,
     * the plasmid is validated also
     *
     * @param strain strain entry to validate
     * @return true if strain validates, false otherwise
     */
    private static boolean validateStrain(Strain strain) {
        if (!validateCommonFields(strain))
            return false;

        return (!stringIsEmpty(strain.getSelectionMarkersAsString()));
    }

    private static boolean validatePlasmid(Plasmid plasmid) {
        if (!validateCommonFields(plasmid))
            return false;

        return (!stringIsEmpty(plasmid.getSelectionMarkersAsString()));
    }

    /**
     * validates fields that are common to all entries (e.g. BioSafety Level)
     *
     * @param entry entry whose common fields are being validated
     * @return true if validation completes successfully and no issues found, false otherwise
     */
    private static boolean validateCommonFields(Entry entry) {
        if (!BioSafetyOption.isValidOption(entry.getBioSafetyLevel()))
            return false;

        if (StatusType.displayValueOf(entry.getStatus()).isEmpty())
            return false;

        if (entry.getOneName() == null)
            return false;

        if (stringIsEmpty(entry.getCreator()))
            return false;

        if (stringIsEmpty(entry.getCreatorEmail()))
            return false;

        // principal investigator is required and that should create at least one funding source
        if (entry.getEntryFundingSources() == null || entry.getEntryFundingSources().isEmpty())
            return false;

        if (stringIsEmpty(entry.getShortDescription()))
            return false;

        return true;
    }

    private static boolean stringIsEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
