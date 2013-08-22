package org.jbei.ice.client.admin.part;

import java.util.ArrayList;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.entry.display.EntryPresenter;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.client.event.ShowEntryListEventHandler;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * Panel presenter for managing parts that have been transferred from other registries
 *
 * @author Hector Plahar
 */
public class AdminTransferredPartPresenter extends AdminPanelPresenter {

    private final AdminTransferredPartPanel panel;
    private final TransferredPartDataProvider dataProvider;
    private EntryPresenter entryViewPresenter;
    private boolean isViewingEntry;
    private long currentId;

    public AdminTransferredPartPresenter(RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new AdminTransferredPartPanel(createDelegate());
        dataProvider = new TransferredPartDataProvider(panel.getDataTable(), service);
        panel.setApproveClickHandler(new ApproveRejectHandler(true));
        panel.setRejectClickHandler(new ApproveRejectHandler(false));

        eventBus.addHandler(ShowEntryListEvent.TYPE, new ShowEntryListEventHandler() {
            @Override
            public void onEntryListContextAvailable(ShowEntryListEvent event) {
                isViewingEntry = false;
                panel.initView();
            }
        });

        panel.getDataTable().getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                boolean hasSelection = (panel.getDataTable().getSelectionModel().getSelectedSet().size() > 0);
                panel.setEnableApproveReject(hasSelection);
            }
        });
    }

    private ServiceDelegate<PartData> createDelegate() {
        return new ServiceDelegate<PartData>() {
            @Override
            public void execute(PartData entryInfo) {
                isViewingEntry = true;
                currentId = entryInfo.getId();
                panel.setEnableApproveReject(isViewingEntry);
                EntryContext context = new EntryContext(EntryContext.Type.COLLECTION);
                context.setNav(dataProvider);
                context.setId(currentId);
                context.setRecordId(entryInfo.getRecordId());
                showEntryView(context);
            }
        };
    }

    private void showEntryView(EntryContext event) {
        if (entryViewPresenter == null) {
            entryViewPresenter = new EntryPresenter(this.service, null, this.eventBus, event);
//            entryViewPresenter.setDeleteHandler(new DeleteEntryHandler()); // TODO
        } else {
            entryViewPresenter.setCurrentContext(event);
            entryViewPresenter.showCurrentEntryView();
        }

        // TODO
//        if (event.getPartnerUrl() == null || event.getPartnerUrl().trim().isEmpty())
//            History.newItem(Page.ENTRY_VIEW.getLink() + ";id=" + event.getId(), false);

        panel.setMainContent(entryViewPresenter.getView().asWidget());
    }

    @Override
    public IAdminPanel getView() {
        return panel;
    }

    public void setData(ArrayList<PartData> result) {
        dataProvider.setData(result, true);
    }

    /**
     * Handler for making service calls to the server to approve or
     * reject tranferred parts
     */
    private class ApproveRejectHandler implements ClickHandler {

        private boolean isAccept;

        public ApproveRejectHandler(boolean isAccept) {
            this.isAccept = isAccept;
        }

        @Override
        public void onClick(ClickEvent event) {
            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    ArrayList<Long> list = new ArrayList<Long>();
                    if (isViewingEntry) {
                        list.add(currentId);
                    } else {
                        list.addAll(panel.getSelectParts());
                    }

                    service.processTransferredParts(ClientController.sessionId, list, isAccept, callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    panel.refresh();
                }
            }.go(eventBus);
        }
    }
}
