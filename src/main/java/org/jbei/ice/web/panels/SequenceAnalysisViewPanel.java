package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.TraceSequenceManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.TraceSequence;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.forms.TraceFileNewFormPanel;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.ProfilePage;

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

    private void renderTracesListView() {
        ArrayList<TraceSequence> traces = new ArrayList<TraceSequence>();

        LinkedHashSet<TraceSequence> tracesSet = null;
        try {
            tracesSet = TraceSequenceManager.getByEntry(entry);
        } catch (ManagerException e) {
            Logger.error("Failed to fetch TraceSequence by Entry on SeqAnalysis view", e);
        }

        if (tracesSet != null) {
            for (TraceSequence traceSequence : tracesSet) {
                traces.add(traceSequence);
            }
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
                renderAlignmentScore(item);
                renderDeleteLink(item);
                renderCreationTime(item);
            }

            private void renderNameDownloadLink(final ListItem<TraceSequence> item) {
                TraceSequence traceSequence = item.getModelObject();

                Link<Object> nameDownloadLink = new Link<Object>("nameDownloadLink") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick() {
                        TraceSequence traceSequence = item.getModelObject();

                        String result = traceSequence.getSequenceUser();

                        IResourceStream resourceStream = new StringResourceStream(result,
                                "application/trace");

                        getRequestCycle().setRequestTarget(
                                new ResourceStreamRequestTarget(resourceStream, traceSequence
                                        .getName()));
                    }
                };

                item.add(nameDownloadLink.add(new Label("traceName", traceSequence.getName())));
            }

            private void renderDepositorLink(ListItem<TraceSequence> item) {
                TraceSequence traceSequence = item.getModelObject();

                Account depositorAccount = null;

                try {
                    depositorAccount = AccountManager.getByEmail(traceSequence.getDepositor());
                } catch (ManagerException e) {
                    e.printStackTrace();
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

            private void renderAlignmentScore(ListItem<TraceSequence> item) {
                TraceSequence traceSequence = item.getModelObject();

                String alignmentScore = "-";
                if (traceSequence.getAlignment() != null) {
                    alignmentScore = "Score: "
                            + String.valueOf(traceSequence.getAlignment().getScore());
                }

                item.add(new Label("alignmentScore", alignmentScore));
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

                            TraceSequenceManager.delete(traceSequence);

                            setResponsePage(EntryViewPage.class, new PageParameters("0="
                                    + entry.getId() + ",1=seqanalysis"));
                        } catch (ManagerException e) {
                            Logger.error("Could't delete trace sequence for entry", e);
                        } catch (Exception e) {
                            Logger.error("Could't delete trace sequence for entry", e);
                        }
                    }
                }

                item.add(new DeleteSequenceLink("deleteLink").setVisible(item.getModelObject()
                        .getDepositor().equals(IceSession.get().getAccount().getEmail())));
            }
        };

        WebMarkupContainer tracesContainer = new WebMarkupContainer("tracesContainer");
        tracesContainer.setOutputMarkupId(true);
        tracesContainer.setOutputMarkupPlaceholderTag(true);
        tracesContainer.setVisible(traces.size() != 0);

        add(tracesContainer);
        tracesContainer.add(tracesListView);
    }
}
