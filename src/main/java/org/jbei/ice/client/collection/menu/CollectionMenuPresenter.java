package org.jbei.ice.client.collection.menu;

/**
 * @author Hector Plahar
 */
public class CollectionMenuPresenter {

    public interface IView {
    }

    private final IView view;

    public CollectionMenuPresenter(IView view) {
        this.view = view;
    }
}
