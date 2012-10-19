package org.jbei.ice.client.collection.add;

import org.jbei.ice.client.collection.add.form.IEntryFormSubmit;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntryAddView extends Composite {
    private Label contentHeader;
    private IEntryFormSubmit currentForm;
    private VerticalPanel subContent;

    public EntryAddView() {
        FlexTable layout = new FlexTable();
        layout.setWidth("100%");
        layout.setHeight("100%");
        layout.setCellSpacing(0);
        layout.setCellPadding(0);
        initWidget(layout);
        layout.setWidget(0, 0, createMainContent());
    }

    protected Widget createMainContent() {
        subContent = new VerticalPanel();
        subContent.setWidth("100%");
        contentHeader = new Label("Add New Entry");

        FlexTable mainContent = new FlexTable();
        mainContent.setCellPadding(3);
        mainContent.setWidth("100%");
        mainContent.setCellSpacing(0);
        mainContent.addStyleName("add_new_entry_main_content_wrapper");
        mainContent.setWidget(0, 0, contentHeader);
        mainContent.getCellFormatter().setStyleName(0, 0, "add_new_entry_main_content_header");

        // sub content
        mainContent.setWidget(1, 0, subContent);
        mainContent.getFlexCellFormatter().setStyleName(1, 0, "add_new_entry_sub_content");
        mainContent.getFlexCellFormatter().setColSpan(1, 0, 2);

        return mainContent;
    }

    public void setCurrentForm(IEntryFormSubmit form, String title) {
        this.currentForm = form;
        subContent.clear();
        subContent.add(this.currentForm.asWidget());
        contentHeader.setText(title);
    }

    public IEntryFormSubmit getCurrentForm() {
        return this.currentForm;
    }

    public void setSubmitEnable(boolean b) {
        this.currentForm.getSubmit().setEnabled(b);
    }
}
