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

        String fieldName;
        if (permission.isCanRead() || permission.isCanWrite()) {
            fieldName = "canRead";
        } else
            return;

        // account
        if (permission.getAccount() != null) {
            String existingFieldValue = document.get(fieldName);
            if (!permission.getAccount().getEmail().equalsIgnoreCase(existingFieldValue))
                luceneOptions.addFieldToDocument(fieldName, permission.getAccount().getEmail(), document);
        }

        // group
        if (permission.getGroup() != null) {
            String existingFieldValue = document.get(fieldName);
            if (!permission.getGroup().getUuid().equalsIgnoreCase(existingFieldValue))
                luceneOptions.addFieldToDocument(fieldName, permission.getGroup().getUuid(), document);
        }

        // TODO: adding entries to a folder that has permission granted to someone does not trigger this
        // bridge until an entry is edited.
    }
}
