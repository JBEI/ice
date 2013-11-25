package org.jbei.ice.client.profile.message;

import java.util.LinkedList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.dto.message.MessageInfo;
import org.jbei.ice.lib.shared.dto.message.MessageList;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class MessageDataProvider extends AsyncDataProvider<MessageInfo> {

    private final MessageDataTable table;
    private final RegistryServiceAsync service;
    private final HandlerManager eventBus;
    protected LinkedList<MessageInfo> cachedEntries;
    private MessageList messages;

    public MessageDataProvider(MessageDataTable table, RegistryServiceAsync service, HandlerManager bus) {
        this.table = table;
        this.service = service;
        this.eventBus = bus;
        cachedEntries = new LinkedList<MessageInfo>();
        this.addDataDisplay(table);
    }

    @Override
    protected void onRangeChanged(HasData<MessageInfo> display) {
        if (messages == null || messages.getTotalSize() == 0)   // display changed its range of interest but no data
            return;

        // values of range to display from view
        final Range range = display.getVisibleRange();
        final int rangeStart = range.getStart();
        final int rangeEnd = (rangeStart + range.getLength()) > messages.getTotalSize() ? messages.getTotalSize()
                : (rangeStart + range.getLength());

        // sort did not change
        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));

        if (rangeEnd == cachedEntries.size()) { // or close enough within some delta, retrieve more
            cacheMore(rangeEnd, rangeEnd + range.getLength());
        }
    }

    protected void cacheMore(int rangeStart, int rangeEnd) {
        int factor = (rangeEnd - rangeStart) * 2;  //  pages in advance
        fetchEntryData(rangeStart, factor, false);
    }

    protected void fetchEntryData(final int start, final int factor, final boolean reset) {
        new IceAsyncCallback<MessageList>() {

            @Override
            protected void callService(AsyncCallback<MessageList> callback) throws AuthenticationException {
                service.retrieveMessages(ClientController.sessionId, start, factor, callback);
            }

            @Override
            public void onSuccess(MessageList result) {
                messages = result;
                if (result == null) {
                    return;
                }

                if (reset)
                    setMessages(result);
                else {
                    cachedEntries.addAll(result.getList());
//                    pager.setLoading(true);  //todo
                }
            }
        }.go(eventBus);
    }

    public void setMessages(MessageList messages) {
        reset();
        this.messages = messages;
        if (messages == null) {
            updateRowCount(0, true);
            return;
        }

        cachedEntries.addAll(messages.getList());
        updateRowCount(messages.getTotalSize(), true);

        // retrieve the first page of results and updateRowData
        final Range range = this.table.getVisibleRange();
        final int rangeStart = 0;
        int rangeEnd = rangeStart + range.getLength();
        if (rangeEnd > messages.getTotalSize())
            rangeEnd = messages.getTotalSize();

        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));
        table.setPageStart(0);
    }

    public void reset() {
        this.cachedEntries.clear();
        this.messages = null;
        this.table.setVisibleRangeAndClearData(table.getVisibleRange(), false);
    }
}



