package org.jbei.ice.access;

import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;

public class EntryFolderPermissionBridge implements TypeBinder {

//    @Override
//    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
//        Folder folder = (Folder) value;
//        if (folder.getPermissions() == null || folder.getPermissions().isEmpty())
//            return;
//
//        for (Permission permission : folder.getPermissions()) {
//            if (permission.getFolder() == null)
//                continue;
//
//            if (permission.getFolder().getId() != folder.getId())
//                continue;
//
//            if (!permission.isCanRead() && !permission.isCanWrite())
//                continue;
//
//            String existingFieldValue = document.get(IndexField.CONTAINED_IN);
//            if (String.valueOf(permission.getFolder().getId()).equalsIgnoreCase(existingFieldValue))
//                continue;
//
//            luceneOptions.addFieldToDocument(IndexField.CONTAINED_IN, String.valueOf(folder.getId()), document);
//        }
//    }

    @Override
    public void bind(TypeBindingContext context) {
        context.dependencies().use("permissions");
    }
}
