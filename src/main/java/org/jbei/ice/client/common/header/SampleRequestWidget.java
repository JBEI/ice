package org.jbei.ice.client.common.header;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.lib.shared.dto.entry.PartData;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 * Widget that displays summary list of pending sample requests in the header
 * and allows users to submit them
 *
 * @author Hector Plahar
 */
public class SampleRequestWidget implements IsWidget {

    private final FocusPanel parent;
    private final CellTable<SampleRequest> table;
    private final HTML badge;
    private final MultiSelectionModel<SampleRequest> model;
    private final ListDataProvider<SampleRequest> dataProvider;

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
        model = new MultiSelectionModel<SampleRequest>();
        dataProvider = new ListDataProvider<SampleRequest>();
        dataProvider.addDataDisplay(table);

        parent = new FocusPanel(badge);

        PopupHandler addToHandler = new PopupHandler(table, parent.getElement(), false);
        parent.addClickHandler(addToHandler);

        SampleRequest request = new SampleRequest();
        request.setRequestType(SampleRequestType.STREAK_ON_AGAR_PLATE);
        PartData data = new PartData();
        data.setId(12302);
        data.setPartId("JBx_1232");
        request.setPartData(data);
        dataProvider.getList().add(request);

        initTable();
    }

    protected void initTable() {
        final CheckboxCell columnCell = new CheckboxCell(true, false);

        // selection column
        Column<SampleRequest, Boolean> selectionCol = new Column<SampleRequest, Boolean>(columnCell) {

            @Override
            public Boolean getValue(SampleRequest object) {
                return model.isSelected(object);
            }
        };

        table.addColumn(selectionCol);
        table.setColumnWidth(selectionCol, "5px");

        // add part number column
        Column<SampleRequest, SafeHtml> column = new Column<SampleRequest, SafeHtml>(new SafeHtmlCell()) {

            @Override
            public SafeHtml getValue(SampleRequest object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<a href=\"" + object.getPartData().getId() + "\">" + object.getPartData()
                                                                                                  .getPartId() +
                                              "</a>");
                return sb.toSafeHtml();
            }
        };
        table.addColumn(column);
        table.setColumnWidth(column, "200px");

        // add type column

        // add delete from table column
    }

    public void setRequestCount(int count) {
        String html = "&nbsp;&nbsp;<span style=\"color: #969696\">|</span>&nbsp;&nbsp;"
                + "<i style=\"color:#666; cursor:pointer;\" class=\""
                + FAIconType.SHOPPING_CART.getStyleName() + "\"></i>"
                + "<sup class=\"badge\">" + count + "</sup></span>";
        badge.setHTML(html);
    }

    @Override
    public Widget asWidget() {
        return parent;
    }
}
