package org.jbei.ice.client.collection.presenter;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.ICollectionView;
import org.jbei.ice.client.collection.add.EntryAddPresenter;
import org.jbei.ice.shared.EntryAddType;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Handler for create new entry selections
 * 
 * @author Hector Plahar
 * 
 */
public class CreateNewEntrySelectionHandler implements SelectionChangeEvent.Handler {

    private final EntryAddPresenter presenter;
    private final ICollectionView display;
    private final SingleSelectionModel<EntryAddType> selectionModel;

    public CreateNewEntrySelectionHandler(CollectionsPresenter collectionsPresenter,
            RegistryServiceAsync registryServiceAsync, HandlerManager handlerManager,
            ICollectionView display, SingleSelectionModel<EntryAddType> selectionModel) {
        presenter = new EntryAddPresenter(collectionsPresenter, registryServiceAsync,
                handlerManager);
        this.display = display;
        this.selectionModel = selectionModel;
    }

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {

        EntryAddType type = selectionModel.getSelectedObject();
        if (type == null)
            return;
        presenter.setType(type);
        display.setMainContent(presenter.getView(), false);
        display.getAddEntrySelectionHandler().setSelected(type, false);
    }
}
