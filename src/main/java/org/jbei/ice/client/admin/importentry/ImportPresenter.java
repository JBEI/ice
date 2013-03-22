package org.jbei.ice.client.admin.importentry;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.IceAsyncCallback;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.client.admin.AdminTab;
import org.jbei.ice.client.exception.AuthenticationException;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import gwtupload.client.IUploadStatus;
import gwtupload.client.IUploader;

/**
 * @author Hector Plahar
 */
public class ImportPresenter implements AdminPanelPresenter<EntryInfo> {

    private RegistryServiceAsync service;
    private HandlerManager eventBus;
    private final ImportView view;
    private String fileId;

    public ImportPresenter() {
        this.view = new ImportView();

        // start upload loader handler
        view.setStartUploaderHandler(new IUploader.OnStartUploaderHandler() {

            @Override
            public void onStart(IUploader uploader) {
                uploader.setServletPath("servlet.gupld?type=bulk_file_upload&is_sequence=false");
                fileId = null;
            }
        });

        view.setFinishUploadHandler(new IUploader.OnFinishUploaderHandler() {
            @Override
            public void onFinish(IUploader uploader) {
                if (uploader.getStatus() == IUploadStatus.Status.SUCCESS) {
                    IUploader.UploadedInfo info = uploader.getServerInfo();
                    fileId = info.message;
                    view.setUploaded(uploader.getBasename());
                }
            }
        });

        view.setSubmitHandler(new SubmitClickHandler());
    }

    @Override
    public void go(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
        view.reset();
    }

    @Override
    public AdminPanel<EntryInfo> getView() {
        return view;
    }

    @Override
    public int getTabIndex() {
        return AdminTab.IMPORT.ordinal();
    }

    private class SubmitClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            if (fileId == null || !view.validates())
                return;

            // submit data to service
            new IceAsyncCallback<Boolean>() {

                @Override
                protected void callService(AsyncCallback<Boolean> callback) throws AuthenticationException {
                    service.importXMLEntries(AppController.sessionId, fileId, view.getOwnerEmail(),
                                             view.getOwnerName(), callback);
                }

                @Override
                public void onSuccess(Boolean result) {
                    if (!result) {
                        Window.alert("Import failed!");
                        return;
                    }

                    History.newItem(Page.COLLECTIONS.getLink(), false);
                }
            }.go(eventBus);
        }
    }
}
