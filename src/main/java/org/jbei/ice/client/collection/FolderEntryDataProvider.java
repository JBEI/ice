package org.jbei.ice.client.collection;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.collection.table.CollectionDataTable;
import org.jbei.ice.client.common.EntryDataViewDataProvider;
import org.jbei.ice.client.common.table.DataTable;
import org.jbei.ice.client.common.table.EntryTablePager;
import org.jbei.ice.shared.ColumnField;
import org.jbei.ice.shared.FolderDetails;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.Range;

/**
 * @author Hector Plahar
 */
public class FolderEntryDataProvider extends EntryDataViewDataProvider {

    private FolderDetails details;
    private boolean nextDisabled = false;
    private final EntryTablePager pager;

    public FolderEntryDataProvider(CollectionDataTable view, RegistryServiceAsync service) {
        super(view, service);
        pager = view.getPager();
    }

    @Override
    protected void fetchEntryData(ColumnField field, boolean asc, int start, int factor, final boolean reset) {

        if (!reset)
            pager.setNextEnabled(false);

        switch ((int) details.getId()) {
            case -1:
                service.retrieveAllVisibleEntryIDs(AppController.sessionId, details, field, asc, start, factor,
                                                   new AsyncCallback<FolderDetails>() {

                                                       @Override
                                                       public void onFailure(Throwable caught) {
                                                           Window.alert(caught.getMessage());
                                                       }

                                                       @Override
                                                       public void onSuccess(FolderDetails result) {
                                                           details = result;
                                                           if (result == null) {
                                                               return;
                                                           }

                                                           if (reset)
                                                               setFolderData(details);
                                                           else {
                                                               cachedEntries.addAll(result.getEntries());
                                                               pager.setNextEnabled(true);
                                                           }
                                                       }
                                                   });
                break;

            case 0:
                service.retrieveUserEntries(AppController.sessionId, AppController.accountInfo.getId() + "",
                                            field, asc, start, factor,
                                            new AsyncCallback<FolderDetails>() {

                                                @Override
                                                public void onSuccess(FolderDetails result) {
                                                    details = result;
                                                    if (result == null) {
                                                        return;
                                                    }

                                                    if (reset)
                                                        setFolderData(details);
                                                    else
                                                        cachedEntries.addAll(result.getEntries());
                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    Window.alert(caught.getMessage());
                                                }
                                            });
                break;

            default:
                service.retrieveEntriesForFolder(AppController.sessionId, details.getId(), field, asc, start, factor,
                                                 new AsyncCallback<FolderDetails>() {

                                                     @Override
                                                     public void onSuccess(FolderDetails result) {
                                                         details = result;
                                                         if (result == null) {
                                                             return;
                                                         }

                                                         if (reset)
                                                             setFolderData(details);
                                                         else
                                                             cachedEntries.addAll(result.getEntries());
                                                     }

                                                     @Override
                                                     public void onFailure(Throwable caught) {
                                                         Window.alert(caught.getMessage());
                                                     }
                                                 });
        }
    }

    @Override
    public void reset() {
        this.cachedEntries.clear();
        details = null;
        this.table.setVisibleRangeAndClearData(table.getVisibleRange(), false);

        // reset sort
        if (lastSortField == null) {
            lastSortAsc = false;
            lastSortField = ColumnField.CREATED;

            this.table.getColumnSortList().clear();
            DataTable<EntryInfo>.DataTableColumn<?> defaultSortField = this.table.getColumn(lastSortField);

            if (defaultSortField != null) {
                ColumnSortList.ColumnSortInfo info = new ColumnSortList.ColumnSortInfo(defaultSortField, lastSortAsc);
                this.table.getColumnSortList().push(info);
            }
        }
    }

    public void setFolderData(FolderDetails details) {
        reset();
        this.details = details;
        if (details == null) {
            updateRowCount(0, true);
            return;
        }

        cachedEntries.addAll(details.getEntries());
        resultSize = (int) details.getCount();
        updateRowCount(resultSize, true);

        // retrieve the first page of results and updateRowData
        final Range range = this.table.getVisibleRange();
        final int rangeStart = 0;
        int rangeEnd = rangeStart + range.getLength();
        if (rangeEnd > resultSize)
            rangeEnd = resultSize;

        updateRowData(rangeStart, cachedEntries.subList(rangeStart, rangeEnd));
        table.setPageStart(0);
    }
}

