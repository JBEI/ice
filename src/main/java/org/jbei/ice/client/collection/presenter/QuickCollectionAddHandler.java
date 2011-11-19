package org.jbei.ice.client.collection.presenter;

import org.jbei.ice.shared.FolderDetails;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

public class QuickCollectionAddHandler {

    private final ListDataProvider<FolderDetails> dataProvider;
    private final Button showButton;
    private final TextBox collectionNameBox;

    public QuickCollectionAddHandler(Button showButton, final TextBox collectionNameBox,
            ListDataProvider<FolderDetails> dataProvider) {

        this.dataProvider = dataProvider;
        this.showButton = showButton;
        this.collectionNameBox = collectionNameBox;

        this.addClickHandler();
        this.addFocusHandler();
        this.addKeyPressHandler();
    }

    private void addKeyPressHandler() {
        collectionNameBox.addKeyPressHandler(new KeyPressHandler() {

            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getCharCode() != KeyCodes.KEY_ENTER)
                    return;

                // TODO : save the collection
                collectionNameBox.setVisible(false);
                saveCollection(collectionNameBox.getText());
            }
        });
    }

    private void addFocusHandler() {
        collectionNameBox.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                collectionNameBox.setText("");
            }
        });
    }

    private void addClickHandler() {
        showButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (collectionNameBox.isVisible())
                    collectionNameBox.setVisible(false);
                else {
                    collectionNameBox.setText("");
                    collectionNameBox.setVisible(true);
                }
            }
        });
    }

    private void saveCollection(String value) {
        if (value.isEmpty())
            return;

        FolderDetails newFolder = new FolderDetails();
        newFolder.setCount(0);
        newFolder.setIsSystemFolder(false);
        newFolder.setName(value);
        this.dataProvider.getList().add(newFolder);
    }

    public void hideCollectionBox() {
        collectionNameBox.setVisible(false);
    }
}
