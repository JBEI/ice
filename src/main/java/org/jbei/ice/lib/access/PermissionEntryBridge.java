package org.jbei.ice.lib.access;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.jbei.ice.storage.model.Permission;

/**
 * Permission bridge that indexes the fields needed by the
 * {@link org.jbei.ice.storage.hibernate.filter.EntrySecurityFilterFactory}
 *
 * @author Hector Plahar
 */
public class PermissionEntryBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        if (value == null)
            return;

        Permission permission = (Permission) value;
        if (permission.getEntry() == null && permission.getFolder() == null)
            return;

        if (!permission.isCanRead() && permission.isCanWrite())
            return;

        // account
        if (permission.getAccount() != null && !fieldValueExists(document, permission.getAccount().getEmail())) {
            luceneOptions.addFieldToDocument(IndexField.CAN_READ, permission.getAccount().getEmail(), document);
        }

        // group
        if (permission.getGroup() != null && !fieldValueExists(document, permission.getGroup().getUuid())) {
            luceneOptions.addFieldToDocument(IndexField.CAN_READ, permission.getGroup().getUuid(), document);
        }
    }

    protected boolean fieldValueExists(Document document, String value) {
        String[] values = document.getValues(IndexField.CAN_READ);
        if (values == null || values.length == 0)
            return false;

        for (String fieldValue : values) {
            if (fieldValue.equalsIgnoreCase(value))
                return true;
        }

        return false;
    }
}
