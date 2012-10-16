package org.jbei.ice.client.admin.export;

import java.util.Arrays;
import java.util.HashSet;

import org.jbei.ice.client.RegistryServiceAsync;
import org.jbei.ice.client.admin.AdminPanel;
import org.jbei.ice.client.admin.AdminPanelPresenter;
import org.jbei.ice.shared.dto.EntryInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;

/**
 * @author Hector Plahar
 */
public class ExportPresenter implements AdminPanelPresenter<EntryInfo> {

    private final ExportView view;
    private RegistryServiceAsync service;
    private HandlerManager eventBus;

    public ExportPresenter() {
        view = new ExportView();
        view.setExportHandler(new ExportClickHandler());
    }

    @Override
    public void go(RegistryServiceAsync service, HandlerManager eventBus) {
        this.service = service;
        this.eventBus = eventBus;
    }

    @Override
    public AdminPanel<EntryInfo> getView() {
        return view;
    }

    @Override
    public int getTabIndex() {
        return view.getTab().ordinal();
    }

    private class ExportClickHandler implements ClickHandler {

        @Override
        public void onClick(ClickEvent event) {
            String exportList = view.getIdList();
            HashSet<String> startingSet = new HashSet<String>();
            String[] commaSepList = exportList.split(",");
            for (String item : commaSepList) {
                String[] whitespaceSepList = item.trim().split("\\s+");
                startingSet.addAll(Arrays.asList(whitespaceSepList));
            }

            StringBuilder builder = new StringBuilder();
            for (String number : startingSet) {
                builder.append(number + ", ");
            }

            Window.Location.replace("/export?type=xml&entries=" + builder.toString());
        }
    }
}
