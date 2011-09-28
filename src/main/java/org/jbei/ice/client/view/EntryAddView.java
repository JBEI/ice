package org.jbei.ice.client.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.jbei.ice.client.FeedbackType;
import org.jbei.ice.client.IFeedbackHandler;
import org.jbei.ice.client.common.Footer;
import org.jbei.ice.client.common.HeaderView;
import org.jbei.ice.client.common.HeaderMenu;
import org.jbei.ice.client.presenter.EntryAddPresenter;
import org.jbei.ice.client.view.form.NewArabidopsisForm;
import org.jbei.ice.client.view.form.NewEntryForm;
import org.jbei.ice.client.view.form.NewPartForm;
import org.jbei.ice.client.view.form.NewPlasmidForm;
import org.jbei.ice.client.view.form.NewStrainForm;
import org.jbei.ice.client.view.form.NewStrainWithPlasmidForm;
import org.jbei.ice.shared.AutoCompleteField;
import org.jbei.ice.shared.EntryType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EntryAddView extends Composite implements EntryAddPresenter.Display {

    private final VerticalPanel contents;
    private final FlexTable table;
    private final HashMap<EntryType, NewEntryForm> formsCache;
    private EntryType currentSelection;
    private FeedbackLabel feedbackLabel;
    private Button saveButton;
    private HashMap<AutoCompleteField, ArrayList<String>> data;

    public EntryAddView() {

        contents = new VerticalPanel();
        table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setWidth("100%");
        table.setHeight("98%");
        initWidget(table);

        formsCache = new HashMap<EntryType, NewEntryForm>();
        feedbackLabel = new FeedbackLabel();

        // select new entry type
        contents.add(newEntryWidget());
        contents.setWidth("100%");

        table.setWidget(0, 0, new HeaderView());
        table.setWidget(1, 0, new HeaderMenu());

        // contents
        table.setWidget(2, 0, contents);
        table.getCellFormatter().setHeight(2, 0, "100%");
        table.getCellFormatter().setVerticalAlignment(2, 0, HasVerticalAlignment.ALIGN_TOP);

        table.setWidget(3, 0, Footer.getInstance());

        saveButton = new Button();
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
        layout.setWidget(2, 0, feedbackLabel);

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
        NewEntryForm entryForm = formsCache.get(formType);

        // check if widget already cached
        if (entryForm == null) {
            // widget is null, create a new one
            entryForm = entryForm(formType);
            if (entryForm != null)
                formsCache.put(formType, entryForm);
        }

        if (contents.getWidgetCount() > 1) {
            contents.remove(1);
        }

        if (entryForm != null) {
            contents.add(entryForm);
            currentSelection = formType;
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
    protected NewEntryForm entryForm(EntryType type) {

        switch (type) {

        case PLASMID:
            return new NewPlasmidForm(data, saveButton);

        case STRAIN:
            return new NewStrainForm(data, saveButton);

        case PART:
            return new NewPartForm(data, saveButton);

        case STRAIN_WITH_PLASMID:
            return new NewStrainWithPlasmidForm(data, saveButton);

        case ARABIDOPSIS:
            return new NewArabidopsisForm(data, saveButton);

        default:
            //            layout.setHTML(1, 0, "Part not recognized");
            return null;
        }
    }

    @Override
    public void setAutoCompleteData(HashMap<AutoCompleteField, ArrayList<String>> data) {
        this.data = data;
    }

    @Override
    public HasClickHandlers getSubmitButton() {
        return saveButton;
    }

    private class FeedbackLabel extends HTML implements IFeedbackHandler {

        @Override
        public void setText(ArrayList<String> msgs, FeedbackType type) {

            switch (type) {
            case ERROR:
                this.setStyleName("feedback_panel_err");
                break;

            case FEEDBACK:
                break;
            }

            String html = "<ul>";
            for (String msg : msgs) {
                html += "<li>" + msg + "</li>";
            }
            html += "</ul>";
            setHTML(html);
        }
    }

    @Override
    public NewEntryForm getForm() {
        return formsCache.get(currentSelection);
    }

    @Override
    public HashMap<AutoCompleteField, ArrayList<String>> getAutoCompleteData() {
        return data;
    }
}
