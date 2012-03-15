package org.jbei.ice.client.collection.add.form;

import java.util.LinkedHashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.TextBox;

public class ParametersPanel {

    private final LinkedHashMap<Integer, Parameter> map = new LinkedHashMap<Integer, Parameter>();
    private int row;
    private int i;
    private final FlexTable table;

    public ParametersPanel(FlexTable table, int row) {
        this.table = table;
        this.row = row;
        Parameter param = new Parameter(table, row, true);
        param.getMinus().setVisible(false);
        this.row += 1;
        addClickHandlers(param);
        i = 0;
        addToMap(param);
    }

    public LinkedHashMap<Integer, Parameter> getParameterMap() {
        return new LinkedHashMap<Integer, Parameter>(map);
    }

    private void addToMap(Parameter param) {
        map.put(i, param);
        i += 1;
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

    private void addRow() {
        Parameter param = new Parameter(table, row, false);
        addClickHandlers(param);
        addToMap(param);
        row += 1;
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

            table.setWidget(row, 0, new HTML("<span class=\"font-80em\">Name</span>"));
            table.getFlexCellFormatter().setStyleName(row, 0, "entry_add_parameter");
            name = createStandardTextBox("205px");
            table.setWidget(row, 1, name);
            table.getFlexCellFormatter().setWidth(row, 1, "220px");

            table.setWidget(row, 2, new HTML("<span class=\"font-80em\">Value</span>"));
            table.getFlexCellFormatter().setWidth(row, 2, "50px");

            value = createStandardTextBox("205px");
            table.setWidget(row, 3, value);
            table.getFlexCellFormatter().setWidth(row, 3, "140px");

            plus = new Button("+");
            minus = new Button("-");
            table.setWidget(row, 4, plus);
            if (!firstRow) {
                table.getFlexCellFormatter().setWidth(row, 4, "20px");
                table.setWidget(row, 5, minus);
            }
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

        public Button getPlus() {
            return this.plus;
        }

        public Button getMinus() {
            return this.minus;
        }
    }
}
