package org.jbei.ice.web.panels;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.jbei.ice.lib.models.Entry;

public class MiniPermissionWriteAllowedPanel extends Panel {

    public MiniPermissionWriteAllowedPanel(String id, Entry entry, List<String> writeAllowed) {
        super(id);

        int listLimit = 4;
        if (writeAllowed.size() > listLimit) {
            writeAllowed = writeAllowed.subList(0, listLimit);
            Panel moreReadableLinkPanel = new MorePermissionLinkPanel("moreWritableLinkPanel",
                    entry);
            add(moreReadableLinkPanel);
        } else {
            add(new EmptyPanel("moreWritableLinkPanel"));
        }
        ListView<String> writableList = new ListView<String>("writableList", writeAllowed) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                String itemLabel = item.getModelObject();
                item.add(new Label("writableItem", itemLabel));
            }

        };
        add(writableList);
    }

    private static final long serialVersionUID = 1L;

}
