package org.jbei.ice.client.storage;

import java.util.ArrayList;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class StoragePresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IStorageView display;

    public StoragePresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final IStorageView display, String param) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        long rootId = -1;
        try {
            if (param != null)
                rootId = Long.decode(param);
        } catch (NumberFormatException nfe) {
        }

        if (rootId == -1) {

            // get storage root 
            service.retrieveStorageRoot(AppController.sessionId,
                new AsyncCallback<ArrayList<StorageInfo>>() {

                    @Override
                    public void onSuccess(ArrayList<StorageInfo> result) {
                        Tree tree = createTree(result); // TODO : use a CellTree instead
                        display.setContent(tree);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Failed to retrieve storage \n\n" + caught.getMessage());
                    }
                });
        } else {
            service.retrieveChildren(AppController.sessionId, rootId,
                new AsyncCallback<ArrayList<StorageInfo>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("RPC failure: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(ArrayList<StorageInfo> result) {
                        Tree tree = createTree(result);
                        display.setContent(tree);
                    }
                });
        }
    }

    Tree createTree(ArrayList<StorageInfo> info) {
        Tree dynamicTree = new Tree();
        TreeItem root = dynamicTree.addItem("Storage");

        // add children
        for (StorageInfo childInfo : info) {
            TreeItem childItem = new TreeItem(childInfo.getDisplay());
            childItem.setUserObject(childInfo);
            childItem.addItem("Loading..."); // add dummy node
            root.addItem(childItem);
        }

        dynamicTree.addOpenHandler(new StorageTreeOpenHandler());
        return dynamicTree;
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    // 
    // inner classes
    // 
    private class StorageTreeOpenHandler implements OpenHandler<TreeItem> {

        @Override
        public void onOpen(OpenEvent<TreeItem> event) {
            final TreeItem item = event.getTarget();
            if (item.getUserObject() == null)
                return;

            StorageInfo info = (StorageInfo) item.getUserObject();

            service.retrieveChildren(AppController.sessionId, info.getId(),
                new AsyncCallback<ArrayList<StorageInfo>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Failure: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(ArrayList<StorageInfo> result) {

                        if (item.getChildCount() == 1
                                && "Loading...".equals(item.getChild(0).getText())) {
                            item.setState(false); // close

                            for (StorageInfo storageInfo : result) {

                                TreeItem child = new TreeItem(storageInfo.getDisplay());

                                // Samples represent leaves so we go down an extra layer if any are present
                                ArrayList<SampleInfo> samples = storageInfo.getSamples();
                                if (samples != null && !samples.isEmpty()) {

                                    for (SampleInfo sampleInfo : samples) {
                                        child.addItem(sampleInfo.getLabel());
                                    }

                                } else {
                                    child.addItem("Loading...");
                                    child.setUserObject(storageInfo);
                                }

                                item.addItem(child);
                            }

                            item.getChild(0).remove(); // remove dummy child
                            item.setState(true); // open
                        }
                    }
                });
        }
    }
}
