package org.jbei.ice.client.entry.display.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jbei.ice.client.Callback;
import org.jbei.ice.client.ClientController;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.common.header.HeaderView;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.GenericPopup;
import org.jbei.ice.client.common.widget.ICanReset;
import org.jbei.ice.client.entry.display.model.SampleStorage;
import org.jbei.ice.client.entry.display.panel.sample.Storage96WellPanel;
import org.jbei.ice.client.entry.display.view.CreateSampleForm;
import org.jbei.ice.lib.shared.dto.PartSample;
import org.jbei.ice.lib.shared.dto.StorageInfo;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for displaying sample information
 *
 * @author Hector Plahar
 */
public class EntrySamplePanel extends Composite {

    private final FlexTable table;
    private CreateSampleForm sampleForm;
    private final Button addSample;
    private final Button requestSample;
    private HandlerRegistration handlerRegistration;
    private final HTMLPanel panel;
    private Delegate<SampleRequestType> requestDelegate;
    private Delegate<SampleRequestType> removeRequestDelegate;
    private boolean isRequest;
    private GenericPopup requestDialog;
    private Callback<SampleRequest> callback;

    public EntrySamplePanel() {
        table = new FlexTable();
        initWidget(table);
        table.setWidth("100%");

        addSample = new Button("<i class=\"" + FAIconType.PLUS_CIRCLE.getStyleName() + "\"></i> Create Sample");
        addSample.setVisible(false);
        requestSample = new Button("<i class=\"" + FAIconType.SHOPPING_CART.getStyleName() + "\"></i> Add to Cart");
        panel = new HTMLPanel("<span id=\"add_sample\"></span>&nbsp;<span id=\"request_sample\"></span>");
        panel.add(addSample, "add_sample");
        panel.add(requestSample, "request_sample");

        createCallback();
        createRequestDialog();
        reset();
        setAddRequestSampleHandler();
    }

    protected void createCallback() {
        callback = new Callback<SampleRequest>() {
            @Override
            public void onSuccess(SampleRequest request) {
                if (isRequest) {
                    requestSample.setHTML("<i class=\"" + FAIconType.BAN.getStyleName()
                                                  + " red\"></i> Remove from Cart");
                } else
                    requestSample.setHTML("<i class=\"" + FAIconType.SHOPPING_CART.getStyleName()
                                                  + "\"></i> Add to Cart");
                isRequest = !isRequest;
            }

            @Override
            public void onFailure() {
            }
        };
    }

    public void setAddSampleHandler(ClickHandler handler) {
        if (handlerRegistration != null)
            handlerRegistration.removeHandler();
        handlerRegistration = addSample.addClickHandler(handler);
        addSample.setVisible(true);
    }

    protected void createRequestDialog() {
        final SampleRequestOptionDialogWidget widget = new SampleRequestOptionDialogWidget();
        requestDialog = new GenericPopup(widget, "<b>Request Sample in the form of</b>", "400px", "Add");
        requestDialog.addSaveButtonHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                SampleRequestType type = SampleRequestType.STREAK_ON_AGAR_PLATE;
                if (widget.isCultureRequested())
                    type = SampleRequestType.LIQUID_CULTURE;

                requestDelegate.execute(type);
                requestDialog.hideDialog();
            }
        });
    }

    private void setAddRequestSampleHandler() {
        requestSample.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (isRequest)
                    requestDialog.showDialog();
                else {
                    removeRequestDelegate.execute(null);
                }
            }
        });
    }

    public void setSampleRequestDelegates(Delegate<SampleRequestType> requestDelegate,
            Delegate<SampleRequestType> removeSampleRequestDelegate) {
        this.requestDelegate = requestDelegate;
        this.removeRequestDelegate = removeSampleRequestDelegate;
    }

    public void reset() {
        isRequest = true;
        table.removeAllRows();
        table.setWidget(0, 0, panel);
        table.getFlexCellFormatter().setStyleName(0, 0, "pad_top");

        table.setHTML(1, 0, "");
        table.setHTML(2, 0, "<i class=\"font-75em pad-top\" style=\"color: #999\">No samples available</i>");
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

    public void setData(long entryId, ArrayList<SampleStorage> data, ServiceDelegate<PartSample> deleteHandler) {
        table.removeAllRows();
        table.setWidget(0, 0, panel);
        table.getFlexCellFormatter().setStyleName(0, 0, "pad_top");

        table.setHTML(1, 0, "");
        requestSample.setVisible(!data.isEmpty());
        if (data.isEmpty()) {
            table.setHTML(2, 0, "<i class=\"font-75em pad-top\" style=\"color: #999\">No samples available</i>");
            return;
        }

        // sample data available
        boolean isInCart = HeaderView.getInstance().isInCart(entryId);
        isRequest = !isInCart;
        if (isInCart) {
            requestSample.setHTML("<i class=\"" + FAIconType.BAN.getStyleName() + " red\"></i> Remove from Cart");
        } else
            requestSample.setHTML("<i class=\"" + FAIconType.SHOPPING_CART.getStyleName() + "\"></i> Add to Cart");

        Collections.sort(data, new Comparator<SampleStorage>() {
            @Override
            public int compare(SampleStorage o1, SampleStorage o2) {
                return o1.getPartSample().getLabel().compareToIgnoreCase(o2.getPartSample().getLabel());
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

    public void setAddSampleVisibility(boolean visible) {
        addSample.setVisible(visible);
    }

    public void setAddToCartVisibility(boolean visibility) {
        this.requestSample.setVisible(visibility);
    }

    public Callback<SampleRequest> getCallback() {
        return callback;
    }

    //
    // inner classes
    //
    private static class SampleRequestOptionDialogWidget extends Composite implements ICanReset {

        private RadioButton culture;
        private RadioButton streak;

        public SampleRequestOptionDialogWidget() {
            VerticalPanel panel = new VerticalPanel();
            initWidget(panel);
            panel.setStyleName("font-80em");
            panel.setWidth("350px");
            culture = new RadioButton("sample", "Liquid Culture");
            culture.setValue(true);
            streak = new RadioButton("sample", "Streak on Agar Plate");
            panel.add(culture);
            panel.add(streak);
        }

        @Override
        public void reset() {
            culture.setValue(true);
        }

        public boolean isCultureRequested() {
            return this.culture.getValue();
        }
    }

    private static class GenericStoragePanel extends Composite {

        private final SampleStorage storage;
        private final ServiceDelegate<PartSample> deleteHandler;

        public GenericStoragePanel(SampleStorage storage, ServiceDelegate<PartSample> deleteHandler) {
            this.storage = storage;
            this.deleteHandler = deleteHandler;

            FlexTable panel = new FlexTable();

            panel.setCellPadding(0);
            panel.setCellSpacing(0);
            panel.setStyleName("generic_storage_panel");
            initWidget(panel);
            panel.setWidth("90%");

            panel.setHTML(0, 0, storage.getPartSample().getLocation());
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

            table.setHTML(0, 0, storage.getPartSample().getLabel());
            if (ClientController.account.isAdmin() ||
                    ClientController.account.getEmail().equalsIgnoreCase(storage.getPartSample().getDepositor())) {
                HTML label = new HTML("<i class=\"" + FAIconType.TRASH.getStyleName() + "\"></i> Delete");
                label.setStyleName("footer_feedback_widget");
                label.addStyleName("font-70em");

                if (deleteHandler != null) {
                    label.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            if (Window.confirm(
                                    "Confirm deletion of sample \"" + storage.getPartSample().getLabel() + "\""))
                                deleteHandler.execute(storage.getPartSample());
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
