package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.ParameterInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Panel for displaying the entry parameters. Also maintains
 * a data structure of the user entered values
 *
 * @author Hector Plahar
 */
public class ParametersPanel {

    private final LinkedHashMap<Integer, Parameter> map = new LinkedHashMap<Integer, Parameter>();
    private int row;
    private int i;
    private final FlexTable table;

    public ParametersPanel(FlexTable table, int row) {
        this.table = table;
        this.row = row;
        Parameter param = new Parameter(table, row, true);
        this.row += 1;
        addClickHandlers(param);
        i = 0;
        addToMap(param);
    }

    public ParametersPanel(FlexTable table, int row, ArrayList<ParameterInfo> parameters) {
        this.table = table;
        this.row = row;

        i = -1;
        for (ParameterInfo paramInfo : parameters) {
            Parameter param = addRow();
            param.setName(paramInfo.getName());
            param.setValue(paramInfo.getValue());
        }
    }

    public LinkedHashMap<Integer, Parameter> getParameterMap() {
        return new LinkedHashMap<Integer, Parameter>(map);
    }

    private void addToMap(Parameter param) {
        map.put(i, param);
    }

    private void addClickHandlers(Parameter param) {
        param.getPlus().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addRow();
            }
        });

        param.getMinus().addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Cell cell = table.getCellForEvent(event);
                removeRow(cell.getRowIndex());
            }
        });
    }

    private Parameter addRow() {
        if (i < 0)
            i = 0;
        else
            i += 1;
        Parameter param = new Parameter(table, row, (i == 0));
        addClickHandlers(param);
        addToMap(param);
        row += 1;
        return param;
    }

    private void removeRow(int clickRow) {
        int toRemove = row - clickRow;
        map.remove(toRemove);
        table.removeRow(clickRow);
        row -= 1;
    }

    public class Parameter {

        private final TextBox name;
        private final TextBox value;
        private final Button plus;
        private final Button minus;

        public Parameter(FlexTable table, int row, boolean firstRow) {

            // name label
            table.setWidget(row, 0, new HTML("<span class=\"font-85em\">Name</span>"));
            table.getFlexCellFormatter().setStyleName(row, 0, "entry_add_parameter");

            // input box for name
            name = createStandardTextBox("205px");
            table.setWidget(row, 1, name);
            table.getFlexCellFormatter().setWidth(row, 1, "220px");

            // value label
            table.setWidget(row, 2, new HTML("<span class=\"font-85em\">Value</span>"));
            table.getFlexCellFormatter().setWidth(row, 2, "50px");

            // input box for value
            value = createStandardTextBox("205px");
            table.setWidget(row, 3, value);
            table.getFlexCellFormatter().setWidth(row, 3, "140px");

            // plus, minus buttons for adding and removing rows. first row does not have the minus button
            plus = new Button("<i class=\"" + FAIconType.PLUS.getStyleName() + "\"></i>");
            minus = new Button("<i class=\"" + FAIconType.MINUS.getStyleName() + "\"></i>");
            table.setWidget(row, 4, plus);
            if (!firstRow) {
                table.getFlexCellFormatter().setWidth(row, 4, "20px");
                table.setWidget(row, 5, minus);
            } else
                table.getFlexCellFormatter().setColSpan(row, 4, 2);
        }

        public Parameter(FlexTable table, int row, boolean firstRow, String name, String value) {
            this(table, row, firstRow);
            this.name.setText(name);
            this.value.setText(value);
        }

        protected TextBox createStandardTextBox(String width) {
            final TextBox box = new TextBox();
            box.setStyleName("input_box");
            box.setWidth(width);
            return box;
        }

        public String getName() {
            return this.name.getText();
        }

        public TextBox getNameBox() {
            return this.name;
        }

        public TextBox getValueBox() {
            return this.value;
        }

        public String getValue() {
            return this.value.getText();
        }

        public void setValue(String value) {
            this.value.setText(value);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public Button getPlus() {
            return this.plus;
        }

        public Button getMinus() {
            return this.minus;
        }
    }
}
