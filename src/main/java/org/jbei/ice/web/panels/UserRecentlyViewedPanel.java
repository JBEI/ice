package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.data.tables.AbstractSortableColumn;
import org.jbei.ice.web.data.tables.LabelHeaderColumn;
import org.jbei.ice.web.dataProviders.RecentlyViewedDataProvider;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.utils.WebUtils;

public class UserRecentlyViewedPanel extends SortableDataTablePanel<Workspace> {
    private static final long serialVersionUID = 1L;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

    public UserRecentlyViewedPanel(String id) {
        super(id);
        RecentlyViewedDataProvider data = new RecentlyViewedDataProvider(IceSession.get()
                .getAccount());
        dataProvider = data;
        setEntries(data.getEntries());

        addColumns();
        renderTable();
    }

    protected void addColumns() {

        addIndexColumn();
        addStarColumn();
        addTypeColumn();
        addPartIDColumn();
        addNameColumn();
        addSummaryColumn();
        addLastAddedColumn();
        addLastVisitedColumn();
    }

    protected void addLastAddedColumn() {
        addColumn(new LabelHeaderColumn<Workspace>("Last Added", "dateAdded") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Workspace space, int row) {
                String dateString = dateFormat.format(new Date(space.getDateAdded()));
                return new Label(componentId, dateString);
            }
        });
    }

    protected void addLastVisitedColumn() {
        addColumn(new LabelHeaderColumn<Workspace>("Last Visited", "dateVisited") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Workspace space, int row) {
                String dateString = dateFormat.format(new Date(space.getDateVisited()));
                return new Label(componentId, dateString);
            }
        });
    }

    protected void addNameColumn() {
        addLabelHeaderColumn("Name", "entry.oneName.name", "entry.oneName.name");
    }

    protected void addSummaryColumn() {
        addColumn(new LabelHeaderColumn<Workspace>("Summary", null, null) {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Workspace space, int row) {
                String trimmedDescription = trimLongField(
                    WebUtils.linkifyText(space.getEntry().getShortDescription()),
                    MAX_LONG_FIELD_LENGTH);
                return new Label(componentId, trimmedDescription).setEscapeModelStrings(false);
            }
        });
    }

    protected void addPartIDColumn() {
        addColumn(new LabelHeaderColumn<Workspace>("Part ID") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Workspace space, int row) {
                Fragment fragment = new Fragment(componentId, "part_id_cell",
                        UserRecentlyViewedPanel.this);

                BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>(
                        "partIdLink", EntryViewPage.class, new PageParameters("0="
                                + space.getEntry().getId()));

                entryLink.add(new Label("partNumber", space.getEntry().getOnePartNumber()
                        .getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/"
                        + space.getEntry().getId()));
                fragment.add(entryLink);
                return fragment;
            }
        });
    }

    protected void addTypeColumn() {
        addLabelHeaderColumn("Type", "entry.recordType", "entry.recordType");
    }

    protected void addIndexColumn() {
        addColumn(new AbstractSortableColumn<Workspace>(null, null) {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, final Workspace workspace, int index) {
                return new Label(componentId, String.valueOf((table.getCurrentPage() * table
                        .getRowsPerPage()) + index + 1));
            }

            @Override
            public Component getHeader(String componentId) {
                return new Label(componentId, "#");
            }

            @Override
            public void detach() {
            }
        });
    }

    protected void addStarColumn() {
        final ResourceReference starFilledImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "star-filled.png");
        final ResourceReference starEmptyImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "star-empty.png");

        addColumn(new AbstractSortableColumn<Workspace>(null, null) {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, final Workspace workspace, int index) {
                AjaxLink<Object> starLink = new AjaxLink<Object>("starLink") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        Image image = null;
                        if (workspace.isInWorkspace()) {

                            try {
                                workspace.setInWorkspace(false);
                                WorkspaceManager.save(workspace);
                                image = new Image("starImage", starEmptyImage);
                            } catch (ManagerException e) {
                                Logger.error("Could not save star", e);
                            }
                        } else {
                            try {
                                workspace.setInWorkspace(true);
                                WorkspaceManager.save(workspace);
                                image = new Image("starImage", starFilledImage);
                            } catch (ManagerException e) {
                                throw new ViewException("Could not save star", e);
                            }
                        }
                        this.replace(image);
                        getParent().replace(this);
                        target.addComponent(this);
                    }
                };
                if (workspace.isInWorkspace()) {
                    starLink.add(new Image("starImage", starFilledImage));
                } else {
                    starLink.add(new Image("starImage", starEmptyImage));
                }

                Fragment fragment = new Fragment(componentId, "star_image_fragment",
                        UserRecentlyViewedPanel.this);
                fragment.add(starLink);
                return fragment;
            }

            @Override
            public Component getHeader(String componentId) {
                return new Label(componentId, "");
            }

            @Override
            public void detach() {
            }
        });
    }
}
