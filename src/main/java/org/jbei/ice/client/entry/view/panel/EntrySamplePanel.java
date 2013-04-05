package org.jbei.ice.client.entry.view.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.client.entry.view.panel.sample.Storage96WellPanel;
import org.jbei.ice.client.entry.view.view.CreateSampleForm;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class EntrySamplePanel extends Composite {

    private final FlexTable table;
    private CreateSampleForm sampleForm;
    private Button addSample;
    private HandlerRegistration handlerRegistration;

    public EntrySamplePanel() {
        table = new FlexTable();
        initWidget(table);
        table.setWidth("100%");

        addSample = new Button("Add Sample");

        table.setWidget(0, 0, addSample);
        table.setHTML(1, 0, "");
        table.setHTML(2, 0, "<i class=\"font-75em pad-8\">No Samples Available</i>");
    }

    public void setAddSampleHandler(ClickHandler handler) {
        if (handlerRegistration != null)
            handlerRegistration.removeHandler();
        handlerRegistration = addSample.addClickHandler(handler);
    }

    public void reset() {
        table.removeAllRows();
        table.setWidget(0, 0, addSample);
        table.setHTML(1, 0, "");
        table.setHTML(2, 0, "<i class=\"font-75em pad-8\">No Samples Available</i>");
    }

    public void setSampleOptions(SampleLocation options) {
        sampleForm = new CreateSampleForm(options);
        sampleForm.setVisible(false);
        table.setWidget(1, 0, sampleForm);
        table.getFlexCellFormatter().setColSpan(1, 0, 3);
    }

    public void addSampleSaveHandler(ClickHandler handler) {
        if (sampleForm == null)
            return;
        sampleForm.addSaveHandler(handler);
    }

    public SampleStorage getSampleAddFormValues() {
        if (sampleForm == null)
            return null;
        return sampleForm.populateSample();
    }

    public boolean getSampleFormVisibility() {
        return this.sampleForm.isVisible();
    }

    public void setSampleFormVisibility(boolean visible) {
        this.sampleForm.setVisible(visible);
    }

    public void setData(ArrayList<SampleStorage> data, ServiceDelegate<SampleInfo> deleteHandler) {
        table.removeAllRows();
        table.setWidget(0, 0, addSample);
        table.setHTML(1, 0, "");

        Collections.sort(data, new Comparator<SampleStorage>() {
            @Override
            public int compare(SampleStorage o1, SampleStorage o2) {
                return o1.getSample().getLabel().compareToIgnoreCase(o2.getSample().getLabel());
            }
        });

        int row = 2;
        int col = 0;
        for (int i = 0; i < data.size(); i += 1) {
            SampleStorage datum = data.get(i);
            Widget widget;
            if (isPlate96(datum.getStorageList())) {
                String wellName = getWell(datum.getStorageList());
                String tubeName = getTube(datum.getStorageList());
                String plateName = getPlateName(datum.getStorageList());
                widget = new Storage96WellPanel(datum, wellName, tubeName, plateName);
            } else {
                widget = new GenericStoragePanel(datum, deleteHandler);
            }

            table.setWidget(row, col, widget);
            if ((i + 1) % 2 == 0) {
                row += 1;
                col = 0;
            } else
                col += 1;
        }
    }

    public boolean isShelf(List<StorageInfo> storageInfoList) {
        for (StorageInfo info : storageInfoList) {
            if (info.getType().equalsIgnoreCase("SHELF"))  //TODO : use enums
                return true;
        }
        return false;
    }

    public boolean isFreezer(List<StorageInfo> storageInfoList) {
        for (StorageInfo info : storageInfoList) {
            if (info.getType().equalsIgnoreCase("FREEZER"))  // TODO : use enums
                return true;
        }
        return false;
    }

    public boolean isPlate81(List<StorageInfo> storageInfoList) {
        for (StorageInfo info : storageInfoList) {
            if (info.getType().equalsIgnoreCase("PLATE81"))  // TODO : use enums
                return true;
        }
        return false;
    }

    private boolean isPlate96(List<StorageInfo> storageInfoList) {
        for (StorageInfo info : storageInfoList) {
            if ("PLATE96".equalsIgnoreCase(info.getType()))  // TODO : use enums
                return true;
        }
        return false;
    }

    private String getPlateName(List<StorageInfo> storageInfoList) {
        for (StorageInfo info : storageInfoList) {
            if (info.getType().equalsIgnoreCase("PLATE96"))
                return info.getDisplay();
        }
        return "";
    }

    private String getWell(List<StorageInfo> storageInfoList) {
        for (StorageInfo info : storageInfoList) {
            if (info.getType().equalsIgnoreCase("WELL"))
                return info.getDisplay();
        }
        return "";
    }

    private String getTube(List<StorageInfo> storageInfoList) {
        for (StorageInfo info : storageInfoList) {
            if (info.getType().equalsIgnoreCase("TUBE"))
                return info.getDisplay();
        }
        return "";
    }

    private static class GenericStoragePanel extends Composite {

        private final SampleStorage storage;
        private final ServiceDelegate<SampleInfo> deleteHandler;

        public GenericStoragePanel(SampleStorage storage, ServiceDelegate<SampleInfo> deleteHandler) {
            this.storage = storage;
            this.deleteHandler = deleteHandler;

            FlexTable panel = new FlexTable();

            panel.setCellPadding(0);
            panel.setCellSpacing(0);
            panel.setStyleName("generic_storage_panel");
            initWidget(panel);
            panel.setWidth("90%");

            panel.setHTML(0, 0, storage.getSample().getLocation());
            panel.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
            panel.getFlexCellFormatter().setStyleName(0, 0, "pad-4");

            int i = 0;
            for (; i < storage.getStorageList().size(); i += 1) {
                StorageInfo info = storage.getStorageList().get(i);
                if ("SCHEME".equalsIgnoreCase(info.getType()))
                    continue;

                String html = "<span id=\"type\">" + info.getType() + "</span><br>"
                        + "<span id=\"display\">" + info.getDisplay() + "</span>";
                panel.setHTML(1, i, html);
                panel.getFlexCellFormatter().setHorizontalAlignment(1, i, HasAlignment.ALIGN_CENTER);
                panel.getFlexCellFormatter().setWidth(1, i, "100px");
            }

            panel.getFlexCellFormatter().setColSpan(0, 0, i);

            // footer
            panel.setWidget(2, 0, createFooter());
            panel.getFlexCellFormatter().setColSpan(2, 0, i);
            panel.getFlexCellFormatter().setStyleName(2, 0, "footer");
        }

        protected Widget createFooter() {
            FlexTable table = new FlexTable();
            table.setCellPadding(0);
            table.setCellSpacing(0);
            table.setWidth("100%");

            table.setHTML(0, 0, storage.getSample().getLabel());
            if (ClientController.account.isAdmin() ||
                    ClientController.account.getEmail().equalsIgnoreCase(storage.getSample().getDepositor())) {
                HTML label = new HTML("<i class=\"" + FAIconType.TRASH.getStyleName() + "\"></i> Delete");
                label.setStyleName("footer_feedback_widget");
                label.addStyleName("font-70em");

                if (deleteHandler != null) {
                    label.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            if (Window.confirm("Confirm deletion of sample \"" + storage.getSample().getLabel() + "\""))
                                deleteHandler.execute(storage.getSample());
                        }
                    });
                }

                table.setWidget(0, 1, label);
                table.getFlexCellFormatter().setWidth(0, 1, "50px");
            }

            return table;
        }
    }
}
