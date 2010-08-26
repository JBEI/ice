package org.jbei.ice.web.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.ProjectController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Project;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class VectorEditorToolPage extends WebPage {
    public VectorEditorToolPage(PageParameters parameters) {
        super(parameters);

        initialize(parameters);
    }

    private void initialize(PageParameters parameters) {
        WebComponent flashComponent = new WebComponent("flashComponent");

        String projectId = "";
        String accountSessionId = IceSession.get().getSessionKey();

        if (parameters != null && parameters.size() == 1) {
            projectId = parameters.getString("projectId");
        }

        ResourceReference vetResourceReference = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.VET_RESOURCE_LOCATION + "VectorEditor.swf?projectId=" + projectId
                        + "&sessionId=" + accountSessionId);

        flashComponent.add(new SimpleAttributeModifier("src", urlFor(vetResourceReference)));
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

        ProjectController projectController = new ProjectController(IceSession.get().getAccount());

        try {
            if (projectId != null && !projectId.isEmpty()) {
                Project project = projectController.getProjectByUUID(projectId);

                add(new Label("title", new Model<String>(project.getName())));
            } else {
                add(new Label("title", "New Project"));
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
    }
}