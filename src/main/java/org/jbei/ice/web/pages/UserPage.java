package org.jbei.ice.web.pages;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.JbeiConstants;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.data.tables.AbstractEntryColumn;
import org.jbei.ice.web.data.tables.ImageHeaderEntryColumn;
import org.jbei.ice.web.data.tables.LabelHeaderEntryColumn;
import org.jbei.ice.web.dataProviders.UserEntriesDataProvider;
import org.jbei.ice.web.panels.EmptyWorkspaceMessagePanel;
import org.jbei.ice.web.panels.EntryDataTablePanel;
import org.jbei.ice.web.panels.UserProjectsViewPanel;
import org.jbei.ice.web.panels.UserRecentlyViewedPanel;
import org.jbei.ice.web.panels.UserSamplesViewPanel;
import org.jbei.ice.web.panels.WorkspaceTablePanel;
import org.jbei.ice.web.utils.WebUtils;

public class UserPage extends ProtectedPage {
    private static final int MAX_LONG_FIELD_LENGTH = 100;

    public Component currentPanel;
    public Component entriesPanel;
    public Component samplesPanel;
    public Component workspacePanel;
    public Component recentlyViewedPanel;
    public Component projectsPanel;

    public BookmarkablePageLink<Object> entriesLink;
    public BookmarkablePageLink<Object> samplesLink;
    public BookmarkablePageLink<Object> workspaceLink;
    public BookmarkablePageLink<Object> recentlyViewedLink;
    public BookmarkablePageLink<Object> projectsLink;

    private ResourceReference blankImage = new ResourceReference(UnprotectedPage.class,
            UnprotectedPage.IMAGES_RESOURCE_LOCATION + "blank.png");
    private ResourceReference hasAttachmentImage = new ResourceReference(UnprotectedPage.class,
            UnprotectedPage.IMAGES_RESOURCE_LOCATION + "attachment.gif");
    private ResourceReference hasSequenceImage = new ResourceReference(UnprotectedPage.class,
            UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sequence.gif");
    private ResourceReference hasSampleImage = new ResourceReference(UnprotectedPage.class,
            UnprotectedPage.IMAGES_RESOURCE_LOCATION + "sample.png");

    public String currentPage = null;

    public UserPage(PageParameters parameters) {
        super(parameters);

        initialize(parameters);
    }

    private void initialize(PageParameters parameters) {
        if (parameters == null || parameters.size() == 0) {
            currentPage = null;
        } else {
            currentPage = parameters.getString("0");
        }

        entriesLink = new BookmarkablePageLink<Object>("entriesLink", UserPage.class,
                new PageParameters("0=entries"));
        entriesLink.setOutputMarkupId(true);
        samplesLink = new BookmarkablePageLink<Object>("samplesLink", UserPage.class,
                new PageParameters("0=samples"));
        samplesLink.setOutputMarkupId(true);
        workspaceLink = new BookmarkablePageLink<Object>("workspaceLink", UserPage.class,
                new PageParameters("0=workspace"));
        workspaceLink.setOutputMarkupId(true);
        recentlyViewedLink = new BookmarkablePageLink<Object>("recentlyViewedLink", UserPage.class,
                new PageParameters("0=recent"));
        recentlyViewedLink.setOutputMarkupId(true);
        projectsLink = new BookmarkablePageLink<Object>("projectsLink", UserPage.class,
                new PageParameters("0=projects"));
        projectsLink.setOutputMarkupId(true);

        projectsLink.setVisible(false); // TODO: Comment this to see projects tab

        updateTab();

        add(entriesLink);
        add(samplesLink);
        add(workspaceLink);
        add(recentlyViewedLink);
        add(projectsLink);

        if (currentPage != null && currentPage.equals("samples")) {
            currentPanel = createSamplesPanel();
        } else if (currentPage != null && currentPage.equals("workspace")) {
            currentPanel = createWorkspacePanel();
        } else if (currentPage != null && currentPage.equals("recent")) {
            currentPanel = createRecentlyViewedPanel();
        } else if (currentPage != null && currentPage.equals("projects")) {
            currentPanel = createProjectsPanel();
        } else {
            currentPanel = createEntriesPanel();
        }

        add(currentPanel);
    }

    private void updateTab() {
        entriesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        samplesLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        workspaceLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);
        recentlyViewedLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(
            true);
        projectsLink.add(new SimpleAttributeModifier("class", "inactive")).setOutputMarkupId(true);

        if (currentPage != null && currentPage.equals("samples")) {
            samplesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        } else if (currentPage != null && currentPage.equals("workspace")) {
            workspaceLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(
                true);
        } else if (currentPage != null && currentPage.equals("recent")) {
            recentlyViewedLink.add(new SimpleAttributeModifier("class", "active"))
                    .setOutputMarkupId(true);
        } else if (currentPage != null && currentPage.equals("projects")) {
            projectsLink.add(new SimpleAttributeModifier("class", "active"))
                    .setOutputMarkupId(true);
        } else {
            entriesLink.add(new SimpleAttributeModifier("class", "active")).setOutputMarkupId(true);
        }
    }

