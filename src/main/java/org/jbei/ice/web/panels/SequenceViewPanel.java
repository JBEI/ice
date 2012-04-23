package org.jbei.ice.web.panels;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.composers.SequenceComposerException;
import org.jbei.ice.lib.composers.formatters.FastaFormatter;
import org.jbei.ice.lib.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.composers.formatters.SbolFormatter;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.forms.SequenceNewFormPanel;
import org.jbei.ice.web.pages.DownloadPage;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.pages.VectorEditorPage;

public class SequenceViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Sequence sequence;
    private final Entry entry;

    private String sequenceUser;
    private EmptyPanel emptySequenceFormPanel;

    public SequenceViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        SequenceController sequenceController = new SequenceController(IceSession.get()
                .getAccount());
        try {
            sequence = sequenceController.getByEntry(entry);
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        initializeControls();

        setOutputMarkupId(true);

        updateStatus(sequence);
    }

    private void initializeControls() {
        emptySequenceFormPanel = new EmptyPanel("sequenceFormPanel");
        emptySequenceFormPanel.setOutputMarkupId(true);

        add(emptySequenceFormPanel);
    }

    private void updateStatus(Sequence sequence) {
        if (sequence == null) {
            addOrReplace(createNoSequenceFragment());
        } else {
            this.sequence = sequence;
            addOrReplace(createSequenceFragment());
            sequenceUser = sequence.getSequenceUser();
        }
    }

    private Fragment createNoSequenceFragment() {
        Fragment fragment = new Fragment("sequenceDataPanel", "noSequenceFragment", this);

        fragment.setOutputMarkupPlaceholderTag(true);
        fragment.setOutputMarkupId(true);

        PageParameters parameters = new PageParameters();
        parameters.add("entryId", entry.getRecordId());

        BookmarkablePageLink<VectorEditorPage> createSequenceViaVectorEditorLink = new BookmarkablePageLink<VectorEditorPage>(
                "createSequenceViaVectorEditorLink", VectorEditorPage.class, parameters);
        createSequenceViaVectorEditorLink.add(new Label("createSequenceViaVectorEditorLabel",
                "Create Sequence using VectorEditor"));

        AjaxFallbackLink<Object> uploadSequenceLink = new AjaxFallbackLink<Object>(
                "uploadSequenceLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                SequenceViewPanel sequenceViewPanel = (SequenceViewPanel) getParent().getParent()
                        .getParent();

                SequenceNewFormPanel addNewSequence = new SequenceNewFormPanel("sequenceFormPanel",
                        sequenceViewPanel, entry);
                addNewSequence.setOutputMarkupId(true);

                sequenceViewPanel.addOrReplace(addNewSequence);
                target.addComponent(addNewSequence);
            }
        };
        uploadSequenceLink.add(new Label("uploadSequenceLabel", "Upload Sequence"));

        WebMarkupContainer sequenceCreateLinkContainer = new WebMarkupContainer(
                "sequenceCreateLinkContainer");

        sequenceCreateLinkContainer.add(uploadSequenceLink);
        sequenceCreateLinkContainer.add(createSequenceViaVectorEditorLink);

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            fragment.add(sequenceCreateLinkContainer.setVisible(entryController
                    .hasWritePermission(entry)));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return fragment;
    }

    private Fragment createSequenceFragment() {
        Fragment fragment = new Fragment("sequenceDataPanel", "sequenceFragment", this);

        if (sequence != null) {
            renderDeleteLink(fragment);
            renderOriginalDownloadLink(fragment);
            renderGenbankDownloadLink(fragment);
            renderFastaDownloadLink(fragment);
            renderSbolDownloadLink(fragment);
            renderVectorEditorLink(fragment);
            renderVectorViewerEmbededObject(fragment);

            fragment.setOutputMarkupPlaceholderTag(true);
            fragment.setOutputMarkupId(true);

            add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "extMouseWheel.js"));
            add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "hookMouseWheel.js"));
        }

        return fragment;
    }

    public String getSequenceUser() {
        return sequenceUser;
    }

    public void clearForm() {
        addOrReplace(emptySequenceFormPanel);
    }

    public void updateView(Sequence sequence) {
        clearForm();

        updateStatus(sequence);
    }

    private void renderVectorEditorLink(WebMarkupContainer container) {
        PageParameters parameters = new PageParameters();
        parameters.add("entryId", entry.getRecordId());

        container.add(new BookmarkablePageLink<VectorEditorPage>("viewInVectorEditorLink",
                VectorEditorPage.class, parameters));
    }

    private void renderVectorViewerEmbededObject(WebMarkupContainer container) {
        WebComponent flashComponent = new WebComponent("vectorViewer");

        ResourceReference veResourceReference = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.VV_RESOURCE_LOCATION + "VectorViewer.swf?entryId="
                        + entry.getRecordId() + "&sessionId=" + IceSession.get().getSessionKey());

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

        container.add(flashComponent);
    }

    private void renderOriginalDownloadLink(WebMarkupContainer container) {
        Link<Object> originalDownloadLink = new Link<Object>("originalDownloadLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                String sequenceString = sequence.getSequenceUser();

                if (sequenceString != null && !sequenceString.isEmpty()) {
                    setResponsePage(new DownloadPage(entry.getPartNumbersAsString() + ".seq",
                            "text/plain", sequenceString));
                }
            }
        };

        WebMarkupContainer originalDownloadLinkContainer = new WebMarkupContainer(
                "originalDownloadLinkContainer");

        originalDownloadLinkContainer.add(originalDownloadLink);

        container.add(originalDownloadLinkContainer);

        if (sequence.getSequenceUser() == null || sequence.getSequenceUser().isEmpty()) {
            originalDownloadLinkContainer.setVisible(false);
        }
    }

    private void renderGenbankDownloadLink(WebMarkupContainer container) {
        Link<Object> downloadLink = new Link<Object>("genbankDownloadLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                String sequenceString = null;
                try {
                    GenbankFormatter genbankFormatter = new GenbankFormatter(sequence.getEntry()
                            .getNamesAsString());
                    genbankFormatter
                            .setCircular((sequence.getEntry() instanceof Plasmid) ? ((Plasmid) sequence
                                    .getEntry()).getCircular() : false);

                    sequenceString = SequenceController.compose(sequence, genbankFormatter);
                } catch (SequenceComposerException e) {
                    throw new ViewException("Failed to generate genbank file for download!", e);
                }

                setResponsePage(new DownloadPage(entry.getPartNumbersAsString() + ".gb",
                        "text/plain", sequenceString));
            }
        };

        container.add(downloadLink);
    }

    private void renderFastaDownloadLink(WebMarkupContainer container) {
        Link<Object> downloadLink = new Link<Object>("fastaDownloadLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                String sequenceString = null;
                try {
                    sequenceString = SequenceController.compose(sequence, new FastaFormatter(
                            sequence.getEntry().getNamesAsString()));
                } catch (SequenceComposerException e) {
                    throw new ViewException("Failed to generate fasta file for download!", e);
                }

                setResponsePage(new DownloadPage(entry.getPartNumbersAsString() + ".fasta",
                        "text/plain", sequenceString));
            }
        };

        container.add(downloadLink);
    }

    private void renderSbolDownloadLink(WebMarkupContainer container) {
        Link<Object> downloadLink = new Link<Object>("sbolDownloadLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                String sequenceString = null;
                try {
                    sequenceString = SequenceController.compose(sequence, new SbolFormatter());
                } catch (SequenceComposerException e) {
                    throw new ViewException("Failed to generate fasta file for download!", e);
                }

                setResponsePage(new DownloadPage(entry.getPartNumbersAsString() + ".xml",
                        "text/xml", sequenceString));
            }
        };

        container.add(downloadLink);
    }

    private void renderDeleteLink(WebMarkupContainer container) {
        class DeleteSequenceLink extends AjaxFallbackLink<Object> {
            private static final long serialVersionUID = 1L;

            public DeleteSequenceLink(String id) {
                super(id);
                this.add(new SimpleAttributeModifier("onclick",
                        "return confirm('Delete this sequence?');"));
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    SequenceController sequenceController = new SequenceController(IceSession.get()
                            .getAccount());

                    sequenceController.delete(sequence);

                    updateView(null);
                } catch (ControllerException e) {
                    throw new ViewException(e);
                } catch (PermissionException e) {
                    throw new ViewPermissionException("No permission to delete sequence!", e);
                }
            }
        }

        EntryController entryController = new EntryController(IceSession.get().getAccount());

        try {
            container.add(new DeleteSequenceLink("deleteLink").setVisible(entryController
                    .hasWritePermission(entry)));
        } catch (ControllerException e) {
            throw new ViewException(e);
        }
    }
}
