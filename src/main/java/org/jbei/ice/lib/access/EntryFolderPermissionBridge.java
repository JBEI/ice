package org.jbei.ice.lib.access;


import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Permission;

public class EntryFolderPermissionBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        Folder folder = (Folder) value;
        Logger.info("folder " + folder.getId());

        if (folder.getPermissions() == null || folder.getPermissions().isEmpty())
            return;

        String fieldName = "containedIn";

        for (Permission permission : folder.getPermissions()) {
            if (permission.getFolder() == null)
                continue;

            if (permission.getFolder().getId() != folder.getId())
                continue;

            if (!permission.isCanRead() && !permission.isCanWrite())
                continue;

            String existingFieldValue = document.get(fieldName);
            if (String.valueOf(permission.getFolder().getId()).equalsIgnoreCase(existingFieldValue))
                continue;

            luceneOptions.addFieldToDocument(fieldName, String.valueOf(folder.getId()), document);
        }
    }
}
