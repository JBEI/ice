package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
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

    public Component displayPanel;
    public Component generalPanel;

    public BookmarkablePageLink<Object> generalLink;
    public String subPage = null;

    public EntryTipPage(PageParameters parameters) {
        super(parameters);

        int entryId = parameters.getInt("0");
        subPage = parameters.getString("1");

        try {
            entry = AuthenticatedEntryManager.get(entryId, IceSession.get().getSessionKey());
        } catch (ManagerException e) {
            e.printStackTrace();
        } catch (PermissionException e) {
            // do nothing
        }

        generalPanel = makeGeneralPanel(entry);
        displayPanel = generalPanel;
        add(displayPanel);
    }

    @Override
    protected void initializeComponents() {
        // keep it empty on purpose
    }

    public Panel makeGeneralPanel(Entry entry) {
        String recordType = entry.getRecordType();

        Panel panel = null;
        if (recordType.equals("strain")) {
            panel = new StrainSimpleViewPanel("centerPanel", (Strain) entry, true);
        } else if (recordType.equals("plasmid")) {
            panel = new PlasmidSimpleViewPanel("centerPanel", (Plasmid) entry, true);
        } else if (recordType.equals("part")) {
            panel = new PartSimpleViewPanel("centerPanel", (Part) entry, true);
        }

        panel.setOutputMarkupId(true);

        return panel;
    }
}
