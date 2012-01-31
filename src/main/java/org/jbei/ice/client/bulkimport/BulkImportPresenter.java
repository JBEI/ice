package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;

public class BulkImportPresenter extends AbstractPresenter {

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IBulkImportView view;
    private HashMap<AutoCompleteField, ArrayList<String>> autoCompleteData;
    private HashMap<ImportType, NewBulkInput> sheetCache;

    public BulkImportPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final IBulkImportView display) {

        this.service = service;
        this.eventBus = eventBus;
        this.view = display;

        sheetCache = new HashMap<ImportType, NewBulkInput>();

        // add menu items
        this.view.getMenu().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
                ImportType selection = view.getMenu().getCurrentSelection();
                display.setHeader(selection.getDisplay() + " BULK IMPORT");
                final NewBulkInput input;

                if (sheetCache.containsKey(selection))
                    input = sheetCache.get(selection);
                else {
                    Sheet sheet = SheetFactory.getSheetForType(selection);

                    if (sheet == null) {
                        display.setSheet(new Label("Error"));
                        return;
                    }

                    input = new NewBulkInput(sheet);
                    input.getSubmit().addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent event) {
                            input.getSheet().validate();
                        }
                    });
                }
                display.setSheet(input);
            }
        });

        retrieveAutoCompleteData();

        setSaveDraftHandler();

        setDraftMenu();
    }

    protected void setSaveDraftHandler() {
        //        this.view.getSaveDraftButton().addClickHandler(new ClickHandler() {
        //
        //            @Override
        //            public void onClick(ClickEvent event) {
        //                // TODO Auto-generated method stub
        //            }
        //        });
    }

    private void setSaveImportDataHandler() {
        //        this.view.getSaveDraftButton()
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
                    view.getDraftMenu().setData(result);
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
