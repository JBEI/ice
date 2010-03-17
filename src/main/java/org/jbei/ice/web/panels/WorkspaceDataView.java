package org.jbei.ice.web.panels;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.WorkspaceManager;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Workspace;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.EntryTipPage;
import org.jbei.ice.web.pages.EntryViewPage;
import org.jbei.ice.web.pages.UnprotectedPage;
import org.jbei.ice.web.pages.UserPage;
import org.jbei.ice.web.utils.WebUtils;

public class WorkspaceDataView extends DataView<Workspace> {

    protected WorkspaceDataView(String id, IDataProvider<Workspace> dataProvider, int itemsPerPage) {
        super(id, dataProvider, itemsPerPage);
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected void populateItem(Item<Workspace> item) {
        item.add(new SimpleAttributeModifier("class", item.getIndex() % 2 == 0 ? "odd_row"
                : "even_row"));
        final Workspace workspace = getWorkspace(item);
        final Entry entry = workspace.getEntry();

        item.add(new Label("index", String.valueOf(getItemsPerPage() * getCurrentPage()
                + item.getIndex() + 1)));
        item.add(new Label("recordType", entry.getRecordType()));

        final ResourceReference starFilledImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "star-filled.png");
        final ResourceReference starEmptyImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "star-empty.png");

        renderStarLink(item, workspace, starFilledImage, starEmptyImage);

        // renderEntryLink 
        BookmarkablePageLink<String> entryLink = new BookmarkablePageLink<String>("partIdLink",
                EntryViewPage.class, new PageParameters("0=" + entry.getId()));
        entryLink.add(new Label("partNumber", entry.getOnePartNumber().getPartNumber()));
        String tipUrl = (String) urlFor(EntryTipPage.class, new PageParameters());
        entryLink.add(new SimpleAttributeModifier("rel", tipUrl + "/" + entry.getId()));
        item.add(entryLink);

        item.add(new Label("name", entry.getOneName().getName()));
        item.add(new Label("description", WebUtils.jbeiLinkifyText(entry.getShortDescription()))
                .setEscapeModelStrings(false));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        String dateString = dateFormat.format(new Date(workspace.getDateAdded()));
        item.add(new Label("dateAdded", dateString));
        dateString = dateFormat.format(new Date(workspace.getDateVisited()));
        item.add(new Label("dateVisited", dateString));

        // renderDeleteLink
        final ResourceReference xFilledImage = new ResourceReference(UnprotectedPage.class,
                UnprotectedPage.IMAGES_RESOURCE_LOCATION + "x-filled.png");
        AjaxLink<Object> removeLink = new AjaxLink<Object>("removeLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    workspace.setInWorkspace(false);
                    WorkspaceManager.save(workspace);
                    setResponsePage(UserPage.class, new PageParameters("0=" + "workspace"));
                } catch (ManagerException e) {
                    throw new ViewException("Could not remove from workspace", e);
                }
            }

        };
        removeLink.add(new Image("removeImage", xFilledImage));
        item.add(removeLink);
    }

    @SuppressWarnings("unchecked")
    private void renderStarLink(Item<Workspace> item, final Workspace workspace,
            final ResourceReference starFilledImage, final ResourceReference starEmptyImage) {
        AjaxLink starLink = new AjaxLink("starLink") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                Image image = null;
                if (workspace.isStarred()) {

                    try {
                        workspace.setStarred(false);
                        WorkspaceManager.save(workspace);
                        image = new Image("starImage", starEmptyImage);
                    } catch (ManagerException e) {
                        Logger.error("Could not save star", e);
                    }
                } else {
                    try {
                        workspace.setStarred(true);
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
        if (workspace.isStarred()) {
            starLink.add(new Image("starImage", starFilledImage));
        } else {
            starLink.add(new Image("starImage", starEmptyImage));
        }
        item.add(starLink);
    }

    protected Workspace getWorkspace(Item<Workspace> item) {
        return item.getModelObject();
    }

}
