package org.jbei.ice.storage.hibernate.dao;

import org.jbei.ice.shared.ColumnField;

public class EntryAccessorUtilities {

    public static String columnFieldToString(ColumnField field) {
        if (field == null)
            return "creationTime";

        switch (field) {
            case TYPE:
                return "recordType";

            case STATUS:
                return "status";

            case PART_ID:
                return "partNumber";

            case NAME:
                return "name";

            case ALIAS:
                return "alias";

            case SUMMARY:
                return "shortDescription";

            case CREATED:
            default:
                return "creationTime";
        }
    }
}
