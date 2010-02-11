package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.panels.PartSimpleViewPanel;
import org.jbei.ice.web.panels.PlasmidSimpleViewPanel;
import org.jbei.ice.web.panels.StrainSimpleViewPanel;

public class EntryTipPage extends ProtectedPage {
    public Entry entry;

    public EntryTipPage(PageParameters parameters) {
        super(parameters);

        int entryId = 0;
        String identifier = parameters.getString("0");

        try {
            entryId = Integer.parseInt(identifier);
            entry = AuthenticatedEntryManager.get(entryId, IceSession.get().getSessionKey());
        } catch (NumberFormatException e) {
            // Not a number. Perhaps it's a part number or recordId?
            try {
                entry = AuthenticatedEntryManager.getByPartNumber(identifier, IceSession.get()
                        .getSessionKey());
                entryId = entry.getId();
            } catch (PermissionException e1) {
                // entryId is still 0
            } catch (ManagerException e1) {
                Logger.warn("EntryTipPage: " + e1.toString());
            }

            if (entryId == 0) {
                try {
                    entry = AuthenticatedEntryManager.getByRecordId(identifier, IceSession.get()
                            .getSessionKey());
                    entryId = entry.getId();
                } catch (PermissionException e1) {
                    // entryId is still 0
                } catch (ManagerException e1) {
                    Logger.warn("EntryTipPage: " + e1.toString());
                }
            }
        } catch (PermissionException e) {
            entryId = 0;
        } catch (ManagerException e) {
            Logger.warn("EntryTipPage: " + e.toString());
        }

        Panel panel = null;
        if (entryId == 0) {
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

        panel.setOutputMarkupId(true);
        add(panel);
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
}
