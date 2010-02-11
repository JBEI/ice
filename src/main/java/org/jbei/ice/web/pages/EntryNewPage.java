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
import org.jbei.ice.lib.utils.Utils;
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
                    TreeSet<String> uniquePlasmids = UtilsManager.getUniquePublicPlasmidNames();

                    dataMap = new MiniMap<String, Object>(4);

                    StringBuilder selectionMarkersCollection = new StringBuilder();
                    StringBuilder promotersCollection = new StringBuilder();
                    StringBuilder originOfReplicationsCollection = new StringBuilder();
                    StringBuilder plasmidsCollection = new StringBuilder();

                    for (String selectionMarker : uniqueSelectionMarkers) {
                        selectionMarkersCollection.append(
                                Utils.escapeSpecialJavascriptCharacters(selectionMarker)).append(
                                "', ");
                    }

                    for (String promoter : uniquePromoters) {
                        promotersCollection.append(
                                Utils.escapeSpecialJavascriptCharacters(promoter)).append("', ");
                    }

                    for (String originOfReplication : uniqueOriginOfReplications) {
                        originOfReplicationsCollection.append(
                                Utils.escapeSpecialJavascriptCharacters(originOfReplication))
                                .append("', ");
                    }

                    for (String plasmid : uniquePlasmids) {
                        plasmidsCollection.append(Utils.escapeSpecialJavascriptCharacters(plasmid))
                                .append("', ");
                    }

                    dataMap
                            .put("selectionMarkersCollection", selectionMarkersCollection
                                    .toString());
                    dataMap.put("promotersCollection", promotersCollection.toString());
                    dataMap.put("originOfReplicationsCollection", originOfReplicationsCollection
                            .toString());
                    dataMap.put("plasmidsCollection", plasmidsCollection.toString());
                }

                return dataMap;
            }
        };

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.autocomplete.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.autocomplete.css"));
        add(TextTemplateHeaderContributor.forJavaScript(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "autocompleteDataTemplate.js",
                autocompleteDataMap));
    }

    @Override
    protected String getTitle() {
        return "New Entry - " + super.getTitle();
    }
}
