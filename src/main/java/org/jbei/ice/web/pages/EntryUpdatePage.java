package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
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
import org.jbei.ice.web.forms.PartUpdateFormPanel;
import org.jbei.ice.web.forms.PlasmidUpdateFormPanel;
import org.jbei.ice.web.forms.StrainUpdateFormPanel;

public class EntryUpdatePage extends ProtectedPage {
    private Entry entry;

    public EntryUpdatePage(PageParameters parameters) {
        super(parameters);

        initializeControls(parameters);
    }

    @Override
    protected String getTitle() {
        return "Update Entry - " + super.getTitle();
    }

    private void initializeControls(PageParameters parameters) {
        if (parameters == null || parameters.size() == 0) {
            throw new ViewException("Parameters are missing!");
        }

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            String identifier = parameters.getString("0");

            entry = entryController.getByIdentifier(identifier);

            Panel panel = null;

            if (entry == null) {
                throw new ViewException("Couldn't find entry by parameters!");
            } else if (entry instanceof Strain) {
                panel = new StrainUpdateFormPanel("entry", (Strain) entry);
            } else if (entry instanceof Plasmid) {
                panel = new PlasmidUpdateFormPanel("entry", (Plasmid) entry);
            } else if (entry instanceof Part) {
                panel = new PartUpdateFormPanel("entry", (Part) entry);
            }

            add(panel);
        } catch (ControllerException e) {
            throw new ViewException(e);
        } catch (PermissionException e) {
            throw new RestartResponseAtInterceptPageException(PermissionDeniedPage.class);
        }
    }
}
