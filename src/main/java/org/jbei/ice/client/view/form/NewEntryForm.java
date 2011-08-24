package org.jbei.ice.client.view.form;

import com.google.gwt.user.client.ui.CellPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class NewEntryForm extends Composite {

    protected final FlexTable layout;

    public NewEntryForm() {
        layout = new FlexTable();
        initWidget(layout);
    }

    public Widget createTextFieldWithHelpText(int width, String helpText) {

        HorizontalPanel panel = new HorizontalPanel();
        TextBox input = new TextBox();
        input.setStyleName("inputbox");
        panel.add(input);
        Label help = new Label(helpText);
        help.setStyleName("help_text");
        panel.add(help);
        return panel;
    }

    public Widget createWidgetWithHelpText(Widget widget, String helpText, boolean horizontal) {

        CellPanel panel;
        if (horizontal)
            panel = new HorizontalPanel();
        else
            panel = new VerticalPanel();

        panel.add(widget);
        Label help = new Label(helpText);
        help.setStyleName("help_text");
        panel.add(help);

        return panel;
    }

}
