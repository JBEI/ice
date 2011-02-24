package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebComponent;
import org.jbei.ice.web.IceSession;

public class BulkImportPage extends ProtectedPage {

    public BulkImportPage(PageParameters parameters) {
        super(parameters);

        initialize(parameters);
    }

    private void initialize(PageParameters parameters) {
        WebComponent flashComponent = new WebComponent("bulkImport");

        String accountSessionId = IceSession.get().getSessionKey();

        ResourceReference biResourceReference = new ResourceReference(UnprotectedPage.class,
                "static/bi/EntryBulkImport.swf?sessionId=" + accountSessionId);

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
