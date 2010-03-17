package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.panels.PartSimpleViewPanel;
import org.jbei.ice.web.panels.PlasmidSimpleViewPanel;
import org.jbei.ice.web.panels.StrainSimpleViewPanel;

public class EntryTipPage extends ProtectedPage {
    public Entry entry;

    public EntryTipPage(PageParameters parameters) {
        super(parameters);

        initializeControls(parameters);
    }

    @Override
    protected void initializeComponents() {
        // keep it empty on purpose
    }

    @Override
    protected void initializeStyles() {
        // keep it empty on purpose
    }

    @Override
    protected void initializeJavascript() {
        // keep it empty on purpose
    }

    private void initializeControls(PageParameters parameters) {
        Panel panel = null;

        if (parameters == null || parameters.size() == 0) {
            throw new ViewException("Parameters are missing!");
        } else {
            EntryController entryController = new EntryController(IceSession.get().getAccount());

            try {
                String identifier = parameters.getString("0");

                Entry entry = entryController.getByIdentifier(identifier);

                if (entry == null) {
                    panel = new EmptyPanel("centerPanel");
                } else if (entry instanceof Strain) {
                    panel = new StrainSimpleViewPanel("centerPanel", (Strain) entry);
                } else if (entry instanceof Plasmid) {
                    panel = new PlasmidSimpleViewPanel("centerPanel", (Plasmid) entry);
                } else if (entry instanceof Part) {
                    panel = new PartSimpleViewPanel("centerPanel", (Part) entry);
                } else {
                    panel = new EmptyPanel("centerPanel");
                }
            } catch (ControllerException e) {
                throw new ViewException(e);
            } catch (PermissionException e) {
                throw new ViewPermissionException("No permissions to view this entry!", e);
            }
        }

        panel.setOutputMarkupId(true);
        add(panel);
    }
}
