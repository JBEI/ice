package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
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

        int entryId = parameters.getInt("0");

        try {
            entry = AuthenticatedEntryManager.get(entryId, IceSession.get().getSessionKey());
        } catch (ManagerException e) {
            e.printStackTrace();
        } catch (PermissionException e) {
            // do nothing
        }

        String recordType = entry.getRecordType();

        Panel panel = null;

        try {
            if (recordType.equals("strain")) {
                panel = new StrainSimpleViewPanel("centerPanel", (Strain) entry);
            } else if (recordType.equals("plasmid")) {
                panel = new PlasmidSimpleViewPanel("centerPanel", (Plasmid) entry);
            } else if (recordType.equals("part")) {
                panel = new PartSimpleViewPanel("centerPanel", (Part) entry);
            }
        } catch (Exception e) {
            e.printStackTrace();

            panel = new EmptyPanel("centerPanel");
        }

        panel.setOutputMarkupId(true);

        add(panel);
    }

    @Override
    protected void initializeComponents() {
        // keep it empty on purpose
    }

    protected void initializeStyles() {
        // keep it empty on purpose
    }

    protected void initializeJavascript() {
        // keep it empty on purpose
    }
}
