package org.jbei.ice.client.presenter;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.view.form.NewEntryForm;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class EntryAddPresenter extends Presenter {

    public interface Display {

        void setAutoCompleteData(HashMap<AutoCompleteField, ArrayList<String>> data);

        HashMap<AutoCompleteField, ArrayList<String>> getAutoCompleteData();

        NewEntryForm getForm();

        HasClickHandlers getSubmitButton();

        Widget asWidget();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;
    private final String sid;

    public EntryAddPresenter(RegistryServiceAsync service, HandlerManager eventBus, Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;
        sid = AppController.sessionId;

        bind();
    }

    protected void bind() {

        display.getSubmitButton().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                NewEntryForm form = display.getForm();
                save(form.getEntry());
            }
        });

        if (display.getAutoCompleteData() != null)
            return;

        service.retrieveAutoCompleteData(sid,
            new AsyncCallback<HashMap<AutoCompleteField, ArrayList<String>>>() {

                @Override
                public void onFailure(Throwable caught) {
                    Window.alert("Failed to retrieve the autocomplete data: " + caught.getMessage());
                }

                @Override
                public void onSuccess(HashMap<AutoCompleteField, ArrayList<String>> result) {
                    display.setAutoCompleteData(result);
                }
            });
    }

    protected void save(EntryInfo entry) {
        //        switch (entry.getRecordType()) {
        //
        //        }
        // if save is successful
    }

    @Override
    public void go(HasWidgets container) {

        container.clear();
        container.add(this.display.asWidget());
    }
}
