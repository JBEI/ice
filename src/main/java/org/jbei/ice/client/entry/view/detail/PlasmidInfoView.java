package org.jbei.ice.client.entry.view.detail;

import java.util.HashMap;

import org.jbei.ice.client.Page;
import org.jbei.ice.lib.shared.dto.entry.PlasmidData;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Details view for entries of type plasmid
 *
 * @author Hector Plahar
 */

public class PlasmidInfoView extends EntryInfoView<PlasmidData> {

    public PlasmidInfoView(PlasmidData data) {
        super(data);
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
        addLongField("Replicates In", info.getReplicatesIn());
    }

    private static class StrainWidget extends Composite {

        public StrainWidget(HashMap<Long, String> strains) {
            VerticalPanel panel = new VerticalPanel();
            initWidget(panel);

            for (Long id : strains.keySet()) {
                String partNumber = strains.get(id);
                panel.add(new Hyperlink(partNumber, Page.ENTRY_VIEW.getLink() + ";id=" + id.toString()));
            }
        }
    }
}
