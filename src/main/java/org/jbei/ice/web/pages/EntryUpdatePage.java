package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.AuthenticatedEntryManager;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.web.forms.PartUpdateFormPanel;
import org.jbei.ice.web.forms.PlasmidUpdateFormPanel;
import org.jbei.ice.web.forms.StrainUpdateFormPanel;

public class EntryUpdatePage extends ProtectedPage {
    private Entry entry;

    public EntryUpdatePage(PageParameters parameters) {
        super(parameters);
        int entryId = parameters.getInt("0");

        if (!PermissionManager.hasWritePermission(entryId)) {
            throw new RestartResponseAtInterceptPageException(PermissionDeniedPage.class);
        }

        try {
            entry = AuthenticatedEntryManager.get(entryId);
            String recordType = entry.getRecordType();
            if (recordType.equals("strain")) {
                StrainUpdateFormPanel panel = new StrainUpdateFormPanel("entry", (Strain) entry);
                add(panel);
            } else if (recordType.equals("plasmid")) {
                PlasmidUpdateFormPanel panel = new PlasmidUpdateFormPanel("entry", (Plasmid) entry);
                add(panel);
            } else if (recordType.equals("part")) {
                PartUpdateFormPanel panel = new PartUpdateFormPanel("entry", (Part) entry);
                add(panel);
            }
        } catch (ManagerException e) {
            e.printStackTrace();
        } catch (PermissionException e) {
            throw new RestartResponseAtInterceptPageException(PermissionDeniedPage.class);
        }
    }

    @Override
    protected String getTitle() {
        return "Update Entry - " + super.getTitle();
    }
}
