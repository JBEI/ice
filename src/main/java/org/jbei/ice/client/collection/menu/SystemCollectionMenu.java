package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.ClientController;

import java.util.Arrays;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class SystemCollectionMenu extends CollectionMenu {

    public SystemCollectionMenu() {
        super(false, "Collections");
        setEmptyCollectionMessage("No collections available");

        if (ClientController.account.isAdmin()) {
            List<HoverOption> options = Arrays.asList(HoverOption.UNPIN);
            setCellHoverOptions(options);
        }
    }
}
