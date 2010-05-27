package org.jbei.ice.web.panels;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;

public class MiniPermissionReadAllowedPanel extends Panel {

    public MiniPermissionReadAllowedPanel(String id, Entry entry, List<String> readAllowed) {
        super(id);

        int listLimit = 4;
        if (readAllowed.size() > listLimit) {
            readAllowed = readAllowed.subList(0, listLimit);
            Panel moreReadableLinkPanel = new MorePermissionLinkPanel("moreReadableLinkPanel",
                    entry);
            add(moreReadableLinkPanel);
        } else {
            add(new EmptyPanel("moreReadableLinkPanel"));
        }
        ListView<String> readableList = new ListView<String>("readableList", readAllowed) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                String itemLabel = item.getModelObject();
                item.add(new Label("readableItem", itemLabel));
            }

        };
        add(readableList);
    }

    private static final long serialVersionUID = 1L;

}
