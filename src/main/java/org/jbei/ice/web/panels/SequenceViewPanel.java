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
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SequenceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.permissions.PermissionManager;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.forms.SequenceNewFormPanel;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.pages.VectorEditorPage;

public class SequenceViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Sequence sequence;
    private Entry entry;

    private String sequenceUser;
    private EmptyPanel emptySequenceFormPanel;

    public SequenceViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        try {
            sequence = SequenceManager.getByEntry(entry);
        } catch (ManagerException e) {
            Logger.error(Utils.stackTraceToString(e));
        } catch (Exception e) {
            Logger.error(Utils.stackTraceToString(e));
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

        fragment.add(new BookmarkablePageLink<VectorEditorPage>(
                "createSequenceViaVectorEditorLink", VectorEditorPage.class, parameters)
                .add(new Label("createSequenceViaVectorEditorLabel",
                        "Create Sequence using VectorEditor")));

        AjaxFallbackLink<Object> uploadSequenceLink = new AjaxFallbackLink<Object>(
                "uploadSequenceLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                SequenceNewFormPanel addNewSequence = new SequenceNewFormPanel("sequenceFormPanel",
                        (SequenceViewPanel) getParent().getParent(), entry);
                addNewSequence.setOutputMarkupId(true);

                getParent().getParent().addOrReplace(addNewSequence);
                target.addComponent(addNewSequence);
            }
        };
        fragment.add(uploadSequenceLink.add(new Label("uploadSequenceLabel", "Upload Sequence")));

        return fragment;
    }

    private Fragment createSequenceFragment() {
        Fragment fragment = new Fragment("sequenceDataPanel", "sequenceFragment", this);

        if (sequence != null) {
            renderDeleteLink(fragment);
            renderGenbankDownloadLink(fragment);
            renderFastaDownloadLink(fragment);
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

    private void renderGenbankDownloadLink(WebMarkupContainer container) {
        Link<Object> downloadLink = new Link<Object>("genbankDownloadLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                IResourceStream resourceStream = new StringResourceStream(sequence
                        .getSequenceUser(), "application/genbank");

                getRequestCycle().setRequestTarget(
                        new ResourceStreamRequestTarget(resourceStream, entry
                                .getPartNumbersAsString()
                                + ".gb"));
            }
        };

        container.add(downloadLink);
    }

    private void renderFastaDownloadLink(WebMarkupContainer container) {
        Link<Object> downloadLink = new Link<Object>("fastaDownloadLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
                IResourceStream resourceStream = new StringResourceStream(sequence
                        .getSequenceUser(), "application/fasta");

                getRequestCycle().setRequestTarget(
                        new ResourceStreamRequestTarget(resourceStream, entry
                                .getPartNumbersAsString()
                                + ".fasta"));
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
                    entry.setSequence(null);

                    SequenceManager.delete(sequence);

                    updateView(null);
                } catch (ManagerException e) {
                    Logger.error(Utils.stackTraceToString(e));
                } catch (Exception e) {
                    Logger.error(Utils.stackTraceToString(e));
                }
            }
        }

        container.add(new DeleteSequenceLink("deleteLink").setVisible(PermissionManager
                .hasWritePermission(entry.getId())));
    }
}
