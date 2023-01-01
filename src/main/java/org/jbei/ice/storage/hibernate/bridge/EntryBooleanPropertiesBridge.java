package org.jbei.ice.storage.hibernate.bridge;

import org.apache.lucene.document.Document;
import org.hibernate.search.mapper.pojo.bridge.RoutingBridge;
import org.hibernate.search.mapper.pojo.bridge.binding.RoutingBindingContext;
import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.RoutingBinder;
import org.hibernate.search.mapper.pojo.bridge.runtime.RoutingBridgeRouteContext;
import org.hibernate.search.mapper.pojo.route.DocumentRoutes;
import org.jbei.ice.storage.model.Entry;

import java.util.Map;

/**
 * @author Hector Plahar
 */
public class EntryBooleanPropertiesBridge implements RoutingBinder {

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

    @Override
    public void bind(RoutingBindingContext context) {
        context.dependencies().use(BOOLEAN_PROPERTY);

        context.bridge(Entry.class, );

    }


    public static class Bridge implements RoutingBridge<Entry> {
        @Override
        public void route(DocumentRoutes routes, Object entityIdentifier, Entry entry,
                          RoutingBridgeRouteContext context) {
            if (entry.getSamples() != null && !entry.getSamples().isEmpty()) {
                routes.addRoute();
            }

            switch (entry.) {
                case "hasSample":
                    booleanValue =;
                    break;

                case "hasAttachment":
                    booleanValue = (entry.getAttachments() != null && !entry.getAttachments().isEmpty());
                    break;

                case "hasSequence":
                    booleanValue = (entry.getSequence() != null);
                    break;
            }
        }

        @Override
        public void previousRoutes(DocumentRoutes documentRoutes, Object o, Entry entry, RoutingBridgeRouteContext routingBridgeRouteContext) {

        }

        @Override
        public void close() {
            RoutingBridge.super.close();
        }
    }
}
