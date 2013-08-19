package org.jbei.ice.client.collection.add.form;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.Icon;
import org.jbei.ice.lib.shared.dto.entry.CustomField;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Panel for displaying the entry parameters. Also maintains
 * a data structure of the user entered values
 *
 * @author Hector Plahar
 */
public class CustomFieldPanel extends Composite {

    private final LinkedHashMap<Integer, Parameter> map = new LinkedHashMap<Integer, Parameter>();
    private int row;
    private final FlexTable table;
    private final Button addFieldButton;

    public CustomFieldPanel() {
        this.table = new FlexTable();
        this.table.setCellPadding(0);
        this.table.setCellSpacing(3);
        this.table.setWidth("100%");
        initWidget(table);

        addFieldButton = new Button("Add custom field");
        addFieldButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addRow();
            }
        });
    }

    public Button getFieldButton() {
        return addFieldButton;
    }

    public LinkedHashMap<Integer, Parameter> getParameterMap() {
        return new LinkedHashMap<Integer, Parameter>(map);
    }

    private void addRow() {
        Parameter param = new Parameter(row, new RemoveParameterHandler(row));
        table.setWidget(row, 0, param);
        map.put(row, param);
        row += 1;
    }

    public void setFields(ArrayList<CustomField> customFields) {
        if (customFields == null || customFields.isEmpty())
            return;

        for (CustomField field : customFields) {
            Parameter param = new Parameter(row, new RemoveParameterHandler(row));
            param.setName(field.getName());
            param.setValue(field.getValue());
            table.setWidget(row, 0, param);
            map.put(row, param);
            row += 1;
        }
    }

    public class RemoveParameterHandler implements ClickHandler {

        private final int row;

        public RemoveParameterHandler(int row) {
            this.row = row;
        }

        @Override
        public void onClick(ClickEvent event) {
            for (int i = 0; i < table.getRowCount(); i += 1) {
                Widget widget = table.getWidget(i, 0);
                if (!(widget instanceof Parameter))
                    continue;
                Parameter parameter = (Parameter) widget;
                if (parameter.getId() != row)
                    continue;

                table.removeRow(i);
                map.remove(row);
                break;
            }
        }
    }

    public class Parameter extends Composite {

        private TextBox name;
        private TextBox value;
        private FlexTable panel;
        private HTML nameLabel;
        private Icon iconRemove;
        private final int id;

        public Parameter(int id, ClickHandler handler) {
            initComponents();
            this.id = id;
            panel = new FlexTable();
            panel.setCellPadding(0);
            panel.setCellSpacing(0);

            panel.setWidget(0, 0, name);
            name.setFocus(true);
            panel.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
            panel.getFlexCellFormatter().setWidth(0, 0, "170px");

            panel.setHTML(0, 1, "&nbsp;");
            panel.getFlexCellFormatter().setWidth(0, 1, "5px");

            panel.setWidget(0, 2, value);
            panel.setHTML(0, 3, "&nbsp;");
            panel.setWidget(0, 4, iconRemove);
            iconRemove.addClickHandler(handler);

            initWidget(panel);
            initHandlers();
        }

        public Parameter(int id, String name, String value, ClickHandler handler) {
            this(id, handler);

            this.name.setText(name);
            this.value.setText(value);

            String html = "<span class=\"font-85em\" style=\"white-space:nowrap\">" + name + " <span " +
                    "class=\"required\">*</span></span>";
            nameLabel.setHTML(html);
            panel.setWidget(0, 0, nameLabel);
        }

        public int getId() {
            return this.id;
        }

        protected void initHandlers() {
            value.addFocusHandler(new FocusHandler() {
                @Override
                public void onFocus(FocusEvent event) {
                    if (name.getText().isEmpty())
                        return;

                    String nameText = name.getText();
                    String html = "<span class=\"font-85em\" style=\"white-space:nowrap\">" + nameText + " <span " +
                            "class=\"required\">*</span></span>";
                    nameLabel.setHTML(html);
                    panel.setWidget(0, 0, nameLabel);
                }
            });

            nameLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    panel.setWidget(0, 0, name);
                    name.setFocus(true);
                }
            });
        }

        protected void initComponents() {
            name = createStandardTextBox("165px");
            name.getElement().setAttribute("placeHolder", "Enter field name");
            name.setMaxLength(150);

            value = createStandardTextBox("300px");
            value.setMaxLength(500);

            iconRemove = new Icon(FAIconType.REMOVE);
            iconRemove.addStyleName("delete_icon");

            nameLabel = new HTML();
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
    }
}
