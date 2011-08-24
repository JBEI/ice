package org.jbei.ice.client.view;

import java.util.HashMap;

import org.jbei.ice.client.panel.Footer;
import org.jbei.ice.client.panel.Header;
import org.jbei.ice.client.panel.HeaderMenu;
import org.jbei.ice.client.presenter.EntryAddPresenter;
import org.jbei.ice.client.view.form.NewArabidopsisForm;
import org.jbei.ice.client.view.form.NewPartForm;
import org.jbei.ice.client.view.form.NewPlasmidForm;
import org.jbei.ice.client.view.form.NewStrainForm;
import org.jbei.ice.client.view.form.NewStrainWithPlasmidForm;
import org.jbei.ice.shared.EntryType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntryAddView extends Composite implements EntryAddPresenter.Display {

    private final VerticalPanel contents;
    private final FlexTable table;
    private final HashMap<EntryType, Widget> forms;

    public EntryAddView() {

        contents = new VerticalPanel();
        table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");
        table.setHeight("98%");
        initWidget(table);

        forms = new HashMap<EntryType, Widget>();

        // select new entry type
        contents.add(newEntryWidget());
        contents.setWidth("100%");

        table.setWidget(0, 0, new Header());
        table.setWidget(1, 0, new HeaderMenu());

        // contents
        table.setWidget(2, 0, contents);
        table.getCellFormatter().setHeight(2, 0, "100%");
        table.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);

        table.setWidget(3, 0, Footer.getInstance());
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    protected Widget newEntryWidget() {

        FlexTable layout = new FlexTable();
        layout.setWidth("800px");
        layout.addStyleName("gray_border");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        layout.setHTML(0, 0, "Select New Entry Type");
        layout.getCellFormatter().setStyleName(0, 0, "collections_header");

        layout.setWidget(1, 0, entryOptions());

        return layout;
    }

    private Widget entryOptions() {

        VerticalPanel panel = new VerticalPanel();
        for (final EntryType type : EntryType.values()) {
            RadioButton radio = new RadioButton("Entries", type.getDisplay());
            radio.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    switchForm(type);
                }
            });
            panel.add(radio);
        }

        return panel;
    }

    protected void switchForm(EntryType formType) {
        Widget widget = forms.get(formType);

        // check if widget already cached
        if (widget == null) {
            // widget is null, create a new one
            widget = entryForm(formType);
            if (widget != null)
                forms.put(formType, widget);
        }

        if (contents.getWidgetCount() > 1) {
            contents.remove(1);
        }

        if (widget != null) {
            contents.add(widget);
        }
    }

    /**
     * creates a new form based on specific types of entries.
     * To create a new entry/form, add the type to {@link EntryType} and create a new form here
     * 
     * @param type
     *            EntryType
     * @return form specific to type
     */
    protected Widget entryForm(EntryType type) {

        switch (type) {

        case PLASMID:
            return new NewPlasmidForm();

        case STRAIN:
            return new NewStrainForm();

        case PART:
            return new NewPartForm();

        case STRAIN_WITH_PLASMID:
            return new NewStrainWithPlasmidForm();

        case ARABIDOPSIS:
            return new NewArabidopsisForm();

        default:
            //            layout.setHTML(1, 0, "Part not recognized");
            return null;
        }
    }
}
