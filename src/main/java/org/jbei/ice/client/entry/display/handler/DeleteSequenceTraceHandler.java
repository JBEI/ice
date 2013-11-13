package org.jbei.ice.client.entry.display.handler;

import java.util.ArrayList;
import java.util.Set;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.common.entry.IHasPartData;
import org.jbei.ice.client.entry.display.view.IEntryView;
import org.jbei.ice.client.entry.display.view.MenuItem;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.entry.SequenceAnalysisInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class DeleteSequenceTraceHandler implements ClickHandler {

    private final IHasPartData<PartData> hasPart;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    private final IEntryView display;

    public DeleteSequenceTraceHandler(RegistryServiceAsync service, HandlerManager eventBus, IEntryView view,
            IHasPartData<PartData> hasPart) {
        this.hasPart = hasPart;
        this.service = service;
        this.eventBus = eventBus;
        this.display = view;
    }

    @Override
    public void onClick(ClickEvent event) {
        final long entryId = hasPart.getPart().getId();
        Set<SequenceAnalysisInfo> selected = display.getSequenceTableSelectionModel().getSelectedSet();
        if (selected == null || selected.isEmpty())
            return;

        final ArrayList<String> fileIds = new ArrayList<String>();
        for (SequenceAnalysisInfo info : selected) {
            fileIds.add(info.getFileId());
        }

        new IceAsyncCallback<ArrayList<SequenceAnalysisInfo>>() {

            @Override
            protected void callService(AsyncCallback<ArrayList<SequenceAnalysisInfo>> callback)
                    throws AuthenticationException {
                service.deleteEntryTraceSequences(ClientController.sessionId, entryId, fileIds, callback);
            }

            @Override
            public void onSuccess(ArrayList<SequenceAnalysisInfo> result) {
                display.setSequenceData(result, hasPart.getPart());
                display.getMenu().updateMenuCount(MenuItem.Menu.SEQ_ANALYSIS, result.size());
            }
        }.go(eventBus);
    }
}