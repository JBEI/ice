package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.utils.WebUtils;

public class StrainViewPanel extends AbstractEntryViewPanel<Strain> {
    private static final long serialVersionUID = 1L;

    public StrainViewPanel(String id, Strain entry) {
        super(id, new Model<Strain>(entry));

        renderTopLink();

        renderSelectionMarkers();
        renderHost();
        renderGenotypePhenotype();
        renderPlasmids();
    }

    protected void renderHost() {
        add(new Label("host", getEntry().getHost()));
    }

    protected void renderGenotypePhenotype() {
        add(new Label("genotypePhenotype", getEntry().getGenotypePhenotype()));
    }

    protected void renderPlasmids() {
        add(new Label("plasmids", WebUtils.linkifyText(getEntry().getPlasmids()))
                .setEscapeModelStrings(false));
    }
}
