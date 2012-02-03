package org.jbei.ice.client.bulkimport;

import java.util.HashMap;

import org.jbei.ice.client.AbstractPresenter;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEvent;
import org.jbei.ice.client.bulkimport.events.SavedDraftsEventHandler;
import org.jbei.ice.client.bulkimport.model.BulkImportModel;
import org.jbei.ice.client.bulkimport.model.NewBulkInput;
import org.jbei.ice.client.bulkimport.panel.SheetHeaderPanel;
import org.jbei.ice.client.bulkimport.sheet.Sheet;
import org.jbei.ice.client.common.FeedbackPanel;
import org.jbei.ice.client.event.AutoCompleteDataEvent;
import org.jbei.ice.client.event.AutoCompleteDataEventHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;

public class BulkImportPresenter extends AbstractPresenter {

    private final IBulkImportView view;
    private HashMap<ImportType, NewBulkInput> sheetCache;
    private final FeedbackPanel feedback;
    private final BulkImportModel model;

    public BulkImportPresenter(BulkImportModel model, final IBulkImportView display) {

        this.view = display;
        this.model = model;

        sheetCache = new HashMap<ImportType, NewBulkInput>();

        // retrieveData
        retrieveSavedDrafts();
        retrieveAutoCompleteData();

        // add menu items
        this.view.getMenu().addClickHandler(new MainMenuClickHandler());

        // set feedback panel
        feedback = new FeedbackPanel(true, "330px");
        this.view.setFeedbackPanel(feedback);
    }

    private void retrieveSavedDrafts() {
        this.model.retrieveDraftMenuData(new SavedDraftsEventHandler() {

            @Override
            public void onDataRetrieval(SavedDraftsEvent event) {
                view.getDraftMenu().setData(event.getData());
            }
        });
    }

    private void retrieveAutoCompleteData() {
        this.model.retrieveAutoCompleteData(new AutoCompleteDataEventHandler() {

            @Override
            public void onDataRetrieval(AutoCompleteDataEvent event) {
                // TODO : do something with the data
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
                return;
                // TODO : feedback panel message
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
            input.getSheet().reset();
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

    private class MainMenuClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            feedback.setVisible(false);
            ImportType selection = view.getMenu().getCurrentSelection();
            view.setHeader(selection.getDisplay() + " BULK IMPORT");
            final NewBulkInput input;

            if (sheetCache.containsKey(selection))
                input = sheetCache.get(selection);
            else {
                Sheet sheet = SheetFactory.getSheetForType(selection);
                if (sheet == null) {
                    view.setSheet(new Label("Error"));
                    return;
                }

                input = new NewBulkInput(selection, sheet);

                // submit handler
                SheetSubmitHandler handler = new SheetSubmitHandler(input);
                input.getSheetHeaderPanel().getSubmit().addClickHandler(handler);

                // reset
                SheetResetHandler resetHandler = new SheetResetHandler(input);
                input.getSheetHeaderPanel().getReset().addClickHandler(resetHandler);

                // save draft
                SheetDraftSaveHandler draftSaveHandler = new SheetDraftSaveHandler(
                        input.getSheetHeaderPanel());
                input.getSheetHeaderPanel().getDraftSave().addClickHandler(draftSaveHandler);

                // header Panel 
                sheetCache.put(selection, input);
            }
            view.setSheet(input);
        }
    }
}
