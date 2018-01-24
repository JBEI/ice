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
 * @author William Morrell
 */
public class PermissionEntryBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        if (value == null) {
            return;
        }

        Permission permission = (Permission) value;
        if (permission.getEntry() == null && permission.getFolder() == null) {
            return;
        }

        String fieldName;
        if (permission.isCanRead() || permission.isCanWrite()) {
            fieldName = "canRead_";
        } else {
            // doing nothing if no read or write permission
            return;
        }

        // account
        if (permission.getAccount() != null) {
            String email = permission.getAccount().getEmail();
            String existingFieldValue = document.get(fieldName + email);
            if (!email.equalsIgnoreCase(existingFieldValue)) {
                luceneOptions.addFieldToDocument(fieldName + email, email, document);
            }
        }

        // group
        if (permission.getGroup() != null) {
            String groupId = permission.getGroup().getUuid();
            String existingFieldValue = document.get(fieldName + groupId);
            if (!groupId.equalsIgnoreCase(existingFieldValue)) {
                luceneOptions.addFieldToDocument(fieldName + groupId, groupId, document);
            }
        }

        // TODO: adding entries to a folder that has permission granted to someone does not trigger this
        // bridge until an entry is edited.
    }
}
