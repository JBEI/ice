package org.jbei.ice.storage.hibernate.bridge;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.ParameterizedBridge;
import org.jbei.ice.storage.model.Entry;

import java.util.Map;

/**
 * @author Hector Plahar
 */
public class EntryBooleanPropertiesBridge implements FieldBridge, ParameterizedBridge {

    public static final String BOOLEAN_PROPERTY = "boolean";
    private String field;

    @Override
    public void setParameterValues(Map parameters) {
        field = (String) parameters.get(BOOLEAN_PROPERTY);
    }

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        if (value == null)
            return;

        Entry entry = (Entry) value;
        boolean booleanValue = false;

        switch (field) {
            case "hasSample":
                booleanValue = (entry.getSamples() != null && !entry.getSamples().isEmpty());
                break;

            case "hasAttachment":
                booleanValue = (entry.getAttachments() != null && !entry.getAttachments().isEmpty());
                break;

            case "hasSequence":
                booleanValue = (entry.getSequence() != null);
                break;
        }

        luceneOptions.addFieldToDocument(field, Boolean.toString(booleanValue), document);
    }
}