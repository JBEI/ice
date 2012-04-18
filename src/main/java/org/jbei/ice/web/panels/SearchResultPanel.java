package org.jbei.ice.web.panels;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.lucene.SearchResult;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.data.tables.ImageHeaderColumn;
import org.jbei.ice.web.data.tables.LabelHeaderColumn;
import org.jbei.ice.web.dataProviders.SearchDataProvider;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.ProfilePage;
import org.jbei.ice.web.utils.WebUtils;

/**
 * Panel for displaying a table of {@link org.jbei.ice.lib.search.lucene.SearchResult}s
 * 
 * @author Hector Plahar
 */
public class SearchResultPanel extends SortableDataTablePanel<SearchResult> {
    private static final long serialVersionUID = 2L;
    private static final int MAX_LONG_FIELD_LENGTH = 100;

    protected void addScoreColumn() {
        addColumn(new LabelHeaderColumn<SearchResult>("Score", "score") {
            private static final long serialVersionUID = 1L;

            @Override
            public Component evaluate(String componentId, SearchResult result, int row) {
                NumberFormat formatter = new DecimalFormat("##");
                String scoreString = formatter.format(result.getScore() * 100);
                return new Label(componentId, scoreString);
            }
        });
    }

    protected void addTypeColumn() {
        addColumn(new LabelHeaderColumn<SearchResult>("Type", "entry.recordType",
                "entry.recordType"));
    }

    protected void addPartIDColumn() {
        addColumn(new LabelHeaderColumn<SearchResult>("Part ID", "entry.onePartNumber.partNumber") {

            private static final long serialVersionUID = 1L;

            @Override
            public Component evaluate(String componentId, SearchResult result, int row) {

                Entry entry = result.getEntry();
                Fragment fragment = new Fragment(componentId, "part_id_cell",
                        SearchResultPanel.this);

                BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>(
                        "partIdLink", EntryViewPage.class, new PageParameters("0=" + entry.getId()));

                entryLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
                fragment.add(entryLink);
                return fragment;
            }
        });
    }

    protected void addNameColumn() {
        addColumn(new LabelHeaderColumn<SearchResult>("Name", "entry.oneName.name",
                "entry.oneName.name"));
    }

    protected void addSummaryColumn() {
        addColumn(new LabelHeaderColumn<SearchResult>("Summary") {
            private static final long serialVersionUID = 1L;

            @Override
            public Component evaluate(String componentId, SearchResult result, int row) {
                String value = result.getEntry().getShortDescription();
                if (value == null || value.isEmpty()) {
                    value = "";
                }
                if (value.length() > MAX_LONG_FIELD_LENGTH) {
                    value = value.substring(0, MAX_LONG_FIELD_LENGTH) + "...";
                }

                return new Label(componentId, WebUtils.linkifyText(IceSession.get().getAccount(),
                    value)).setEscapeModelStrings(false);
            }
        });
    }

    protected void addOwnerColumn() {
        addColumn(new LabelHeaderColumn<SearchResult>("Owner") {

            private static final long serialVersionUID = 1L;

            public Component evaluate(String id, SearchResult result, int row) {

                Fragment fragment = new Fragment(id, "owner_fragment", SearchResultPanel.this);
                Entry entry = result.getEntry();
                Account ownerAccount = null;

                try {
                    ownerAccount = AccountController.getByEmail(entry.getOwnerEmail());
                } catch (ControllerException e) {
                    throw new ViewException(e);
                }

                BookmarkablePageLink<ProfilePage> ownerProfileLink = new BookmarkablePageLink<ProfilePage>(
                        "ownerProfileLink", ProfilePage.class, new PageParameters("0=about,1="
                                + entry.getOwnerEmail()));
                ownerProfileLink.add(new Label("owner", (ownerAccount != null) ? ownerAccount
                        .getFullName() : entry.getOwner()));

                if (ownerAccount != null) {
                    String ownerAltText = "Profile " + ownerAccount.getFullName();
                    ownerProfileLink.add(new SimpleAttributeModifier("title", ownerAltText));
                    ownerProfileLink.add(new SimpleAttributeModifier("alt", ownerAltText));
                } else {
                    ownerProfileLink.setEnabled(false);
                }

                fragment.add(ownerProfileLink);
                return fragment;
            }
        });
    }

    protected void addStatusColumn() {
        addColumn(new LabelHeaderColumn<SearchResult>("Status", "entry.status") {

            private static final long serialVersionUID = 1L;

            @Override
            public Component evaluate(String id, SearchResult result, int row) {
                return new Label(id, JbeiConstants.getStatus(result.getEntry().getStatus()));
            }
        });
    }

    protected void addHasAttachmentColumn() {
        addColumn(new ImageHeaderColumn<SearchResult>("has_attachment_fragment", "has_attachment",
                "attachment.gif", null, "Has Attachment", this) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component evaluate(String id, SearchResult result, int row) {
                Fragment fragment = getHeader(id);
                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    if (!entryController.hasAttachments(result.getEntry()))
                        fragment.replace(new Image("has_attachment", getBlankImage()));
                } catch (ControllerException e) {
                    throw new ViewException(e);
                }
                return fragment;
            }
        });
    }

    protected void addHasSamplesColumn() {
        addColumn(new ImageHeaderColumn<SearchResult>("has_sample_fragment", "has_sample",
                "sample.png", null, "Has Samples", this) {

            private static final long serialVersionUID = 1L;

            @Override
            public Component evaluate(String id, SearchResult result, int row) {
                Fragment fragment = getHeader(id);
                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    if (!entryController.hasSamples(result.getEntry()))
                        fragment.replace(new Image("has_sample", getBlankImage()));
                } catch (ControllerException e) {
                    throw new ViewException(e);
                }
                return fragment;
            }

        });
    }

    protected void addHasSequenceColumn() {
        addColumn(new ImageHeaderColumn<SearchResult>("has_sequence_fragment", "has_sequence",
                "sequence.gif", null, "Has Sequence", this) {

            private static final long serialVersionUID = 1L;

            @Override
            public Component evaluate(String id, SearchResult result, int row) {
                Fragment fragment = getHeader(id);
                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    if (!entryController.hasSequence(result.getEntry()))
                        fragment.replace(new Image("has_sequence", getBlankImage()));
                } catch (ControllerException e) {
                    throw new ViewException(e);
                }
                return fragment;
            }

        });
    }

    protected void addCreatedColumn() {
        addColumn(new LabelHeaderColumn<SearchResult>("Created", "entry.creationTime") {
            private static final long serialVersionUID = 1L;

            @Override
            public Component evaluate(String id, SearchResult result, int row) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(result.getEntry().getCreationTime());
                return new Label(id, dateString);
            }
        });
    }

    public SearchResultPanel(String id, ArrayList<SearchResult> searchResults, int limit) {
        super(id);

        SearchDataProvider provider = new SearchDataProvider(searchResults);
        dataProvider = provider;

        // table columns
        addScoreColumn();
        addTypeColumn();
        addPartIDColumn();
        addNameColumn();
        addSummaryColumn();
        addOwnerColumn();
        addStatusColumn();
        addHasAttachmentColumn();
        addHasSamplesColumn();
        addHasSequenceColumn();
        addCreatedColumn();

        setEntries(provider.getEntries());

        renderTable();
    }
}
