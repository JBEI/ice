package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEventHandler;
import org.jbei.ice.client.bulkimport.model.BulkImportModel;
import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.bulkimport.panel.SheetHeaderPanel;
import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.client.collection.menu.MenuItem;
import org.jbei.ice.client.event.AutoCompleteDataEvent;
import org.jbei.ice.client.event.AutoCompleteDataEventHandler;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class BulkImportPresenter extends AbstractPresenter {

    private final IBulkImportView view;
    private HashMap<EntryAddType, NewBulkInput> sheetCache;
    private final BulkImportModel model;
    private HashMap<AutoCompleteField, ArrayList<String>> data;

    public BulkImportPresenter(BulkImportModel model, final IBulkImportView display) {
        this.view = display;
        this.model = model;
        sheetCache = new HashMap<EntryAddType, NewBulkInput>();

        // selection model handlers
        setMenuSelectionModel();
        setCreateSelectionModel();

        // retrieveData
        retrieveSavedDrafts();
        retrieveAutoCompleteData();
    }

    private void setMenuSelectionModel() {
        final SingleSelectionModel<MenuItem> draftSelection = view.getDraftMenuModel();
        draftSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                MenuItem item = draftSelection.getSelectedObject();

                // TODO : load sheet with data loaded from database
            }
        });
    }

    private void setCreateSelectionModel() {
        final SingleSelectionModel<EntryAddType> createSelection = view.getImportCreateModel();
        createSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                EntryAddType selection = createSelection.getSelectedObject();

                final NewBulkInput input;

                if (sheetCache.containsKey(selection))
                    input = sheetCache.get(selection);
                else {
                    Sheet sheet = new Sheet(selection);
                    sheet.setAutoCompleteData(data);
                    input = new NewBulkInput(selection, sheet);

                    // submit handler
                    SheetSubmitHandler handler = new SheetSubmitHandler(input);
                    input.getSheetHeaderPanel().getSubmit().addClickHandler(handler);

                    // reset
                    SheetResetHandler resetHandler = new SheetResetHandler(input);
                    input.getSheetHeaderPanel().getReset().addClickHandler(resetHandler);

                    // save draft
                    SheetDraftSaveHandler draftSaveHandler = new SheetDraftSaveHandler(input
                            .getSheetHeaderPanel());
                    input.getSheetHeaderPanel().getDraftSave().addClickHandler(draftSaveHandler);

                    // header Panel 
                    sheetCache.put(selection, input);
                }
                view.setSheet(input);
            }
        });
    }

    private void retrieveSavedDrafts() {
        this.model.retrieveDraftMenuData(new SavedDraftsEventHandler() {

            @Override
            public void onDataRetrieval(SavedDraftsEvent event) {
                ArrayList<MenuItem> data = new ArrayList<MenuItem>();
                for (BulkImportDraftInfo info : event.getData()) {
                    MenuItem item = new MenuItem(info.getId(), info.getName(), info.getCount(),
                            false);
                    data.add(item);
                }

                //                view.setSavedDraftsData(data);
            }
        });
    }

    private void retrieveAutoCompleteData() {
        this.model.retrieveAutoCompleteData(new AutoCompleteDataEventHandler() {

            @Override
            public void onDataRetrieval(AutoCompleteDataEvent event) {
                HashMap<AutoCompleteField, ArrayList<String>> eventData = event.getData();
                if (eventData == null)
                    return;

                data = eventData;

                for (Entry<EntryAddType, NewBulkInput> entry : sheetCache.entrySet()) {
                    NewBulkInput input = entry.getValue();
                    input.getSheet().setAutoCompleteData(eventData);
                }
            }
        });
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.view.asWidget());
    }

    // inner classes
    private class SheetSubmitHandler implements ClickHandler {

        private final NewBulkInput input;

        public SheetSubmitHandler(NewBulkInput input) {
            this.input = input;
        }

        @Override
        public void onClick(ClickEvent event) {
            boolean isValid = input.getSheet().validate();
            if (!isValid) {
                view.showFeedback("Please correct validation errors.", true);
                return;
            }

            model.saveData(input.getImportType(), input.getSheet().getCellData());
        }
    }

    private class SheetResetHandler implements ClickHandler {
        private final NewBulkInput input;

        public SheetResetHandler(NewBulkInput input) {
            this.input = input;
        }

        @Override
        public void onClick(ClickEvent event) {
            input.getSheet().clear();
        }
    }

    private class SheetDraftSaveHandler implements ClickHandler {

        private final SheetHeaderPanel panel;

        public SheetDraftSaveHandler(SheetHeaderPanel panel) {
            this.panel = panel;
        }

        @Override
        public void onClick(ClickEvent event) {
            String name = panel.getDraftInput().getText();
            if (name == null || name.isEmpty()) {
                panel.getDraftInput().setStyleName("bulk_import_draft_input_error");
                return;
            }

            // save draft
            panel.getDraftInput().setStyleName("bulk_import_draft_input");

            //            service.saveBulkImportDraft(AppController.sessionId,
            //                AppController.accountInfo.getEmail(), name, input.getSheet().getInfos(), // TODO : get this from model
            //                new AsyncCallback<BulkImportDraftInfo>() {
            //
            //                    @Override
            //                    public void onSuccess(BulkImportDraftInfo result) {
            //                        if (result == null)
            //                            return;
            //
            //                        view.getDraftMenu().addMenuData(result);
            //                        panel.setDraftName(result.getName());
            //
            //                        // change view draft to read only
            //
            //                        // highlight just added menu; associate just created one with top menu
            //
            //                        // 
            //                    }
            //
            //                    @Override
            //                    public void onFailure(Throwable caught) {
            //                        feedback.setFailureMessage("Problem saving draft");
            //                    }
            //                });
        }
    }
}
