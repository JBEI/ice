package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.List;

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
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.SequenceAnalysisController;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.common.ViewPermissionException;
import org.jbei.ice.web.forms.TraceFileNewFormPanel;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.pages.UnprotectedPage;

public class SequenceAnalysisViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    private Entry entry;
    private EmptyPanel traceSequenceFormPanel;

    public SequenceAnalysisViewPanel(String id, Entry entry) {
        super(id);

        this.entry = entry;

        renderAddTraceFileLink();
        renderTraceSequenceFormPanel();
        renderTracesListView();
    }

    public void removeForm() {
        addOrReplace(traceSequenceFormPanel);
    }

    private void renderTraceSequenceFormPanel() {
        traceSequenceFormPanel = new EmptyPanel("traceSequenceFormPanel");
        traceSequenceFormPanel.setOutputMarkupId(true);

        add(traceSequenceFormPanel);
    }

    private void renderAddTraceFileLink() {
        add(new AjaxFallbackLink<Object>("addTraceFileLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                TraceFileNewFormPanel addTraceFileNewFormPanel = new TraceFileNewFormPanel(
                        "traceSequenceFormPanel", (SequenceAnalysisViewPanel) getParent(), entry);
                addTraceFileNewFormPanel.setOutputMarkupId(true);

                getParent().addOrReplace(addTraceFileNewFormPanel);
                target.addComponent(addTraceFileNewFormPanel);
            }
        });
    }

    private WebComponent renderSequenceCheckerEmbededObject() {
        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "extMouseWheel.js"));
        add(JavascriptPackageResource.getHeaderContribution(UnprotectedPage.class,
                UnprotectedPage.JS_RESOURCE_LOCATION + "hookMouseWheel.js"));

        WebComponent flashComponent = new WebComponent("sequenceChecker");

        ResourceReference veResourceReference = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.SC_RESOURCE_LOCATION + "SequenceChecker.swf?entryId="
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

        return flashComponent;
    }

    private void renderTracesListView() {
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                IceSession.get().getAccount());

        List<TraceSequence> traces = null;
        try {
            traces = sequenceAnalysisController.getTraceSequences(entry);
        } catch (ControllerException e) {
            throw new ViewException("Failed to fetch TraceSequence by Entry on SeqAnalysis view", e);
        }

        ListView<TraceSequence> tracesListView = new ListView<TraceSequence>("tracesListView",
                traces) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<TraceSequence> item) {
                item.add(new SimpleAttributeModifier("class", item.getIndex() % 2 == 0 ? "odd_row"
                        : "even_row"));
                item.add(new Label("index", String.valueOf(item.getIndex() + 1)));

                renderNameDownloadLink(item);
                renderDepositorLink(item);
                renderDeleteLink(item);
                renderCreationTime(item);
            }

            @SuppressWarnings("unchecked")
            private void renderNameDownloadLink(final ListItem<TraceSequence> item) {
                TraceSequence traceSequence = item.getModelObject();

                SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                        IceSession.get().getAccount());

                Link nameDownloadLink;

                try {
                    nameDownloadLink = new DownloadLink("nameDownloadLink",
                            sequenceAnalysisController.getFile(traceSequence), traceSequence
                                    .getFilename());
                } catch (ControllerException e) {
                    nameDownloadLink = new Link("nameDownloadLink") {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void onClick() {
                        }
                    };

                    nameDownloadLink.setEnabled(false);
                }

                item.add(nameDownloadLink.add(new Label("traceName", traceSequence.getFilename())));
            }

            private void renderDepositorLink(ListItem<TraceSequence> item) {
                TraceSequence traceSequence = item.getModelObject();

                Account depositorAccount = null;

                try {
                    depositorAccount = AccountController.getByEmail(traceSequence.getDepositor());
                } catch (ControllerException e) {
                    throw new ViewException(e);
                }

                BookmarkablePageLink<ProfilePage> depositorProfileLink = new BookmarkablePageLink<ProfilePage>(
                        "depositorProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                                + traceSequence.getDepositor()));
                depositorProfileLink.add(new Label("depositor",
                        (depositorAccount != null) ? depositorAccount.getFullName() : traceSequence
                                .getDepositor()));

                if (depositorAccount != null) {
                    String depositorAltText = "Profile " + depositorAccount.getFullName();
                    depositorProfileLink
                            .add(new SimpleAttributeModifier("title", depositorAltText));
                    depositorProfileLink.add(new SimpleAttributeModifier("alt", depositorAltText));
                } else {
                    depositorProfileLink.setEnabled(false);
                }

                item.add(depositorProfileLink);
            }

            private void renderCreationTime(ListItem<TraceSequence> item) {
                TraceSequence traceSequence = item.getModelObject();

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(traceSequence.getCreationTime());

                item.add(new Label("created", dateString));
            }

            private void renderDeleteLink(final ListItem<TraceSequence> item) {
                class DeleteSequenceLink extends AjaxFallbackLink<Object> {
                    private static final long serialVersionUID = 1L;

                    public DeleteSequenceLink(String id) {
                        super(id);

                        this.add(new SimpleAttributeModifier("onclick",
                                "return confirm('Delete this trace file?');"));
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        try {
                            TraceSequence traceSequence = item.getModelObject();

                            SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                                    IceSession.get().getAccount());

                            sequenceAnalysisController.removeTraceSequence(traceSequence);

                            setResponsePage(EntryViewPage.class, new PageParameters("0="
                                    + entry.getId() + ",1=seqanalysis"));
                        } catch (ControllerException e) {
                            throw new ViewException(e);
                        } catch (PermissionException e) {
                            throw new ViewPermissionException(
                                    "No permissions to remove trace sequence!", e);
                        }
                    }
                }

                SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController(
                        IceSession.get().getAccount());

                item.add(new DeleteSequenceLink("deleteLink").setVisible(sequenceAnalysisController
                        .hasWritePermission(item.getModelObject())));
            }
        };

        WebMarkupContainer tracesContainer = new WebMarkupContainer("tracesContainer");
        tracesContainer.setOutputMarkupId(true);
        tracesContainer.setOutputMarkupPlaceholderTag(true);
        tracesContainer.setVisible(traces.size() != 0);

        WebMarkupContainer sequenceCheckerContainer = new WebMarkupContainer(
                "sequenceCheckerContainer");
        tracesContainer.setOutputMarkupId(true);
        tracesContainer.setOutputMarkupPlaceholderTag(true);
        tracesContainer.setVisible(traces.size() != 0);

        // hide sequence checker if sequence doesn't exist
        SequenceController sequenceController = new SequenceController(IceSession.get()
                .getAccount());

        WebComponent flashComponent = renderSequenceCheckerEmbededObject();
        sequenceCheckerContainer.add(flashComponent);
        try {
            if (sequenceController.getByEntry(entry) == null) {
                sequenceCheckerContainer.setVisible(false);
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        add(tracesContainer);
        add(sequenceCheckerContainer);

        WebMarkupContainer emptyTracesContainer = new WebMarkupContainer("emptyTracesContainer");
        emptyTracesContainer.setOutputMarkupId(true);
        emptyTracesContainer.setVisible(traces.size() == 0);
        add(emptyTracesContainer);

        tracesContainer.add(tracesListView);
    }
}
