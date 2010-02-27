package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;

public class VectorEditorPage extends WebPage {
    public VectorEditorPage(PageParameters parameters) {
        WebComponent flashComponent = new WebComponent("flashComponent");

        String entryRecordId = parameters.getString("entryId");
        String accountSessionId = IceSession.get().getSessionKey();

        ResourceReference veResourceReference = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.VE_RESOURCE_LOCATION + "VectorEditor.swf?entryId=" + entryRecordId
                        + "&sessionId=" + accountSessionId);

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

        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "extMouseWheel.js"));

        try {
            Entry entry = EntryManager.getByRecordId(entryRecordId);

            add(new Label("title", new Model<String>(entry.getNamesAsString())));
        } catch (ManagerException e) {
            Logger.error("Failed to pull entry by id '" + entryRecordId + "': "
                    + Utils.stackTraceToString(e));
        }
    }
}
