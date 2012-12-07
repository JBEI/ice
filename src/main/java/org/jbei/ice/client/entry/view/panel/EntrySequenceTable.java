package org.jbei.ice.client.entry.view.panel;

import java.util.ArrayList;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.util.DateUtilities;
import org.jbei.ice.shared.dto.entry.SequenceAnalysisInfo;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.CellTable.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;

/**
 * Table for displaying sequence trace files
 *
 * @author Hector Plahar
 */
public class EntrySequenceTable extends Composite {

    private final CellTable<SequenceAnalysisInfo> table;
    private final ListDataProvider<SequenceAnalysisInfo> dataProvider;
    private final EntrySequenceSelectionColumnHeader.TableSelectionModel selectionModel;
    private final SimplePager pager;

    protected interface EntryResources extends Resources {

        static EntryResources INSTANCE = GWT.create(EntryResources.class);

        @Override
        @Source("org/jbei/ice/client/resource/css/EntrySequenceTable.css")
        Style cellTableStyle();
    }

    public EntrySequenceTable() {
        table = new CellTable<SequenceAnalysisInfo>(5, EntryResources.INSTANCE);
        table.setEmptyTableWidget(new HTML("<i>No sequence trace files provided</i>"));
        VerticalPanel panel = new VerticalPanel();
        panel.setWidth("98%");

        initWidget(panel);
        table.setWidth("100%");
        dataProvider = new ListDataProvider<SequenceAnalysisInfo>();
        dataProvider.addDataDisplay(table);

        // selection model
        selectionModel = new EntrySequenceSelectionColumnHeader.TableSelectionModel();
        table.setSelectionModel(selectionModel,
                                DefaultSelectionEventManager.<SequenceAnalysisInfo>createCheckboxManager());

        // add columns
        addColumns();

        // other table props
        pager = new SimplePager();
        pager.setDisplay(table);
        pager.setStyleName("font-75em");
        pager.addStyleName("font-bold");

        panel.add(table);
        panel.add(pager);
        panel.setCellHorizontalAlignment(pager, HasAlignment.ALIGN_CENTER);
    }

    private void addColumns() {
        createSelectionColumn();
        addNameColumn();
        addDepositorColumn();
    }

    public MultiSelectionModel<SequenceAnalysisInfo> getSelectionModel() {
        return this.selectionModel;
    }

    private void createSelectionColumn() {
        final CheckboxCell columnCell = new CheckboxCell(true, false) {

            @Override
            public void onBrowserEvent(Context context, Element parent, Boolean value,
                    NativeEvent event, ValueUpdater<Boolean> valueUpdater) {
                String type = event.getType();

                boolean enterPressed = "keydown".equals(type) && event.getKeyCode() == KeyCodes.KEY_ENTER;
                if ("change".equals(type) || enterPressed) {
                    InputElement input = parent.getFirstChild().cast();
                    Boolean isChecked = input.isChecked();

                    if (!isChecked) {
                        selectionModel.setAllSelected(false);
                    }
                }
            }
        };

        Column<SequenceAnalysisInfo, Boolean> checkColumn = new Column<SequenceAnalysisInfo, Boolean>(columnCell) {

            @Override
            public Boolean getValue(SequenceAnalysisInfo object) {
                return selectionModel.isSelected(object);
            }
        };

        checkColumn.setSortable(false);
        SelectionColumnHeader header = new SelectionColumnHeader();
        table.addColumn(checkColumn, header);
        table.setColumnWidth(checkColumn, 40, Unit.PX);
    }

    protected void addNameColumn() {
        HTMLColumn<SequenceAnalysisInfo> nameColumn = new HTMLColumn<SequenceAnalysisInfo>() {

            @Override
            public SafeHtml getValue(SequenceAnalysisInfo info) {
                return SafeHtmlUtils.fromSafeConstant("<a href=\"/download?type=sequence&id=" + info.getFileId()
                                                              + "&name=" + info.getName() + "\">"
                                                              + info.getName() + "</a>");
            }
        };

        table.addColumn(nameColumn, "Trace Sequence Files");
        table.setColumnWidth(nameColumn, 70, Unit.PCT);
    }

    protected void addDepositorColumn() {
        HTMLColumn<SequenceAnalysisInfo> depositorColumn = new HTMLColumn<SequenceAnalysisInfo>() {

            @Override
            public SafeHtml getValue(SequenceAnalysisInfo info) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.appendHtmlConstant("<span class=\"font-85em\">");
                sb.appendEscaped(DateUtilities.formatDate(info.getCreated()));
                sb.appendHtmlConstant("</span><br /><span class=\"font-85em\">");
                if (info.getDepositor() == null) {
                    sb.appendHtmlConstant("</span>");
                } else {
                    String name = info.getDepositor().getFullName();
                    if (info.getDepositor().getId() <= 0)
                        sb.appendHtmlConstant("by <i>" + name + "</i></span>");
                    else {
                        String link = Page.PROFILE.getLink() + ";id=" + info.getDepositor().getId();
                        sb.appendHtmlConstant("by <a href=\"#" + link + "\">" + name + "</a></span>");
                    }
                }

                return sb.toSafeHtml();
            }
        };

        table.addColumn(depositorColumn, "Uploaded");
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
        pager.setVisible(data.size() > 0);
    }

    public void reset() {
        dataProvider.getList().clear();
        table.setRowCount(0, true);
        pager.setVisible(false);
    }

    private class SelectionColumnHeader extends Header<Boolean> {

        public SelectionColumnHeader() {
            super(new EntrySequenceSelectionColumnHeader(EntrySequenceTable.this.table, selectionModel, true, false));
        }

        @Override
        public Boolean getValue() {
            if (selectionModel.isAllSelected())
                return true;

            return !(selectionModel.getSelectedSet().isEmpty());
        }
    }
}
