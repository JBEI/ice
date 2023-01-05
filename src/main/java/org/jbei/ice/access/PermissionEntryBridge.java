package org.jbei.ice.access;

import org.apache.lucene.document.Document;
import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.mapper.pojo.bridge.TypeBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.TypeBridgeWriteContext;
import org.jbei.ice.storage.model.Permission;

/**
 * Permission bridge that indexes the fields needed by the
 * {@link org.jbei.ice.storage.hibernate.filter.EntrySecurityFilterFactory}
 *
 * @author Hector Plahar
 */

public class PermissionEntryBridge implements TypeBinder {

//    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
//        if (value == null)
//            return;
//
//        Permission permission = (Permission) value;
//        if (permission.getEntry() == null && permission.getFolder() == null)
//            return;
//
//        if (!permission.isCanRead() && !permission.isCanWrite())
//            return;
//
//        // account
//        if (permission.getAccount() != null && !fieldValueExists(document, permission.getAccount().getEmail())) {
//            luceneOptions.addFieldToDocument(IndexField.CAN_READ, permission.getAccount().getEmail(), document);
//        }
//
//        // group
//        if (permission.getGroup() != null && !fieldValueExists(document, permission.getGroup().getUuid())) {
//            luceneOptions.addFieldToDocument(IndexField.CAN_READ, permission.getGroup().getUuid(), document);
//        }
//    }

    protected boolean fieldValueExists(Document document, String value) {
        String[] values = document.getValues(IndexField.CAN_READ);
        if (values == null)
            return false;

        for (String fieldValue : values) {
            if (fieldValue.equalsIgnoreCase(value))
                return true;
        }

        return false;
    }

    @Override
    public void bind(TypeBindingContext context) {
        context.dependencies()
            .use("account")
            .use("group");

        IndexFieldReference<String> accountReference = context.indexSchemaElement()
            .field("canReadAccount", f -> f.asString()).toReference();

        IndexFieldReference<String> groupReference = context.indexSchemaElement()
            .field("canReadGroup", f -> f.asString()).toReference();

        context.bridge(Permission.class, new PermissionBridge(accountReference, groupReference));
    }

    public static class PermissionBridge implements TypeBridge<Permission> {

        private final IndexFieldReference<String> accountReference;
        private final IndexFieldReference<String> groupReference;

        public PermissionBridge(IndexFieldReference<String> accountReference, IndexFieldReference<String> groupReference) {
            this.accountReference = accountReference;
            this.groupReference = groupReference;
        }

        @Override
        public void write(DocumentElement document, Permission permission, TypeBridgeWriteContext typeBridgeWriteContext) {
        }

        @Override
        public void close() {
            TypeBridge.super.close();
        }
    }
}
