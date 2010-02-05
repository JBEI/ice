package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.jbei.ice.web.IceSession;

public class VectorEditorPage extends WebPage {
    // Constructor
    public VectorEditorPage(PageParameters parameters) {
        WebComponent flashComponent = new WebComponent("flashComponent");

        ResourceReference veResourceReference = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.VE_RESOURCE_LOCATION + "VectorEditor.swf?entryId="
                        + parameters.getString("entryId") + "&sessionId="
                        + IceSession.get().getSessionKey());

        System.out.println(urlFor(veResourceReference));

        flashComponent.add(new SimpleAttributeModifier("src", urlFor(veResourceReference)));
        flashComponent.add(new SimpleAttributeModifier("quality", "high"));
        flashComponent.add(new SimpleAttributeModifier("bgcolor", "#869ca7"));
        flashComponent.add(new SimpleAttributeModifier("width", "100%"));
        flashComponent.add(new SimpleAttributeModifier("height", "100%"));
        flashComponent.add(new SimpleAttributeModifier("name", "VectorEditor"));
        flashComponent.add(new SimpleAttributeModifier("align", "middle"));
        flashComponent.add(new SimpleAttributeModifier("play", "true"));
        flashComponent.add(new SimpleAttributeModifier("loop", "false"));
        flashComponent.add(new SimpleAttributeModifier("type", "application/x-shockwave-flash"));
        flashComponent.add(new SimpleAttributeModifier("pluginspage",
                "http://www.adobe.com/go/getflashplayer"));

        add(flashComponent);
    }
}
