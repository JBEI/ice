package org.jbei.ice.client.bulkupload.widget;

import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.common.util.ImageUtil;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.shared.dto.SampleInfo;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author Hector Plahar
 */
public class SampleSelectionWidget extends Composite {

    private final Image sampleImg;
    private final PopupHandler popup;
    private final CellList<SampleInfo> options;
    private SampleLocation currentLocation;
    private final SingleSelectionModel<SampleInfo> optionSelection;

    interface SampleResource extends CellList.Resources {

        static SampleResource INSTANCE = GWT.create(SampleResource.class);

        @Source("org/jbei/ice/client/resource/css/SampleSelectionWidget.css")
        CellList.Style cellListStyle();
    }

    public SampleSelectionWidget() {
        SampleResource.INSTANCE.cellListStyle().ensureInjected();
        sampleImg = ImageUtil.getSampleIcon();
        sampleImg.setHeight("14px");
        sampleImg.setTitle("Add sample locations");
        sampleImg.setAltText("Add sample locations");
        initWidget(sampleImg);

        this.setVisible(false);

        options = new CellList<SampleInfo>(new AbstractCell<SampleInfo>() {

            @Override
            public void render(Context context, SampleInfo value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>" + value.getLocation() + "</span>");
            }
        }, SampleResource.INSTANCE);

        popup = new PopupHandler(options, sampleImg.getElement(), false);
        sampleImg.addClickHandler(popup);
        // TODO : set empty Widget for options

        optionSelection = new SingleSelectionModel<SampleInfo>();
        optionSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                popup.hidePopup();
                String location = optionSelection.getSelectedObject().getLocation();
                sampleImg.setTitle(location);
                sampleImg.setAltText(location);
            }
        });

        options.setSelectionModel(optionSelection);
    }

    public void setLocation(SampleLocation location) {
        currentLocation = location;
        this.setVisible(location != null);
        if (location == null)
            return;

        options.setRowData(location.getLocations());
    }

    public SingleSelectionModel<SampleInfo> getSelectionModel() {
        return this.optionSelection;
    }

    public SampleLocation getCurrentLocation() {
        return this.currentLocation;
    }
}
