package org.jbei.ice.storage.hibernate.bridge;

import org.hibernate.search.engine.backend.document.DocumentElement;
import org.hibernate.search.engine.backend.document.IndexFieldReference;
import org.hibernate.search.mapper.pojo.bridge.TypeBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.TypeBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.TypeBridgeWriteContext;
import org.jbei.ice.storage.model.Entry;

/**
 * @author Hector Plahar
 */
public class EntryBooleanPropertiesBridge implements TypeBinder {

    @Override
    public void bind(TypeBindingContext context) {
        // declare dependencies. these will be use to trigger reindexing when modified
        context.dependencies()
            .use("samples")
            .use("attachments")
            .use("sequence");

        // declare fields that will be populated
        IndexFieldReference<Boolean> hasSampleReference = context.indexSchemaElement()
            .field("hasSample", f -> f.asBoolean()).toReference();
        IndexFieldReference<Boolean> hasAttachmentReference = context.indexSchemaElement()
            .field("hasAttachment", f -> f.asBoolean()).toReference();
        IndexFieldReference<Boolean> hasSequenceReference = context.indexSchemaElement()
            .field("hasSequence", f -> f.asBoolean()).toReference();

        // pass references to bridge
        context.bridge(Entry.class, new EntryBridge(hasSampleReference, hasSequenceReference, hasAttachmentReference));
    }

    public static class EntryBridge implements TypeBridge<Entry> {

        private final IndexFieldReference<Boolean> hasSampleReference;
        private final IndexFieldReference<Boolean> hasSequenceReference;
        private final IndexFieldReference<Boolean> hasAttachmentReference;

        public EntryBridge(IndexFieldReference<Boolean> sampleReference,
                           IndexFieldReference<Boolean> sequenceReference,
                           IndexFieldReference<Boolean> attachmentReference) {
            this.hasSampleReference = sampleReference;
            this.hasSequenceReference = sequenceReference;
            this.hasAttachmentReference = attachmentReference;
        }

        @Override
        public void write(DocumentElement document, Entry entry, TypeBridgeWriteContext context) {
            // set value of has attachments
            if (entry.getAttachments() != null && !entry.getAttachments().isEmpty()) {
                document.addValue(this.hasAttachmentReference, Boolean.TRUE);
            } else {
                document.addValue(this.hasAttachmentReference, Boolean.FALSE);
            }

            // set value of has sample
            if (entry.getSamples() != null && !entry.getSamples().isEmpty())
                document.addValue(this.hasSampleReference, Boolean.TRUE);
            else
                document.addValue(this.hasSampleReference, Boolean.FALSE);

            // set value of has sequence index to true is entry has sequence associated with it, false otherwise
            document.addValue(this.hasSequenceReference, entry.getSequence() != null ? Boolean.TRUE : Boolean.FALSE);
        }

        @Override
        public void close() {
            TypeBridge.super.close();
        }
    }
}
