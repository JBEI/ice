package org.jbei.ice.client.collection.menu;

import java.util.Arrays;
import java.util.List;

import org.jbei.ice.client.ClientController;

/**
 * @author Hector Plahar
 */
public class SystemCollectionMenu extends CollectionMenu {

    public SystemCollectionMenu() {
        super(false, "Collections");
        setEmptyCollectionMessage("No collections available");

        if (ClientController.account.isAdmin()) {
            List<HoverOption> options = Arrays.asList(HoverOption.UNPIN, HoverOption.SHARE);
            setCellHoverOptions(options);
        }
    }
}
