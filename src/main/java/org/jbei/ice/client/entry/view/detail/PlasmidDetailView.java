package org.jbei.ice.client.entry.view.detail;

import java.util.HashMap;

import org.jbei.ice.client.Page;
import org.jbei.ice.shared.dto.PlasmidInfo;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Details view for entries of type plasmid
 *
 * @author Hector Plahar
 */

public class PlasmidDetailView extends EntryDetailView<PlasmidInfo> {

    public PlasmidDetailView(PlasmidInfo info) {
        super(info);
    }

    @Override
    protected void addShortFieldValues() {
        if (info.getStrains().isEmpty())
            addShortField("Strains", "");
        else {
            addShortField("Strains", new StrainWidget(info.getStrains()));
        }
    }

    @Override
    protected void addLongFields() {
        addLongField("Backbone", info.getBackbone());
        addLongField("Origin Of Replication", info.getOriginOfReplication());
        addLongField("Selection Markers", info.getSelectionMarkers());
        addLongField("Promoters", info.getPromoters());
    }

    private static class StrainWidget extends Composite {

        public StrainWidget(HashMap<Long, String> strains) {
            VerticalPanel panel = new VerticalPanel();
            initWidget(panel);

            for (Long id : strains.keySet()) {
                String partNumber = strains.get(id);
                panel.add(new Hyperlink(partNumber, Page.ENTRY_VIEW.getLink() + ";id="
                        + id.toString()));
            }
        }
    }
}
