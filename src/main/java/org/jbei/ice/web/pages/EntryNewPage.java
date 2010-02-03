package org.jbei.ice.web.pages;

import java.util.Map;
import java.util.TreeSet;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.template.TextTemplateHeaderContributor;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.web.panels.EmptyMessagePanel;
import org.jbei.ice.web.panels.SelectNewEntryTypePanel;

public class EntryNewPage extends ProtectedPage {
    public EntryNewPage(PageParameters parameters) {
        super(parameters);

        initializeResources();

        SelectNewEntryTypePanel selectNewEntryTypePanel = new SelectNewEntryTypePanel(
                "selectNewEntryTypePanel");
        add(selectNewEntryTypePanel);

        Panel formPanel = new EmptyMessagePanel("formPanel", "");
        formPanel.setOutputMarkupId(true);
        add(formPanel);
    }

    private void initializeResources() {
        IModel<Map<String, Object>> autocompleteDataMap = new AbstractReadOnlyModel<Map<String, Object>>() {
            private static final long serialVersionUID = 1L;

            private Map<String, Object> dataMap;

            @Override
            public Map<String, Object> getObject() {
                if (dataMap == null) {
                    TreeSet<String> uniqueSelectionMarkers = UtilsManager
                            .getUniqueSelectionMarkers();
                    TreeSet<String> uniquePromoters = UtilsManager.getUniquePromoters();
                    TreeSet<String> uniqueOriginOfReplications = UtilsManager
                            .getUniqueOriginOfReplications();

                    dataMap = new MiniMap<String, Object>(3);

                    String selectionMarkersCollection = "";
                    String promotersCollection = "";
                    String originOfReplicationsCollection = "";

                    for (String selectionMarker : uniqueSelectionMarkers) {
                        selectionMarkersCollection += "'" + selectionMarker + "', ";
                    }

                    for (String promoter : uniquePromoters) {
                        promotersCollection += "'" + promoter + "', ";
                    }

                    for (String originOfReplication : uniqueOriginOfReplications) {
                        originOfReplicationsCollection += "'" + originOfReplication + "', ";
                    }

                    dataMap.put("selectionMarkersCollection", selectionMarkersCollection);
                    dataMap.put("promotersCollection", promotersCollection);
                    dataMap.put("originOfReplicationsCollection", originOfReplicationsCollection);
                }

                return dataMap;
            }
        };

        add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                "jquery-ui-1.7.2.custom.min.js"));
        add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                "jquery.autocomplete.js"));
        add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class, "ui/ui.core.js"));
        add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class,
                "ui/effects.core.js"));
        add(CSSPackageResource.getHeaderContribution(EntryNewPage.class, "ui/ui.all.css"));
        add(CSSPackageResource.getHeaderContribution(EntryNewPage.class, "jquery.autocomplete.css"));

        add(TextTemplateHeaderContributor.forJavaScript(EntryNewPage.class,
                "autocompleteDataTemplate.js", autocompleteDataMap));
    }
}
