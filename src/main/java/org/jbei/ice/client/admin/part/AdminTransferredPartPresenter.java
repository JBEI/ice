package org.jbei.ice.client.admin.part;

import java.util.ArrayList;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.collection.presenter.EntryContext;
import org.jbei.ice.client.entry.display.EntryPresenter;
import org.jbei.ice.lib.shared.dto.entry.PartData;

import com.google.gwt.event.shared.HandlerManager;

/**
 * Panel presenter for managing parts that have been transferred from other registries
 *
 * @author Hector Plahar
 */
public class AdminTransferredPartPresenter extends AdminPanelPresenter {

    private final AdminTransferredPartPanel panel;
    private final TransferredPartDataProvider dataProvider;
    private EntryPresenter entryViewPresenter;

    public AdminTransferredPartPresenter(final RegistryServiceAsync service, HandlerManager eventBus) {
        super(service, eventBus);
        panel = new AdminTransferredPartPanel(createDelegate());
        dataProvider = new TransferredPartDataProvider(panel.getDataTable(), service);
    }

    private ServiceDelegate<PartData> createDelegate() {
        return new ServiceDelegate<PartData>() {
            @Override
            public void execute(PartData entryInfo) {
                EntryContext context = new EntryContext(EntryContext.Type.COLLECTION);
                context.setNav(dataProvider);
                context.setId(entryInfo.getId());
                context.setRecordId(entryInfo.getRecordId());
                showEntryView(context);
            }
        };
    }

    public void showEntryView(EntryContext event) {
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
}
