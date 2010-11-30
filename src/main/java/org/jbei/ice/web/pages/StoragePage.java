package org.jbei.ice.web.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.web.common.ViewException;

public class StoragePage extends ProtectedPage {
    public String storageTitle = "";

    private Storage storage = null;
    private Sample sample = null;

    public StoragePage(PageParameters parameters) {
        super(parameters);

        processPageParameters(parameters);

        add(new Label("storageName", storage.getName() + " "
                + ((storage.getIndex() == null) ? "" : storage.getIndex())));

        Storage parent = storage.getParent();
        BookmarkablePageLink<StoragePage> parentLink = null;
        if (parent != null) {
            parentLink = new BookmarkablePageLink<StoragePage>("parentLink", StoragePage.class,
                    new PageParameters("0=" + parent.getId()));
            parentLink.add(new Label("parentLinkLabel", parent.getName() + " "
                    + ((parent.getIndex() == null) ? "" : parent.getIndex())));
        } else {
            parentLink = new BookmarkablePageLink<StoragePage>("parentLink", StoragePage.class,
                    new PageParameters("0=0"));
            parentLink.add(new Label("parentLinkLabel", ""));
            parentLink.setEnabled(false);
        }
        add(parentLink);

        ArrayList<Storage> siblings = new ArrayList<Storage>();
        if (parent != null) {
            siblings.addAll(parent.getChildren());
        }

        Comparator<Storage> storageComparator = new Comparator<Storage>() {
            @Override
            public int compare(Storage o1, Storage o2) {

                long indexO1 = 0;
                if (o1.getIndex() != null) {
                    indexO1 = Long.valueOf(o1.getIndex());
                }
                long indexO2 = 0;
                if (o2.getIndex() != null) {
                    indexO2 = Long.valueOf(o2.getIndex());
                }
                if (indexO1 < indexO2)
                    return -1;
                else if (indexO1 > indexO2)
                    return 1;
                else
                    return 0;

            }
        };

        Collections.sort(siblings, storageComparator);
        ArrayList<Storage> children = new ArrayList<Storage>();
        children.addAll(storage.getChildren());

        Collections.sort(children, storageComparator);
        if (children.size() == 0) {
            try {
                sample = SampleManager.getSampleByStorage(storage);
            } catch (ManagerException e) {
                throw new ViewException(e);
            }
        }

        ListView<BookmarkablePageLink<ProtectedPage>> childrenListView = makeListView(
            "childrenListView", children, sample);
        ListView<BookmarkablePageLink<ProtectedPage>> siblingListView = makeListView(
            "siblingListView", siblings, null);

        add(siblingListView);
        add(childrenListView);

    }

    protected ListView<BookmarkablePageLink<ProtectedPage>> makeListView(String id,
            List<Storage> items, Sample sample) {
        ArrayList<BookmarkablePageLink<ProtectedPage>> pageLinks = new ArrayList<BookmarkablePageLink<ProtectedPage>>();
        if (sample != null) {
            // add entry view sample link
            BookmarkablePageLink<ProtectedPage> link = new BookmarkablePageLink<ProtectedPage>(
                    "itemLink", EntryViewPage.class, new PageParameters("0="
                            + sample.getEntry().getId() + ",1=" + EntryViewPage.SAMPLES_URL_KEY));
            link.add(new Label("itemLinkLabel", sample.getEntry().getOnePartNumber()
                    .getPartNumber()));
            pageLinks.add(link);
        }
        if (items.size() == 0) {
            // add an empty, disabled link
            BookmarkablePageLink<ProtectedPage> link = new BookmarkablePageLink<ProtectedPage>(
                    "itemLink", StoragePage.class, new PageParameters("0=0"));
            link.add(new Label("itemLinkLabel", ""));
            link.setEnabled(false);
            pageLinks.add(link);

        } else {
            for (Storage storage : items) {
                BookmarkablePageLink<ProtectedPage> link = new BookmarkablePageLink<ProtectedPage>(
                        "itemLink", StoragePage.class, new PageParameters("0=" + storage.getId()));
                link.add(new Label("itemLinkLabel", storage.getName() + " "
                        + ((storage.getIndex() == null) ? "" : storage.getIndex())));
                if (storage.getId() == this.storage.getId()) {
                    link.setEnabled(false);
                }
                pageLinks.add(link);
            }

        }

        ListView<BookmarkablePageLink<ProtectedPage>> view = new ListView<BookmarkablePageLink<ProtectedPage>>(
                id, pageLinks) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<BookmarkablePageLink<ProtectedPage>> item) {
                item.add(item.getModelObject());
            }

        };
        return view;
    }

    @Override
    protected String getTitle() {
        return storageTitle + " - " + super.getTitle();
    }

    private void processPageParameters(PageParameters parameters) {
        if (parameters == null || parameters.size() == 0) {
            throw new ViewException("Parameters are missing!");
        }
        String id = parameters.getString("0");
        try {
            storage = StorageManager.get(Long.valueOf(id));
        } catch (NumberFormatException e) {
            throw new ViewException(e);
        } catch (ManagerException e) {
            throw new ViewException(e);
        }
        if (storage == null) {
            throw new RestartResponseAtInterceptPageException(PermissionDeniedPage.class);
        } else {
            storageTitle = storage.getName() + " "
                    + ((storage.getIndex() == null) ? "" : storage.getIndex());
        }

    }
}
