package org.jbei.ice.web.pages;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AttachmentController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.SequenceAnalysisController;
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

    public BookmarkablePageLink<Object> generalLink;
    public BookmarkablePageLink<Object> sequenceLink;
    public BookmarkablePageLink<Object> sequenceAnalysisLink;
    public BookmarkablePageLink<Object> samplesLink;
    public BookmarkablePageLink<Object> attachmentsLink;
    public BookmarkablePageLink<Object> permissionLink;

    public String subPage = null;
    public String entryTitle = "";

    private final String SAMPLES_URL_KEY = "samples";
    private final String SEQUENCE_URL_KEY = "sequence";
    private final String SEQUENCE_ANALYSIS_URL_KEY = "seqanalysis";
    private final String ATTACHMENTS_URL_KEY = "attachments";
    private final String PERMISSIONS_URL_KEY = "permission";

    public EntryViewPage(PageParameters parameters) {
        super(parameters);

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        processPageParameters(parameters);

        subPage = parameters.getString("1");

        String recordType = JbeiConstants.getRecordType(entry.getRecordType());
        entryTitle = recordType + ": " + entry.getNamesAsString();
        add(new Label("titleName", entryTitle));

        renderGeneralLink();
        renderSamplesLink();
        renderAttachmentsLink();
        renderSequenceLink();
        renderSequenceAnalysisLink();
        renderPermissionsLink();

        setActiveLink();

        add(generalLink);
        add(sequenceLink);
        add(sequenceAnalysisLink);
        add(samplesLink);
        add(attachmentsLink);
        add(permissionLink);
        add(renderAddToWorkspaceLink());

        try {
            if (!entryController.hasWritePermission(entry)) {
                permissionLink.setVisible(false);
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        generalPanel = makeSubPagePanel(entry);
        displayPanel = generalPanel;
        add(displayPanel);
        WorkspaceManager.setVisited(entry);
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

    private void setActiveLink() {
        SimpleAttributeModifier inactiveSimpleAttributeModifier = new SimpleAttributeModifier(
                "class", "inactive");
        SimpleAttributeModifier activeSimpleAttributeModifier = new SimpleAttributeModifier(
                "class", "active");

        generalLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        sequenceLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        sequenceAnalysisLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        samplesLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        attachmentsLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);
        permissionLink.add(inactiveSimpleAttributeModifier).setOutputMarkupId(true);

        if (subPage == null) {
            generalLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(SAMPLES_URL_KEY)) {
            samplesLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(ATTACHMENTS_URL_KEY)) {
            attachmentsLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(SEQUENCE_URL_KEY)) {
            sequenceLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(SEQUENCE_ANALYSIS_URL_KEY)) {
            sequenceAnalysisLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
        } else if (subPage.equals(PERMISSIONS_URL_KEY)) {
            permissionLink.add(activeSimpleAttributeModifier).setOutputMarkupId(true);
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

    private void renderGeneralLink() {
        generalLink = new BookmarkablePageLink<Object>("generalLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId()));

        generalLink.setOutputMarkupId(true);
    }

    private void renderSamplesLink() {
        samplesLink = new BookmarkablePageLink<Object>("samplesLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + SAMPLES_URL_KEY));

        samplesLink.setOutputMarkupId(true);

        SampleController sampleController = new SampleController(IceSession.get().getAccount());

        int numSamples = 0;

        try {
            numSamples = sampleController.getNumberOfSamples(entry);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        String samplesLabel = "Samples";
        if (numSamples > 0) {
            samplesLabel = samplesLabel + " (" + numSamples + ")";
        }

        samplesLink.add(new Label("samplesLabel", samplesLabel));
    }

    private void renderSequenceLink() {
        sequenceLink = new BookmarkablePageLink<Object>("sequenceLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + SEQUENCE_URL_KEY));

        sequenceLink.setOutputMarkupId(true);

        String sequenceLabel = "Sequence";

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            if (entryController.hasSequence(entry)) {
                sequenceLabel = sequenceLabel + " (1)";
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        sequenceLink.add(new Label("sequenceLabel", sequenceLabel));
    }

    private void renderSequenceAnalysisLink() {
        sequenceAnalysisLink = new BookmarkablePageLink<Object>("sequenceAnalysisLink",
                EntryViewPage.class, new PageParameters("0=" + entry.getId() + ",1="
                        + SEQUENCE_ANALYSIS_URL_KEY));

        sequenceAnalysisLink.setOutputMarkupId(true);

        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                IceSession.get().getAccount());

        String sequenceAnalysisLabel = "Seq. Analysis";
        int numTraceSequences;
        try {
            numTraceSequences = sequenceAnalysisController.getNumberOfTraceSequences(entry);

            if (numTraceSequences > 0) {
                sequenceAnalysisLabel = sequenceAnalysisLabel + " (" + numTraceSequences + ")";
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        sequenceAnalysisLink.add(new Label("sequenceAnalysisLabel", sequenceAnalysisLabel));
    }

    private void renderAttachmentsLink() {
        attachmentsLink = new BookmarkablePageLink<Object>("attachmentsLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + ATTACHMENTS_URL_KEY));

        attachmentsLink.setOutputMarkupId(true);

        AttachmentController attachmentController = new AttachmentController(IceSession.get()
                .getAccount());

        int numAttachments = 0;

        try {
            numAttachments = attachmentController.getNumberOfAttachments(entry);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        String attachmentsLabel = "Attachments";
        if (numAttachments > 0) {
            attachmentsLabel = attachmentsLabel + " (" + numAttachments + ")";
        }

        attachmentsLink.add(new Label("attachmentsLabel", attachmentsLabel));
    }

    private void renderPermissionsLink() {
        permissionLink = new BookmarkablePageLink<Object>("permissionLink", EntryViewPage.class,
                new PageParameters("0=" + entry.getId() + ",1=" + PERMISSIONS_URL_KEY));

        permissionLink.setOutputMarkupId(true);
    }

    @SuppressWarnings("unchecked")
    private AjaxLink renderAddToWorkspaceLink() {
        final ResourceReference notInWorkspaceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "plus-empty.png");
        final ResourceReference inWorkspaceImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "plus-filled.png");
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

                Image image = new Image("plusImage", inWorkspaceImage);
                this.replace(image);
                getParent().replace(this);
                target.addComponent(this);
            }
        };
        if (WorkspaceManager.hasEntry(IceSession.get().getAccount(), entry)) {
            addToWorkspaceLink.add(new Image("plusImage", inWorkspaceImage));
        } else {
            addToWorkspaceLink.add(new Image("plusImage", notInWorkspaceImage));
        }
        addToWorkspaceLink.setOutputMarkupId(true);
        return addToWorkspaceLink;

    }
}
