package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.Callback;

import com.google.gwt.event.dom.client.ClickHandler;

public interface IDeleteMenuHandler {

    void delete(long id, Callback<MenuItem> deleteCallback);

    ClickHandler getUndoHandler(MenuItem item, CollectionMenu menu, MenuHiderTimer timer);
}
