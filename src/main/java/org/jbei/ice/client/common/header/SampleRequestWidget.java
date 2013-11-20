package org.jbei.ice.client.common.header;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.ServiceDelegate;
import org.jbei.ice.client.admin.group.DeleteActionCell;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Widget that displays summary list of pending sample requests in the header
 * and allows users to submit them
 *
 * @author Hector Plahar
 */
public class SampleRequestWidget implements IsWidget {

    private final CellTable<SampleRequest> table;
    private final HTML badge;
    private final Button submitButton;
    private HandlerRegistration buttonRegistration;
    private ListDataProvider<SampleRequest> dataProvider;
    private PopupHandler addToHandler;

    interface SelectionResource extends CellTable.Resources {

        static SelectionResource INSTANCE = GWT.create(SelectionResource.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/PermissionSelection.css")
        CellTable.Style cellTableStyle();
    }

    public SampleRequestWidget() {
        badge = new HTML();
        setRequestCount(0);

        table = new CellTable<SampleRequest>(30, SelectionResource.INSTANCE);
        Label empty = new Label();
        empty.setText("No samples have been added to cart");
        empty.setStyleName("no_data_style");

        table.setEmptyTableWidget(empty);
        dataProvider = new ListDataProvider<SampleRequest>();
        dataProvider.addDataDisplay(table);

        FlexTable popupWidget = new FlexTable();
        popupWidget.setStyleName("bg_fc");
        popupWidget.setWidget(0, 0, table);

        // add submit button
        submitButton = new Button("Submit");
        popupWidget.setWidget(1, 0, submitButton);
        submitButton.setWidth("128px");
        submitButton.setEnabled(false);

        addToHandler = new PopupHandler(popupWidget, badge.getElement(), false);
        badge.addClickHandler(addToHandler);
        initTable();
    }

    protected void initTable() {
        // add part number column
        Column<SampleRequest, SafeHtml> column = new Column<SampleRequest, SafeHtml>(new SafeHtmlCell()) {

            @Override
            public SafeHtml getValue(SampleRequest object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<a href=\"#" + Page.ENTRY_VIEW.getLink() + ";id=" + object.getPartData().getId()
                                              + "\">" + object.getPartData().getPartId() + "</a>");
                if (object.getRequestType() == SampleRequestType.LIQUID_CULTURE) {
                    sb.appendHtmlConstant(
                            "&nbsp;<i class=\"" + FAIconType.FLASK.getStyleName() + " display-inline\"></i>");
                } else {
                    sb.appendHtmlConstant(
                            "&nbsp;<i class=\"" + FAIconType.CIRCLE_ALT.getStyleName() + " display-inline\"></i>");
                }
                return sb.toSafeHtml();
            }
        };
        table.addColumn(column);
        table.setColumnWidth(column, "100px");
    }

    public void setDeleteSampleDelegate(ServiceDelegate<SampleRequest> delegate) {
        if (table.getColumnCount() > 1)
            return;

        DeleteActionCell<SampleRequest> cell = new DeleteActionCell<SampleRequest>(delegate);
        Column<SampleRequest, SampleRequest> deleteColumn = new Column<SampleRequest, SampleRequest>(cell) {

            @Override
            public SampleRequest getValue(SampleRequest object) {
                return object;
            }
        };
        table.addColumn(deleteColumn, "");
        table.setColumnWidth(deleteColumn, "25px");
    }

    public void setSubmitRequestsDelegate(final ServiceDelegate<ArrayList<SampleRequest>> requestsDelegate) {
        if (buttonRegistration != null)
            buttonRegistration.removeHandler();

        buttonRegistration = submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                requestsDelegate.execute(new ArrayList<SampleRequest>(dataProvider.getList()));
                addToHandler.hidePopup();
            }
        });
    }

    public void setRequestCount(int count) {
        String html = "&nbsp;&nbsp;<span style=\"color: #969696\">|</span>&nbsp;&nbsp;"
                + "<i style=\"color:#666; cursor:pointer;\" class=\""
                + FAIconType.SHOPPING_CART.getStyleName() + "\"></i>"
                + "<sup class=\"badge\">" + count + "</sup></span>";
        badge.setHTML(html);
        if (submitButton != null)
            submitButton.setEnabled(dataProvider.getList().size() > 0);
    }

    public int getItemCount() {
        return dataProvider.getList().size();
    }

    public boolean isInCart(long entryId) {
        List<SampleRequest> list = this.dataProvider.getList();
        if (list == null || list.isEmpty())
            return false;

        for (SampleRequest request : list) {
            if (request.getPartData().getId() == entryId)
                return true;
        }
        return false;
    }

    public int removeFromCart(SampleRequest request) {
        for (Iterator<SampleRequest> iterator = dataProvider.getList().iterator(); iterator.hasNext(); ) {
            SampleRequest listRequest = iterator.next();
            if (listRequest.getPartData().getId() == request.getPartData().getId()) {
                iterator.remove();
                break;
            }
        }
        int count = getItemCount();
        if (count == 0)
            addToHandler.hidePopup();
        return count;
    }

    public int addToCart(SampleRequest request) {
        this.dataProvider.getList().add(request);
        return getItemCount();
    }

    public void reset() {
        dataProvider.getList().clear();
    }

    public int setData(ArrayList<SampleRequest> sampleRequestData) {
        dataProvider.getList().clear();
        if (sampleRequestData == null)
            return 0;
        dataProvider.getList().addAll(sampleRequestData);
        return getItemCount();
    }

    @Override
    public Widget asWidget() {
        return badge;
    }
}
