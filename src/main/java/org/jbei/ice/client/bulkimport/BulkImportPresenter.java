package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

public class BulkImportPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IBulkImportView view;
    private final SingleSelectionModel<ImportType> selectionModel;
    private HashMap<AutoCompleteField, ArrayList<String>> autoCompleteData;

    public BulkImportPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final IBulkImportView display) {

        this.service = service;
        this.eventBus = eventBus;
        this.view = display;

        selectionModel = new SingleSelectionModel<ImportType>();

        // add menu items
        this.view.getMenu().setRowData(Arrays.asList(ImportType.values()));
        this.view.getMenu().setSelectionModel(selectionModel);

        this.selectionModel.addSelectionChangeHandler(new Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                ImportType selection = selectionModel.getSelectedObject();
                display.setHeader(selection.getDisplay());
                Sheet sheet = SheetFactory.getSheetForType(selection);
                if (sheet == null)
                    display.setSheet(new Label("Error"));
                else
                    display.setSheet(sheet);
            }
        });

        retrieveAutoCompleteData();

        setSaveDraftHandler();

        setDraftMenu();
    }

    protected void setSaveDraftHandler() {
        this.view.getSaveDraftButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
            }
        });
    }

    protected void setDraftMenu() {
        service.retrieveImportDraftData(AppController.sessionId,
            AppController.accountInfo.getEmail(),
            new AsyncCallback<ArrayList<BulkImportDraftInfo>>() {

                @Override
                public void onFailure(Throwable caught) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onSuccess(ArrayList<BulkImportDraftInfo> result) {
                    view.getDraftMenu().setRowData(result);
                }
            });
    }

    protected void retrieveAutoCompleteData() {
        // TODO : need to get it everytime this page is loaded?
        //        service.retrieveAutoCompleteData(AppController.sessionId,
        //            new AsyncCallback<HashMap<AutoCompleteField, ArrayList<String>>>() {
        //
        //                @Override
        //                public void onFailure(Throwable caught) {
        //                    Window.alert("Failed to retrieve the autocomplete data: " + caught.getMessage());
        //                }
        //
        //                @Override
        //                public void onSuccess(HashMap<AutoCompleteField, ArrayList<String>> result) {
        //                    autoCompleteData = new HashMap<AutoCompleteField, ArrayList<String>>(result);
        //                }
        //            });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }
}
