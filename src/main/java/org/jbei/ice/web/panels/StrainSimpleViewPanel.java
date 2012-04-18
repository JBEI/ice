package org.jbei.ice.web.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.utils.WebUtils;

public class StrainSimpleViewPanel extends SimpleEntryViewPanel<Strain> {
    private static final long serialVersionUID = 1L;

    public StrainSimpleViewPanel(String id, Strain entry) {
        super(id, new Model<Strain>(entry));

        renderSelectionMarkers();
        renderHost();
        renderGenotypePhenotype();
        renderPlasmids();
    }

    protected void renderHost() {
        add(new Label("host", WebUtils.linkifyText(IceSession.get().getAccount(), getEntry()
                .getHost())).setEscapeModelStrings(false));
    }

    protected void renderGenotypePhenotype() {
        add(new Label("genotypePhenotype", WebUtils.linkifyText(IceSession.get().getAccount(),
            getEntry().getGenotypePhenotype())).setEscapeModelStrings(false));
    }

    protected void renderPlasmids() {
        add(new Label("plasmids", WebUtils.linkifyText(IceSession.get().getAccount(), getEntry()
                .getPlasmids())).setEscapeModelStrings(false));
    }
}
