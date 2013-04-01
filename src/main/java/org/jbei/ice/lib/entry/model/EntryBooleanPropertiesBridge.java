package org.jbei.ice.lib.entry.model;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.ParameterizedBridge;

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
        luceneOptions.addFieldToDocument(field, Boolean.toString(true), document);
    }
}