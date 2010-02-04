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
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.forms.PartUpdateFormPanel;
import org.jbei.ice.web.forms.PlasmidUpdateFormPanel;
import org.jbei.ice.web.forms.StrainUpdateFormPanel;

public class EntryUpdatePage extends ProtectedPage {

    private Entry entry;

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
                    TreeSet<String> uniquePlasmids = UtilsManager.getUniquePublicPlasmidNames();

                    dataMap = new MiniMap<String, Object>(4);

                    String selectionMarkersCollection = "";
                    String promotersCollection = "";
                    String originOfReplicationsCollection = "";
                    String plasmidsCollection = "";

                    for (String selectionMarker : uniqueSelectionMarkers) {
                        selectionMarkersCollection += "'"
                                + Utils.escapeSpecialJavascriptCharacters(selectionMarker) + "', ";
                    }

                    for (String promoter : uniquePromoters) {
                        promotersCollection += "'"
                                + Utils.escapeSpecialJavascriptCharacters(promoter) + "', ";
                    }

                    for (String originOfReplication : uniqueOriginOfReplications) {
                        originOfReplicationsCollection += "'"
                                + Utils.escapeSpecialJavascriptCharacters(originOfReplication)
                                + "', ";
                    }

                    for (String plasmid : uniquePlasmids) {
                        plasmidsCollection += "'"
                                + Utils.escapeSpecialJavascriptCharacters(plasmid) + "', ";
                    }

                    dataMap.put("selectionMarkersCollection", selectionMarkersCollection);
                    dataMap.put("promotersCollection", promotersCollection);
                    dataMap.put("originOfReplicationsCollection", originOfReplicationsCollection);
                    dataMap.put("plasmidsCollection", plasmidsCollection);
                }

                return dataMap;
            }
        };

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "jquery.autocomplete.js"));
        add(CSSPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.STYLES_RESOURCE_LOCATION + "jquery.autocomplete.css"));
        add(TextTemplateHeaderContributor.forJavaScript(UnprotectedPage.class,
                "autocompleteDataTemplate.js", autocompleteDataMap));

        int entryId = parameters.getInt("0");

        try {
            entry = AuthenticatedEntryManager.get(entryId, IceSession.get().getSessionKey());
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
