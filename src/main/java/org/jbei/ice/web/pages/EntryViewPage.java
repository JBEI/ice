package org.jbei.ice.web.pages;

import java.util.ArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Part;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.panels.AttachmentsViewPanel;
import org.jbei.ice.web.panels.EntryTabPanel;
import org.jbei.ice.web.panels.MiniAttachmentsViewPanel;
import org.jbei.ice.web.panels.MiniPermissionViewPanel;
import org.jbei.ice.web.panels.PartViewPanel;
import org.jbei.ice.web.panels.PermissionEditPanel;
import org.jbei.ice.web.panels.PlasmidViewPanel;
import org.jbei.ice.web.panels.SampleViewPanel;
import org.jbei.ice.web.panels.SequenceAnalysisViewPanel;
import org.jbei.ice.web.panels.SequenceViewPanel;
import org.jbei.ice.web.panels.StrainViewPanel;

public class EntryViewPage extends ProtectedPage {
    public Entry entry = null;

    public Component displayPanel;
    public Component generalPanel;
    public Component samplesPanel;
    public Component attachmentsPanel;
    public Component sequencePanel;
    public Component permissionPanel;

    public String subPage = null;
    public String entryTitle = "";

    private final String SAMPLES_URL_KEY = "samples";
    private final String SEQUENCE_URL_KEY = "sequence";
    private final String SEQUENCE_ANALYSIS_URL_KEY = "seqanalysis";
    private final String ATTACHMENTS_URL_KEY = "attachments";
    private final String PERMISSIONS_URL_KEY = "permission";

    public EntryViewPage(PageParameters parameters) {
        super(parameters);

        processPageParameters(parameters);

        subPage = parameters.getString("1");

        String recordType = JbeiConstants.getRecordType(entry.getRecordType());
        entryTitle = recordType + ": " + entry.getNamesAsString();
        add(new Label("titleName", entryTitle));

        Panel entryTabPanel = new EntryTabPanel("entryTabPanel", subPage, entry);
        entryTabPanel.setOutputMarkupId(true);
        add(entryTabPanel);
        add(renderAddToWorkspaceLink());

        generalPanel = makeSubPagePanel(entry);
        displayPanel = generalPanel;
        add(displayPanel);
        ArrayList<Panel> sidePanels = new ArrayList<Panel>();
        try {
            WorkspaceManager.setVisited(entry);
        } catch (ManagerException e) {
            throw new ViewException(e);
        }

        Panel miniAttachmentsPanel = new MiniAttachmentsViewPanel("sidePanel", entry);
        sidePanels.add(miniAttachmentsPanel);

        EntryController entryController = new EntryController(IceSession.get().getAccount());
        try {
            if (entryController.hasWritePermission(entry)) {
                Panel miniPermissionPanel = new MiniPermissionViewPanel("sidePanel", entry);
                sidePanels.add(miniPermissionPanel);
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        if (subPage != null
                && (subPage.equals(ATTACHMENTS_URL_KEY) || subPage.equals(PERMISSIONS_URL_KEY))) {
            sidePanels.clear();
        }
        ListView<Panel> sidePanelsListView = new ListView<Panel>("sidePanels", sidePanels) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Panel> item) {
                Panel panel = item.getModelObject();
                item.add(panel);
            }
        };
        add(sidePanelsListView);
    }

    @Override
    protected String getTitle() {
        return entryTitle + " - " + super.getTitle();
    }

    private void processPageParameters(PageParameters parameters) {
        if (parameters == null || parameters.size() == 0) {
            throw new ViewException("Parameters are missing!");
        }

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        String identifier = parameters.getString("0");
        try {
            entry = entryController.getByIdentifier(identifier);
        } catch (ControllerException e) {
            throw new ViewException(e);
        } catch (PermissionException e) {
            throw new ViewPermissionException("No permission to view entry!", e);
        }
        if (entry == null) {
            throw new RestartResponseAtInterceptPageException(PermissionDeniedPage.class);
        }

    }

    private Panel makeSubPagePanel(Entry entry) {
        if (subPage == null) {
            return makeGeneralPanel(entry);
        } else if (subPage.equals(SEQUENCE_URL_KEY)) {
            return makeSequencePanel(entry);
        } else if (subPage.equals(SEQUENCE_ANALYSIS_URL_KEY)) {
            return makeSequenceAnalysisPanel(entry);
        } else if (subPage.equals(SAMPLES_URL_KEY)) {
            return makeSamplesPanel(entry);
        } else if (subPage.equals(ATTACHMENTS_URL_KEY)) {
            return makeAttachmentsPanel(entry);
        } else if (subPage.equals(PERMISSIONS_URL_KEY)) {
            return makePermissionPanel(entry);
        } else {
            return makeGeneralPanel(entry);
        }
    }

    private Panel makeGeneralPanel(Entry entry) {
        Panel panel = null;

        if (entry instanceof Strain) {
            panel = new StrainViewPanel("centerPanel", (Strain) entry);
        } else if (entry instanceof Plasmid) {
            panel = new PlasmidViewPanel("centerPanel", (Plasmid) entry);
        } else if (entry instanceof Part) {
            panel = new PartViewPanel("centerPanel", (Part) entry);
        }

        if (panel != null) {
            panel.setOutputMarkupId(true);
        }

        return panel;
    }

    private Panel makeSamplesPanel(Entry entry) {
        Panel panel = new SampleViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel makeAttachmentsPanel(Entry entry) {
        Panel panel = new AttachmentsViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel makeSequencePanel(Entry entry) {
        Panel panel = new SequenceViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel makeSequenceAnalysisPanel(Entry entry) {
        Panel panel = new SequenceAnalysisViewPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    private Panel makePermissionPanel(Entry entry) {
        Panel panel = new PermissionEditPanel("centerPanel", entry);
        panel.setOutputMarkupId(true);
        return panel;
    }

    @SuppressWarnings("unchecked")
    private AjaxLink renderAddToWorkspaceLink() {
        final ResourceReference notInWorkspaceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "star-empty.png");
        final ResourceReference inWorkspaceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "star-filled.png");
        AjaxLink addToWorkspaceLink = new AjaxLink("addToWorkspaceLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                Workspace workspace = new Workspace(IceSession.get().getAccount(), entry);
                try {
                    WorkspaceManager.addOrUpdate(workspace);
                } catch (ManagerException e) {
                    throw new ViewException(e);
                }
                Image image = new Image("starImage", inWorkspaceImage);
                this.replace(image);
                getParent().replace(this);
                target.addComponent(this);
            }
        };
        if (WorkspaceManager.hasEntry(IceSession.get().getAccount(), entry)) {
            addToWorkspaceLink.add(new Image("starImage", inWorkspaceImage));
        } else {
            addToWorkspaceLink.add(new Image("starImage", notInWorkspaceImage));
        }
        addToWorkspaceLink.setOutputMarkupId(true);
        return addToWorkspaceLink;
    }
}
