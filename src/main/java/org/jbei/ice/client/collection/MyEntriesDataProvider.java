package org.jbei.ice.client.collection;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Hector Plahar
 */
public class MyEntriesDataProvider extends FolderEntryDataProvider {

    private final String userId;

    public MyEntriesDataProvider(DataTable<EntryInfo> view, RegistryServiceAsync service, String userId) {
        super(view, service);
        this.userId = userId;
    }

    @Override
    protected void makeServiceCall(final ColumnField field, final boolean ascending, int rangeStart, int factor) {
        service.retrieveUserEntries(AppController.sessionId, userId, field, ascending, rangeStart, factor,
                                    new AsyncCallback<FolderDetails>() {

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            Window.alert(caught.getMessage());
                                        }

                                        @Override
                                        public void onSuccess(FolderDetails result) {
                                            setData(result);
                                        }
                                    });
    }
}
