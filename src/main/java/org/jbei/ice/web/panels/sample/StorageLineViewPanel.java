package org.jbei.ice.web.panels.sample;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.web.pages.StoragePage;

public class StorageLineViewPanel extends Panel {
    private static final long serialVersionUID = 1L;

    public StorageLineViewPanel(String id, Storage storage) {
        super(id);

        if (storage == null) {
            return;
        }
        List<Storage> storages = StorageManager.getStoragesUptoScheme(storage);
        Storage scheme = StorageManager.getSchemeContainingParentStorage(storage);
        ArrayList<BookmarkablePageLink<StoragePage>> storageLinks = new ArrayList<BookmarkablePageLink<StoragePage>>();
        ListView<BookmarkablePageLink<StoragePage>> listView = null;

        for (Storage item : storages) {
            BookmarkablePageLink<StoragePage> itemLink = new BookmarkablePageLink<StoragePage>(
                    "storageLink", StoragePage.class, new PageParameters("0=" + item.getId()));

            itemLink.add(new Label("storageLinkLabel", item.toString()));
            storageLinks.add(itemLink);
            listView = new ListView<BookmarkablePageLink<StoragePage>>("storageList", storageLinks) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(ListItem<BookmarkablePageLink<StoragePage>> item) {
                    item.add(item.getModelObject());
                    item.add(new Label("separator", "→"));
                }
            };
        }

        add(listView);

        BookmarkablePageLink<StoragePage> schemeLink = new BookmarkablePageLink<StoragePage>(
                "schemeLink", StoragePage.class, new PageParameters("0=" + scheme.getId()));

        if (scheme != null) {
            schemeLink.add(new Label("schemeLinkLabel", scheme.toString()));
        } else {
            schemeLink.add(new Label("schemeLinkLabel", ""));
        }
        add(schemeLink);
    }
}
