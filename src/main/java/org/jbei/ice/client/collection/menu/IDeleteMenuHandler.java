package org.jbei.ice.client.collection.menu;

import com.google.gwt.event.dom.client.ClickHandler;

public interface IDeleteMenuHandler {

    boolean delete(long id);

    ClickHandler getUndoHandler(MenuItem item, CollectionMenu menu, MenuHiderTimer timer);
}
