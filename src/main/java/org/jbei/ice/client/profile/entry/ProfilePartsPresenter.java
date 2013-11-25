package org.jbei.ice.client.profile.entry;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.FolderEntryDataProvider;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.entry.display.EntryPresenter;
import org.jbei.ice.client.event.ShowEntryListEvent;
import org.jbei.ice.client.event.ShowEntryListEventHandler;
import org.jbei.ice.client.profile.PanelPresenter;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.client.service.RegistryServiceAsync;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.folder.FolderDetails;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class ProfilePartsPresenter extends PanelPresenter {

    private final ProfilePartsView partsView;
    private final FolderEntryDataProvider folderDataProvider;
    private EntryPresenter entryViewPresenter;

    public ProfilePartsPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        this.partsView = new ProfilePartsView(createDelegate());
        this.folderDataProvider = new FolderEntryDataProvider(partsView.getTable(), service);

        eventBus.addHandler(ShowEntryListEvent.TYPE, new ShowEntryListEventHandler() {
            @Override
            public void onEntryListContextAvailable(ShowEntryListEvent event) {
                partsView.initView();
            }
        });
    }

    private ServiceDelegate<PartData> createDelegate() {
        return new ServiceDelegate<PartData>() {
            @Override
            public void execute(PartData entryInfo) {
                EntryContext context = new EntryContext(EntryContext.Type.COLLECTION);
                context.setNav(folderDataProvider);
                context.setId(entryInfo.getId());
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

        partsView.setContent(entryViewPresenter.getView().asWidget());
    }

    public void retrieveUserParts(String userId) {
        folderDataProvider.setUserId(userId);
        service.retrieveUserEntries(ClientController.sessionId, userId, ColumnField.CREATED, false, 0,
                                    partsView.getTable().getVisibleRange().getLength(),
                                    new AsyncCallback<FolderDetails>() {

                                        @Override
                                        public void onSuccess(FolderDetails folder) {
                                            folderDataProvider.setFolderData(folder, true);
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            folderDataProvider.setFolderData(null, true);
                                        }
                                    });
    }

    @Override
    public IUserProfilePanel getView() {
        return partsView;
    }
}
