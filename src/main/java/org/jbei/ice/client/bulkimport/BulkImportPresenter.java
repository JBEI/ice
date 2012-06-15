package org.jbei.ice.client.bulkimport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.AppController;
import org.jbei.ice.client.admin.bulkimport.BulkImportMenuItem;
import org.jbei.ice.client.admin.bulkimport.DeleteBulkImportHandler;
import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent;
import org.jbei.ice.client.bulkimport.events.BulkImportDraftSubmitEvent.BulkImportDraftSubmitEventHandler;
import org.jbei.ice.client.bulkimport.events.BulkImportSubmitEvent;
import org.jbei.ice.client.bulkimport.events.BulkImportSubmitEventHandler;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEventHandler;
import org.jbei.ice.client.bulkimport.model.BulkImportModel;
import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.bulkimport.model.SheetFieldData;
import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.EntryAddType;
import org.jbei.ice.shared.dto.BulkImportDraftInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Presenter for the bulk import page
 * 
 * @author Hector Plahar
 */
public class BulkImportPresenter extends AbstractPresenter {

    private final IBulkImportView view;
    private final HashMap<EntryAddType, NewBulkInput> sheetCache;
    private final BulkImportModel model;
    private NewBulkInput currentInput;

    public BulkImportPresenter(BulkImportModel model, final IBulkImportView display) {
        this.view = display;
        this.model = model;
        sheetCache = new HashMap<EntryAddType, NewBulkInput>();

        setClickHandlers();

        // selection model handlers
        setMenuSelectionModel();
        setCreateSelectionModel();

        // toggle menu
        addToggleMenuHandler();

        // retrieveData
        retrieveSavedDrafts();
        retrieveAutoCompleteData();
    }

    private void setClickHandlers() {
        SheetDraftUpdateHandler handler = new SheetDraftUpdateHandler();
        view.setDraftUpdateHandler(handler);

        // submit
        SheetSubmitHandler submitHandler = new SheetSubmitHandler();
        view.setSubmitHandler(submitHandler);

        // reset
        SheetResetHandler resetHandler = new SheetResetHandler();
        view.setResetHandler(resetHandler);

        // draft save
        SheetDraftSaveHandler draftSaveHandler = new SheetDraftSaveHandler();
        view.setDraftSaveHandler(draftSaveHandler);
    }

