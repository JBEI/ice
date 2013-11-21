package org.jbei.ice.client.admin.sample;

import java.sql.Date;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.sample.SampleRequest;
import org.jbei.ice.lib.shared.dto.sample.SampleRequestType;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Hector Plahar
 */
public class SampleRequestTable extends CellTable<SampleRequest> {

    interface SelectionResource extends CellTable.Resources {

        static SelectionResource INSTANCE = GWT.create(SelectionResource.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/MessageDataTable.css")
        CellTable.Style cellTableStyle();
    }

    public SampleRequestTable(Delegate<SampleRequest> delegate) {
        super(30, SelectionResource.INSTANCE);
        Label empty = new Label();
        empty.setText("No sample request data available");
        empty.setStyleName("no_data_style");
        this.setEmptyTableWidget(empty);

        // columns
        createRequesterColumn();
        addEntryTable();
        createTypeColumn();
        addRequestedColumn();
        addUpdatedColumn();
        addActionColumn(delegate);
    }

    private void createRequesterColumn() {
        Column<SampleRequest, SafeHtml> column = new Column<SampleRequest, SafeHtml>(new SafeHtmlCell()) {

            @Override
            public SafeHtml getValue(SampleRequest object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<a href=\"#" + Page.PROFILE.getLink() + ";id=" + object.getRequester().getId()
                                              + "\">" + object.getRequester().getFullName() + "</a>");
                return sb.toSafeHtml();
            }
        };
        addColumn(column, "Requester");
        setColumnWidth(column, "200px");
    }

    private void addEntryTable() {
        Column<SampleRequest, SafeHtml> column = new Column<SampleRequest, SafeHtml>(new SafeHtmlCell()) {

            @Override
            public SafeHtml getValue(SampleRequest object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<a href=\"#" + Page.ENTRY_VIEW.getLink() + ";id=" + object.getPartData().getId()
                                              + "\">" + object.getPartData().getPartId() + "</a>");
                return sb.toSafeHtml();
            }
        };
        addColumn(column, "Part");
        setColumnWidth(column, "100px");
    }

    private void createTypeColumn() {
        Column<SampleRequest, SafeHtml> column = new Column<SampleRequest, SafeHtml>(new SafeHtmlCell()) {

            @Override
            public SafeHtml getValue(SampleRequest object) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                if (object.getRequestType() == SampleRequestType.LIQUID_CULTURE) {
                    sb.appendHtmlConstant("&nbsp;<i class=\"" + FAIconType.FLASK.getStyleName() + "\"></i> ");
                } else {
                    sb.appendHtmlConstant("&nbsp;<i class=\"" + FAIconType.CIRCLE_ALT.getStyleName() + "\"></i> ");
                }
                sb.appendEscaped(object.getRequestType().toString());
                return sb.toSafeHtml();
            }
        };
        addColumn(column, "Type");
        setColumnWidth(column, "220px");
    }

    private void addRequestedColumn() {
        Column<SampleRequest, String> column = new Column<SampleRequest, String>(new TextCell()) {

            @Override
            public String getValue(SampleRequest object) {
                DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMM d, yyyy h:mm a");
                return dateTimeFormat.format(new Date(object.getRequestTime()));
            }
        };
        addColumn(column, "Requested");
    }

    private void addUpdatedColumn() {
        Column<SampleRequest, String> column = new Column<SampleRequest, String>(new TextCell()) {

            @Override
            public String getValue(SampleRequest object) {
                DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat("MMM d, yyyy h:mm a");
                return dateTimeFormat.format(new Date(object.getUpdateTime()));
            }
        };
        addColumn(column, "Updated");
    }

    private void addActionColumn(Delegate<SampleRequest> delegate) {
        SampleStatusChangeCell cell = new SampleStatusChangeCell(delegate);
        Column<SampleRequest, SampleRequest> deleteColumn = new Column<SampleRequest, SampleRequest>(cell) {

            @Override
            public SampleRequest getValue(SampleRequest object) {
                return object;
            }
        };
        addColumn(deleteColumn, "");
        setColumnWidth(deleteColumn, "25px");
    }
}
