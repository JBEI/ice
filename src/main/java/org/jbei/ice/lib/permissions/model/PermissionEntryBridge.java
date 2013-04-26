package org.jbei.ice.lib.permissions.model;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * Permission bridge that indexes all accounts that can read the entry
 * that the indexes will be contained in
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

        String fieldName = null;
        if (permission.isCanRead() || permission.isCanWrite()) {
            fieldName = "canRead";
        }

        if (fieldName == null)
            return;

        // account
        if (permission.getAccount() != null) {
            luceneOptions.addFieldToDocument(fieldName, permission.getAccount().getEmail(), document);
        }

        // group
        if (permission.getGroup() != null) {
            luceneOptions.addFieldToDocument(fieldName, permission.getGroup().getUuid(), document);
        }

        // folder
        if (permission.getFolder() != null) {
            luceneOptions.addFieldToDocument(fieldName, "f_" + permission.getFolder().getId(), document);
        }

        // entry
        luceneOptions.addFieldToDocument(fieldName, permission.getEntry().getOwnerEmail(), document);
    }
}