    private Panel createEntriesPanel() {

        UserEntriesDataProvider provider = new UserEntriesDataProvider(IceSession.get()
                .getAccount());

        List<AbstractEntryColumn> columns = new LinkedList<AbstractEntryColumn>();

        // columns for the user entries panel
        columns.add(new LabelHeaderEntryColumn("Type", "recordType", "recordType"));
        columns.add(new LabelHeaderEntryColumn("Part ID", null, "id") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Entry entry) {
                Fragment fragment = new Fragment(componentId, "part_id_cell", UserPage.this);

                BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>(
                        "partIdLink", EntryViewPage.class, new PageParameters("0=" + entry.getId()));

                entryLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
                String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
                entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
                fragment.add(entryLink);
                return fragment;
            }
        });
        columns.add(new LabelHeaderEntryColumn("Name", "oneName.name", "oneName.name"));
        columns.add(new LabelHeaderEntryColumn("Summary", null, null) {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Entry entry) {
                String trimmedDescription = trimLongField(
                    WebUtils.linkifyText(entry.getShortDescription()), MAX_LONG_FIELD_LENGTH);
                return new Label(componentId, trimmedDescription).setEscapeModelStrings(false);
            }
        });
        columns.add(new LabelHeaderEntryColumn("Status", null, "status") {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Entry entry) {
                return new Label(componentId, JbeiConstants.getStatus(entry.getStatus()));
            }
        });

        columns.add(new ImageHeaderEntryColumn("has_attachment_fragment", "has_attachment",
                "attachment.gif", null, this) {
            private static final long serialVersionUID = 1L;

            protected Component evaluate(String id, Entry entry) {

                EntryController entryController = new EntryController(IceSession.get().getAccount());
                Fragment fragment = new Fragment(id, "has_attachment_fragment", UserPage.this);

                try {
                    if (entryController.hasAttachments(entry))
                        fragment.add(new Image("has_attachment", hasAttachmentImage));
                    else
                        fragment.add(new Image("has_attachment", blankImage));
                } catch (ControllerException e) {
                    fragment.add(new Image("has_attachment", blankImage));
                }
                return fragment;
            }
        });
        columns.add(new ImageHeaderEntryColumn("has_sample_fragment", "has_sample", "sample.png",
                null, this) {
            private static final long serialVersionUID = 1L;

            protected Component evaluate(String id, Entry entry) {
                Fragment fragment = new Fragment(id, "has_sample_fragment", UserPage.this);

                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    if (entryController.hasAttachments(entry))
                        fragment.add(new Image("has_sample", hasSampleImage));
                    else
                        fragment.add(new Image("has_sample", blankImage));
                } catch (ControllerException e) {
                    fragment.add(new Image("has_sample", blankImage));
                }
                return fragment;
            }
        });
        columns.add(new ImageHeaderEntryColumn("has_sequence_fragment", "has_sequence",
                "sequence.gif", null, this) {
            private static final long serialVersionUID = 1L;

            protected Component evaluate(String id, Entry entry) {
                Fragment fragment = new Fragment(id, "has_sequence_fragment", UserPage.this);

                EntryController entryController = new EntryController(IceSession.get().getAccount());
                try {
                    if (entryController.hasAttachments(entry))
                        fragment.add(new Image("has_sequence", hasSequenceImage));
                    else
                        fragment.add(new Image("has_sequence", blankImage));
                } catch (ControllerException e) {
                    fragment.add(new Image("has_sequence", blankImage));
                }
                return fragment;
            }
        });

        columns.add(new LabelHeaderEntryColumn("Created", null, "creationTime") {
            private static final long serialVersionUID = 1L;

            @Override
            protected Component evaluate(String componentId, Entry entry) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
                String dateString = dateFormat.format(entry.getCreationTime());
                return new Label(componentId, dateString);
            }
        });

        EntryDataTablePanel<Entry> panel = new EntryDataTablePanel<Entry>("centerPanel", provider,
                columns, true);
        panel.setOutputMarkupId(true);
        return panel;
    }

    protected String trimLongField(String value, int maxLength) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (value.length() > maxLength) {
            return value.substring(0, maxLength) + "...";
        } else {
            return value;
        }
    }

    private Panel createSamplesPanel() {
        UserSamplesViewPanel userSamplesViewPanel = new UserSamplesViewPanel("centerPanel");

        userSamplesViewPanel.setOutputMarkupId(true);

        return userSamplesViewPanel;
    }

    private Panel createWorkspacePanel() {
        long workspaces = 0;
        try {
            workspaces = WorkspaceManager.getCountByAccount(IceSession.get().getAccount());
        } catch (ManagerException e) {
            throw new ViewException(e);
        }

        Panel workspacePanel = null;
        if (workspaces > 0) {

            workspacePanel = new WorkspaceTablePanel("centerPanel");
            workspacePanel.setOutputMarkupId(true);
        } else {
            workspacePanel = new EmptyWorkspaceMessagePanel("centerPanel");
        }
        return workspacePanel;
    }

    private Panel createProjectsPanel() {
        UserProjectsViewPanel userProjectsViewPanel = new UserProjectsViewPanel("centerPanel");

        userProjectsViewPanel.setOutputMarkupId(true);

        return userProjectsViewPanel;
    }

    private Panel createRecentlyViewedPanel() {
        UserRecentlyViewedPanel userRecentlyViewedPanel = new UserRecentlyViewedPanel("centerPanel");
        userRecentlyViewedPanel.setOutputMarkupId(true);
        return userRecentlyViewedPanel;
    }

    @Override
    protected String getTitle() {
        return "My Entries - " + super.getTitle();
    }
}
