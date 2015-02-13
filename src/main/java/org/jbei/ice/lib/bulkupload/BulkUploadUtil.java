package org.jbei.ice.lib.bulkupload;

import org.apache.commons.lang.StringUtils;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.entry.model.Plasmid;
import org.jbei.ice.lib.entry.model.Strain;
import org.jbei.ice.lib.shared.BioSafetyOption;
import org.jbei.ice.lib.shared.StatusType;

/**
 * Utility class for Bulk Import
 *
 * @author Hector Plahar
 */
public class BulkUploadUtil {

    /**
     * Validates the contents of a bulk upload. This is intended for use when it is being submitted for approval.
     * Validation is required on the business logic side as a result of the ability to save drafts
     *
     * @param bulkUpload bulk upload to validate
     * @return true if the entries associated with the bulk upload validate. false otherwise
     */
    public static boolean validate(BulkUpload bulkUpload) {
        for (Entry entry : bulkUpload.getContents()) {
            if (!validateHelper(entry))
                return false;

            for (Entry linked : entry.getLinkedEntries()) {
                if (!validateHelper(linked))
                    return false;
            }
        }
        return true;
    }

    protected static boolean validateHelper(Entry entry) {
        boolean isValid = false;

        EntryType type = EntryType.nameToType(entry.getRecordType());
        switch (type) {
            case STRAIN:
                isValid = validateStrain((Strain) entry);
                break;

            case PLASMID:
                isValid = validatePlasmid((Plasmid) entry);
                break;

            case PART:
                isValid = validateCommonFields(entry);
                break;

            case ARABIDOPSIS:
                isValid = validateCommonFields(entry);
                break;
        }

        return isValid;
    }

    /**
     * Validates the fields of a strain to ensure that the required properties are valid. If a strain has a plasmid,
     * the plasmid is validated also
     *
     * @param strain strain entry to validate
     * @return true if strain validates, false otherwise
     */
    private static boolean validateStrain(Strain strain) {
        return validateCommonFields(strain) && (!strain.getSelectionMarkers().isEmpty());
    }

    private static boolean validatePlasmid(Plasmid plasmid) {
        return validateCommonFields(plasmid) && (!plasmid.getSelectionMarkers().isEmpty());
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

        if (entry.getName() == null)
            return false;

        if (StringUtils.isBlank(entry.getCreator()))
            return false;

        if (StringUtils.isBlank(entry.getCreatorEmail()))
            return false;

        // principal investigator is required and that should create at least one funding source
        if (StringUtils.isBlank(entry.getPrincipalInvestigator()))
            return false;

        return !StringUtils.isBlank(entry.getShortDescription());
    }
}
