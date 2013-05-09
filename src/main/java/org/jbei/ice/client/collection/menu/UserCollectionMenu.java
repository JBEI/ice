package org.jbei.ice.client.collection.menu;

import java.util.ArrayList;
import java.util.List;

import org.jbei.ice.client.ClientController;

/**
 * @author Hector Plahar
 */
public class UserCollectionMenu extends CollectionMenu {

    public UserCollectionMenu() {
        super(true, "My Collections");
        List<HoverOption> options = new ArrayList<HoverOption>();
        options.add(HoverOption.EDIT);
        options.add(HoverOption.DELETE);
        options.add(HoverOption.SHARE);

        if (ClientController.account.isAdmin()) {
            options.add(HoverOption.PIN);
        }

        setCellHoverOptions(options);
        setEmptyCollectionMessage("No collections available");
    }
}
