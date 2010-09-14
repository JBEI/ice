package org.jbei.ice.web.panels;

import java.util.LinkedHashSet;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.UtilsManager;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.utils.WebUtils;

public class PlasmidViewPanel extends AbstractEntryViewPanel<Plasmid> {
    private static final long serialVersionUID = 1L;

    public PlasmidViewPanel(String id, Plasmid entry) {
        super(id, new Model<Plasmid>(entry));

        renderTopLink();

        renderSelectionMarkers();
        renderOriginOfReplication();
        renderPromoters();
        renderLinksToStrain();
        renderBackbone();
    }

    protected void renderOriginOfReplication() {
        add(new Label("originOfReplication", WebUtils.linkifyText(getEntry()
                .getOriginOfReplication())).setEscapeModelStrings(false));
    }

    protected void renderPromoters() {
        add(new Label("promoters", WebUtils.linkifyText(getEntry().getPromoters()))
                .setEscapeModelStrings(false));
    }

    protected void renderLinksToStrain() {
        LinkedHashSet<Strain> temp = new LinkedHashSet<Strain>();

        try {
            temp = UtilsManager.getStrainsForPlasmid(getEntry());
        } catch (ManagerException e) {
            throw new ViewException(e);
        }

        add(new Label("linksToStrains", WebUtils.makeEntryLinks(temp)).setEscapeModelStrings(false));
    }

    protected void renderBackbone() {
        add(new Label("backbone", WebUtils.linkifyText(getEntry().getBackbone()))
                .setEscapeModelStrings(false));
    }
}
