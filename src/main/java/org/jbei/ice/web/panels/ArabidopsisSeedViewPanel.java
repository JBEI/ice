package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.ArabidopsisSeed;
import org.jbei.ice.web.utils.WebUtils;

public class ArabidopsisSeedViewPanel extends AbstractEntryViewPanel<ArabidopsisSeed> {
    private static final long serialVersionUID = 1L;

    public ArabidopsisSeedViewPanel(String id, ArabidopsisSeed entry) {
        super(id, new Model<ArabidopsisSeed>(entry));

        renderTopLink();

        renderSelectionMarkers();
        renderHomozygosity();
        renderEcotype();
        renderHarvestDate();
        renderParents();
        renderGeneration();
        renderPlantType();
    }

    protected void renderHomozygosity() {
        add(new Label("homozygosity", WebUtils.linkifyText(getEntry().getHomozygosity()))
                .setEscapeModelStrings(false));
    }

    protected void renderEcotype() {
        add(new Label("ecotype", WebUtils.linkifyText(getEntry().getEcotype()))
                .setEscapeModelStrings(false));
    }

    protected void renderParents() {
        add(new Label("parents", WebUtils.linkifyText(getEntry() // 
                .getParents())).setEscapeModelStrings(false));
    }

    protected void renderGeneration() {
        String labelString = ArabidopsisSeed.getGenerationOptionsMap().get(
            getEntry().getGeneration().name());
        add(new Label("generation", labelString).setEscapeModelStrings(false));
    }

    protected void renderPlantType() {
        String labelString = ArabidopsisSeed.getPlantTypeOptionsMap().get(
            getEntry().getPlantType().name());
        add(new Label("plantType", labelString).setEscapeModelStrings(false));
    }

    protected void renderHarvestDate() {
        if (getEntry().getHarvestDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
            String harvestDate = dateFormat.format(getEntry().getHarvestDate());

            add(new Label("harvestDate", harvestDate));
        } else {
            add(new Label("harvestDate", ""));
        }
    }

}
