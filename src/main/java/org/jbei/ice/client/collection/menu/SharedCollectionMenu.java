package org.jbei.ice.client.collection.menu;

import java.util.Arrays;
import java.util.List;

import org.jbei.ice.client.ClientController;

/**
 * Collection menu for shared collections. Adds additional functionality for administrators
 * and regular users
 *
 * @author Hector Plahar
 */
public class SharedCollectionMenu extends CollectionMenu {

    public SharedCollectionMenu() {
        super(false, "Shared Collections");

        if (ClientController.account.isAdmin()) {
            List<HoverOption> options = Arrays.asList(HoverOption.PIN);
            setCellHoverOptions(options);
        }

        setEmptyCollectionMessage("No collections have been shared with you");
    }
}
