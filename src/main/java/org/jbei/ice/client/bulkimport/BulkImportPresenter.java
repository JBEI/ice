package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.bulkimport.events.BulkImportSubmitEvent;
import org.jbei.ice.client.bulkimport.events.BulkImportSubmitEventHandler;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEventHandler;
import org.jbei.ice.client.bulkimport.model.BulkImportModel;
import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.bulkimport.model.SheetFieldData;
import org.jbei.ice.client.bulkimport.panel.SheetHeaderPanel;
import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.client.collection.menu.MenuItem;
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
                    sheet.setAutoCompleteData(AppController.autoCompleteData);
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
                view.setHeader(selection.getDisplay() + " Bulk Import");
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
        for (Entry<EntryAddType, NewBulkInput> entry : sheetCache.entrySet()) {
            NewBulkInput input = entry.getValue();
            input.getSheet().setAutoCompleteData(AppController.autoCompleteData);
        }
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

            ArrayList<SheetFieldData[]> cellData = input.getSheet().getCellData();
            if (cellData == null || cellData.isEmpty()) {
                view.showFeedback("Please enter data into the sheet before saving", true);
                return;
            }

            view.clearFeedback();

            model.saveData(input.getImportType(), cellData, new BulkImportSubmitEventHandler() {

                @Override
                public void onSubmit(BulkImportSubmitEvent event) {
                    if (event.isSuccess()) {
                        //
                        // TODO : reset
                        view.showFeedback("Entries submitted successfully for verification.", false);
                    } else {
                        view.showFeedback("Error saving entries", true);
                    }
                }
            });
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

        }
    }
}
