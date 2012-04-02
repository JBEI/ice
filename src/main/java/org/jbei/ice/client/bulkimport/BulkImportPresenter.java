package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent;
import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent.BulkImportDraftSubmitEventHandler;
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
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class BulkImportPresenter extends AbstractPresenter {

    private final IBulkImportView view;
    private final HashMap<EntryAddType, NewBulkInput> sheetCache;
    private final BulkImportModel model;

    public BulkImportPresenter(BulkImportModel model, final IBulkImportView display) {
        this.view = display;
        this.model = model;
        sheetCache = new HashMap<EntryAddType, NewBulkInput>();

        // selection model handlers
        setMenuSelectionModel();
        setCreateSelectionModel();

        // toggle menu
        addToggleMenuHandler();

        // retrieveData
        retrieveSavedDrafts();
        retrieveAutoCompleteData();
    }

    private void addToggleMenuHandler() {
        view.addToggleMenuHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                view.setMenuVisibility(!view.getMenuVisibility());
            }
        });
    }

    private void setMenuSelectionModel() {
        final SingleSelectionModel<MenuItem> draftSelection = view.getDraftMenuModel();
        draftSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final MenuItem item = draftSelection.getSelectedObject();
                model.retrieveUserSavedDrafts(item.getId(), new SavedDraftsEventHandler() {

                    @Override
                    public void onDataRetrieval(SavedDraftsEvent event) {
                        if (event == null)
                            return; // TODO : error msg

                        BulkImportDraftInfo info = event.getData().get(0);
                        Sheet sheet = new Sheet(info.getType(), info);
                        sheet.setAutoCompleteData(AppController.autoCompleteData);
                        NewBulkInput input = new NewBulkInput(info.getType(), sheet);

                        // submit handler
                        SheetSubmitHandler handler = new SheetSubmitHandler(input);
                        input.getSheetHeaderPanel().getSubmit().addClickHandler(handler);

                        // reset
                        SheetResetHandler resetHandler = new SheetResetHandler(input);
                        input.getSheetHeaderPanel().getReset().addClickHandler(resetHandler);

                        // save draft
                        SheetDraftSaveHandler draftSaveHandler = new SheetDraftSaveHandler(input);
                        input.getSheetHeaderPanel().getDraftSave()
                                .addClickHandler(draftSaveHandler);
                        view.setSheet(input);
                        view.setHeader(item.getName());
                        view.setMenuVisibility(false);
                    }
                });
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
                    Sheet sheet = new Sheet(selection, null);
                    sheet.setAutoCompleteData(AppController.autoCompleteData);
                    input = new NewBulkInput(selection, sheet);

                    // submit handler
                    SheetSubmitHandler handler = new SheetSubmitHandler(input);
                    input.getSheetHeaderPanel().getSubmit().addClickHandler(handler);

                    // reset
                    SheetResetHandler resetHandler = new SheetResetHandler(input);
                    input.getSheetHeaderPanel().getReset().addClickHandler(resetHandler);

                    // save draft
                    SheetDraftSaveHandler draftSaveHandler = new SheetDraftSaveHandler(input);
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
                    String name = info.getName();
                    if (name == null)
                        name = DateUtilities.formatDate(info.getCreated());
                    MenuItem item = new MenuItem(info.getId(), name, info.getCount(), false);
                    data.add(item);
                }

                if (!data.isEmpty()) {
                    view.setSavedDraftsData(data, null); // tOdO : delete handler
                }
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
        private final NewBulkInput input;

        public SheetDraftSaveHandler(NewBulkInput input) {
            this.panel = input.getSheetHeaderPanel();
            this.input = input;
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
            ArrayList<SheetFieldData[]> cellData = input.getSheet().getCellData();
            if (cellData == null || cellData.isEmpty()) {
                view.showFeedback("Please enter data into the sheet before saving draft", true);
                return;
            }

            model.saveDraftData(input.getImportType(), name, cellData,
                new BulkImportDraftSubmitEventHandler() {

                    @Override
                    public void onSubmit(BulkImportDraftSubmitEvent event) {
                        if (event == null || event.getDraftInfo() == null)
                            view.showFeedback("Error saving draft", true);
                        else {
                            BulkImportDraftInfo info = event.getDraftInfo();
                            view.showFeedback(
                                "Draft \"" + info.getName() + "\" with <b>" + info.getCount()
                                        + "</b> entry/ies successfully saved", false);
                            MenuItem item = new MenuItem(info.getId(), info.getName(), info
                                    .getCount(), false);
                            view.addSavedDraftData(item, null); // TODO : deleteHandler
                            panel.setDraftName(event.getDraftInfo().getName());
                        }
                    }
                });
        }
    }
}
