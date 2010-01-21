package org.jbei.ice.web.pages;

import java.util.Map;
import java.util.TreeSet;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.template.TextTemplateHeaderContributor;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.forms.PartUpdateFormPanel;
import org.jbei.ice.web.forms.PlasmidUpdateFormPanel;
import org.jbei.ice.web.forms.StrainUpdateFormPanel;

public class EntryUpdatePage extends ProtectedPage {
    public EntryUpdatePage(PageParameters parameters) {
        super(parameters);

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

        add(JavascriptPackageResource.getHeaderContribution(EntryNewPage.class, "jquery-1.3.2.js"));
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

        int entryId = parameters.getInt("0");
        Entry entry;
        try {
            entry = EntryManager.get(entryId);
            String recordType = entry.getRecordType();
            if (recordType.equals("strain")) {
                StrainUpdateFormPanel panel = new StrainUpdateFormPanel("entry", (Strain) entry);
                add(panel);
            } else if (recordType.equals("plasmid")) {
                PlasmidUpdateFormPanel panel = new PlasmidUpdateFormPanel("entry", (Plasmid) entry);
                add(panel);
            } else if (recordType.equals("part")) {
                PartUpdateFormPanel panel = new PartUpdateFormPanel("entry", (Part) entry);
                add(panel);
            }
        } catch (ManagerException e) {
            e.printStackTrace();
        }
    }
}
