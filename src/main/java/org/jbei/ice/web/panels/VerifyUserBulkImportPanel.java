package org.jbei.ice.web.panels;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.pages.UnprotectedPage;

public class VerifyUserBulkImportPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public VerifyUserBulkImportPanel(String id, String importId) {
        super(id);

        addHeaders();
        addFlashComponent(importId);
    }

    protected void addHeaders() {
        add(new Label("current_panel_header", "Verify Bulk Import"));
    }

    protected void addFlashComponent(String importId) {
        WebComponent flashComponent = new WebComponent("bulkImport");
        String accountSessionId = IceSession.get().getSessionKey();
        ResourceReference biResourceReference = new ResourceReference(UnprotectedPage.class,
                "static/bi/EntryBulkImport.swf?sessionId=" + accountSessionId + "&importId="
                        + importId);
        // ?debug=true&sessionId="        + IceSession.get().getSessionKey()

        flashComponent.add(new SimpleAttributeModifier("src", urlFor(biResourceReference)));
        flashComponent.add(new SimpleAttributeModifier("quality", "high"));
        flashComponent.add(new SimpleAttributeModifier("bgcolor", "#869ca7"));
        flashComponent.add(new SimpleAttributeModifier("width", "100%"));
        flashComponent.add(new SimpleAttributeModifier("height", "100%"));
        flashComponent.add(new SimpleAttributeModifier("name", "EntryBulkImport"));
        flashComponent.add(new SimpleAttributeModifier("align", "middle"));
        flashComponent.add(new SimpleAttributeModifier("play", "true"));
        flashComponent.add(new SimpleAttributeModifier("loop", "false"));
        flashComponent.add(new SimpleAttributeModifier("type", "application/x-shockwave-flash"));
        flashComponent.add(new SimpleAttributeModifier("pluginspage",
                "http://www.adobe.com/go/getflashplayer"));

        this.add(flashComponent);
    }
}
