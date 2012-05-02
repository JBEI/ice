package org.jbei.ice.client.entry.view.table;

import java.util.ArrayList;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.SequenceAnalysisInfo;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Table for displaying sequence trace files
 * 
 * @author Hector Plahar
 */
public class EntrySequenceTable extends Composite {
    private CellTable<SequenceAnalysisInfo> table;
    private final boolean showAdminFeature = false; // TODO 
    private final ListDataProvider<SequenceAnalysisInfo> dataProvider;
    private final SimplePager pager;

    private final VerticalPanel panel;

    protected interface EntryResources extends Resources {

        static EntryResources INSTANCE = GWT.create(EntryResources.class);

        /**
         * The styles used in this widget.
         */
        @Override
        @Source("org/jbei/ice/client/resource/css/EntrySequenceTable.css")
        Style cellTableStyle();
    }

    public EntrySequenceTable() {
        table = new CellTable<SequenceAnalysisInfo>(5, EntryResources.INSTANCE);
        Widget emptyWidget = createEmptyTableWidget();
        table.setEmptyTableWidget(emptyWidget);
        panel = new VerticalPanel();
        panel.setWidth("100%");

        initWidget(panel);
        table.setWidth("100%");
        dataProvider = new ListDataProvider<SequenceAnalysisInfo>();
        dataProvider.addDataDisplay(table);

        // add columns
        addNameColumn();
        addDepositorColumn();

        // other table props
        pager = new SimplePager();
        pager.setDisplay(table);
        pager.setStyleName("font-75em");

        panel.add(table);
        panel.add(pager);
        panel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);
    }

    protected void addNameColumn() {
        HTMLColumn<SequenceAnalysisInfo> nameColumn = new HTMLColumn<SequenceAnalysisInfo>() {

            @Override
            public SafeHtml getValue(SequenceAnalysisInfo info) {
                return SafeHtmlUtils.fromSafeConstant("<a href=\"/download?type=sequence&id="
                        + info.getFileId() + "&name=" + info.getName() + "\">" + info.getName()
                        + "</a>");
            }
        };

        table.addColumn(nameColumn);
        table.setColumnWidth(nameColumn, 70, Unit.PCT);
    }

    protected void addDepositorColumn() {
        HTMLColumn<SequenceAnalysisInfo> depositorColumn = new HTMLColumn<SequenceAnalysisInfo>() {

            @Override
            public SafeHtml getValue(SequenceAnalysisInfo info) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<span>");
                sb.appendEscaped(DateUtilities.formatDate(info.getCreated()));
                sb.appendHtmlConstant("</span><br /><span>");
                if (info.getDepositor() == null) {
                    sb.appendHtmlConstant("</span>");
                } else {
                    String name = info.getDepositor().getFullName();
                    String email = info.getDepositor().getEmail();
                    if (info.getDepositor().getLastName() == null || name.trim().isEmpty())
                        name = email;
                    Hyperlink link = new Hyperlink(name, Page.PROFILE.getLink() + ";id=" + email);
                    link.setStyleName("display-inline");
                    sb.appendHtmlConstant("by " + link.toString() + "</span>");
                }

                return sb.toSafeHtml();
            }
        };

        table.addColumn(depositorColumn);
    }

    private abstract class HTMLColumn<T> extends Column<T, SafeHtml> {

        /**
         * Construct a new TextColumn.
         */
        public HTMLColumn() {
            super(new SafeHtmlCell());
        }
    }

    public void setData(ArrayList<SequenceAnalysisInfo> data) {
        dataProvider.setList(data);
        table.setRowCount(data.size(), true);
        if (data.size() == 0)
            pager.setVisible(false);
    }

    /**
     * @return widget that is display when the table contains no data
     */
    private Widget createEmptyTableWidget() {
        HTML widget = new HTML("<br><br>No sequence trace files provided.");
        widget.setStyleName("no_data_style");
        return widget;
    }
}
