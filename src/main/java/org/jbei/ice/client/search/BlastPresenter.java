package org.jbei.ice.client.search;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.Presenter;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.shared.BlastOption;
import org.jbei.ice.shared.BlastProgram;
import org.jbei.ice.shared.dto.BlastResultInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class BlastPresenter implements Presenter {

    public interface Display {

        BlastResultsTable getResultsTable();

        String getSequence();

        BlastProgram getProgram();

        HasClickHandlers getSubmit();

        void setProgramOptions(HashMap<String, String> options);

        Widget asWidget();
    }

    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final Display display;

    public BlastPresenter(RegistryServiceAsync service, HandlerManager eventBus,
            final Display display) {

        this.service = service;
        this.eventBus = eventBus;
        this.display = display;

        // set program options
        HashMap<String, String> options = new HashMap<String, String>();
        for (BlastOption option : BlastOption.values()) {
            options.put(option.getDisplay(), option.name());
        }
        display.setProgramOptions(options);

        // submit handler
        this.display.getSubmit().addClickHandler(new BlastSearchHandler());
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(this.display.asWidget());
    }

    private class BlastSearchHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            String sequence = display.getSequence();
            BlastProgram program = display.getProgram();

            // make service call, get results an assign to table of results 
            service.blastSearch(AppController.sessionId, sequence, program,
                new AsyncCallback<ArrayList<BlastResultInfo>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Failure: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(ArrayList<BlastResultInfo> result) {
                        if (result == null)
                            return;

                        new BlastSearchDataProvider(display.getResultsTable(), result, service);
                    }
                });
        }
    }
}