    private void addToggleMenuHandler() {
        view.addToggleMenuHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                view.setDraftMenuVisibility(!view.getMenuVisibility());
            }
        });
    }

    private void setMenuSelectionModel() {
        final SingleSelectionModel<BulkImportMenuItem> draftSelection = view.getDraftMenuModel();
        draftSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final BulkImportMenuItem item = draftSelection.getSelectedObject();
                model.retrieveBulkImport(item.getId(), new SavedDraftsEventHandler() {

                    @Override
                    public void onDataRetrieval(SavedDraftsEvent event) {
                        if (event == null) {
                            view.showFeedback("Could not retrieve your saved drafts.", true);
                            return;
                        }

                        BulkImportDraftInfo info = event.getData().get(0);
                        Sheet sheet = new Sheet(info.getType(), info);

                        sheet.setAutoCompleteData(AppController.autoCompleteData);
                        currentInput = new NewBulkInput(info.getType(), sheet);
                        currentInput.setId(info.getId());
                        String name = info.getName();
                        if (name == null) {
                            name = DateUtilities.formatDate(info.getCreated());
                            info.setName(name);
                        }
                        currentInput.setName(name);

                        view.setSheet(currentInput, false);
                        view.setHeader(info.getType().getDisplay() + " Bulk Import");
                        //                                                view.setMenuVisibility(false);
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
                if (selection == null)
                    return;

                if (sheetCache.containsKey(selection))
                    currentInput = sheetCache.get(selection);
                else {
                    Sheet sheet = new Sheet(selection);
                    sheet.setAutoCompleteData(AppController.autoCompleteData);
                    currentInput = new NewBulkInput(selection, sheet);

                    // header Panel 
                    sheetCache.put(selection, currentInput);
                }
                view.setSheet(currentInput, true);
                view.setHeader(selection.getDisplay() + " Bulk Import");
                view.setDraftMenuVisibility(false);
                createSelection.setSelected(selection, false);
            }
        });
    }

    private void retrieveSavedDrafts() {
        this.model.retrieveDraftMenuData(new SavedDraftsEventHandler() {

            @Override
            public void onDataRetrieval(SavedDraftsEvent event) {
                ArrayList<BulkImportMenuItem> data = new ArrayList<BulkImportMenuItem>();
                for (BulkImportDraftInfo info : event.getData()) {
                    String name = info.getName();
                    String dateTime = DateUtilities.formatShorterDate(info.getCreated());
                    BulkImportMenuItem item = new BulkImportMenuItem(info.getId(), name, info
                            .getCount(), dateTime, info.getType().toString(), info.getAccount()
                            .getEmail());
                    data.add(item);
                }

                if (!data.isEmpty()) {
                    view.setSavedDraftsData(data, new DeleteBulkImportHandler(model.getService()));
                } else
                    view.setToggleMenuVisibility(false);
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

        @Override
        public void onClick(ClickEvent event) {
            boolean isValid = currentInput.getSheet().validate();
            if (!isValid) {
                view.showFeedback("Please correct validation errors.", true);
                return;
            }

            ArrayList<SheetFieldData[]> cellData = currentInput.getSheet().getCellData();
            if (cellData == null || cellData.isEmpty()) {
                view.showFeedback("Please enter data into the sheet before saving.", true);
                return;
            }

            model.submitBulkImport(currentInput.getImportType(), cellData,
                new BulkImportSubmitEventHandler() {

                    @Override
                    public void onSubmit(BulkImportSubmitEvent event) {
                        if (event.isSuccess()) {
                            //
                            // TODO : reset sheet to avoid re-submission
                            view.showFeedback("Entries submitted successfully for verification.",
                                false);
                        } else {
                            view.showFeedback("Error saving entries.", true);
                        }
                    }
                });
        }
    }

    private class SheetResetHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            // TODO : ask for confirmation
            currentInput.getSheet().clear();
        }
    }

    private class SheetDraftSaveHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            String name = view.getDraftName();
            currentInput.setName(name);
            // TODO : validation for draft save
            //            if (name == null || name.isEmpty()) {
            //                panel.getDraftInput().setStyleName("bulk_import_draft_input_error");
            //                return;
            //            }
            //
            //            // save draft
            //            panel.getDraftInput().setStyleName("bulk_import_draft_input");
            ArrayList<SheetFieldData[]> cellData = currentInput.getSheet().getCellData();
            if (cellData == null || cellData.isEmpty()) {
                view.showFeedback("Please enter data into the sheet before saving draft", true);
                return;
            }

            model.saveBulkImportDraftData(currentInput.getImportType(), name, cellData,
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
                            String dateTime = DateUtilities.formatShorterDate(info.getCreated());
                            BulkImportMenuItem item = new BulkImportMenuItem(info.getId(), info
                                    .getName(), info.getCount(), dateTime, info.getType()
                                    .toString(), info.getAccount().getEmail());

                            view.addSavedDraftData(item,
                                new DeleteBulkImportHandler(model.getService()));
                            currentInput.setName(info.getName());
                            currentInput.setId(info.getId());

                            view.setSheet(currentInput, false);
                            view.setHeader(currentInput.getImportType().getDisplay()
                                    + " Bulk Import");
                        }
                    }
                });
        }
    }

    private class SheetDraftUpdateHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            if (currentInput == null)
                return;

            long id = currentInput.getId();
            EntryAddType type = currentInput.getImportType();
            String name = currentInput.getName();
            ArrayList<SheetFieldData[]> cellData = currentInput.getSheet().getCellData();

            model.updateBulkImportDraft(id, type, name, cellData,
                new BulkImportDraftSubmitEventHandler() {

                    @Override
                    public void onSubmit(BulkImportDraftSubmitEvent event) {
                        if (event == null || event.getDraftInfo() == null)
                            view.showFeedback("Error updating draft", true);
                        else {
                            BulkImportDraftInfo info = event.getDraftInfo();
                            view.showFeedback("Update successful", false);
                            // TODO : update menu with new counts
                            //                            MenuItem item = new MenuItem(info.getId(), info.getName(), info
                            //                                    .getCount(), false);
                            //                            view.addSavedDraftData(item, null); // TODO : deleteHandler
                        }
                    }
                });
        }
    }
}
