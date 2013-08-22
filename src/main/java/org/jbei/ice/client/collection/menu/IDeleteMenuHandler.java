package org.jbei.ice.client.collection.menu;

import org.jbei.ice.client.Callback;

public interface IDeleteMenuHandler {

    void delete(long id, Callback<MenuItem> deleteCallback);
}
