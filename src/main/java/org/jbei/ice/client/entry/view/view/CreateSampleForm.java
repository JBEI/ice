package org.jbei.ice.client.entry.view.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.jbei.ice.client.AppController;
import org.jbei.ice.client.collection.add.form.SampleLocation;
import org.jbei.ice.client.entry.view.model.SampleStorage;
import org.jbei.ice.shared.dto.SampleInfo;
import org.jbei.ice.shared.dto.StorageInfo;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CreateSampleForm extends Composite {

    private final FlexTable table;
    private TextBox sampleLabel;
    private TextArea sampleNotes;
    private TextBox depositor;
    private final Button save;
    private HandlerRegistration saveHandlerRegistration;
    private final SampleLocation sampleLocation;
    private final ListBox locationOptions;
    private final ArrayList<TextBox> sampleLocationScheme; // list of boxes representing scheme awaiting user input
    // for each selection

    public CreateSampleForm(SampleLocation options) {
        table = new FlexTable();
        table.setWidth("100%");
        table.setStyleName("bg_f8");
        table.addStyleName("font-85em");
        table.addStyleName("pad-6");
        initWidget(table);
        sampleLocationScheme = new ArrayList<TextBox>();

        addFirstColumn();

        locationOptions = new ListBox();
        locationOptions.setStyleName("pull_down");
        locationOptions.setVisibleItemCount(1);
        sampleLocation = options;
        addSecondColumn();

        addThirdColumn();

        save = new Button("Save");
        Button cancel = new Button("Cancel");
        cancel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                CreateSampleForm.this.setVisible(false);
            }
        });
        HTMLPanel panel = new HTMLPanel(
                "<span id=\"save_new_sample\"></span><span id=\"cancel_new_sample\"></span>");
        panel.add(save, "save_new_sample");
        panel.add(cancel, "cancel_new_sample");
        table.setWidget(1, 0, panel);
        table.getFlexCellFormatter().setColSpan(1, 0, 3);
        table.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasAlignment.ALIGN_RIGHT);
    }

    public void addSaveHandler(final ClickHandler handler) {

        if (saveHandlerRegistration != null)
            saveHandlerRegistration.removeHandler();

        saveHandlerRegistration = save.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (validates())
                    handler.onClick(event);
            }
        });
    }

    private boolean validates() {

        boolean isValid = true;

        if (sampleLabel.getText().trim().isEmpty()) {
            sampleLabel.setStyleName("entry_input_error");
            isValid = false;
        } else
            sampleLabel.setStyleName("input_box");

        for (TextBox scheme : sampleLocationScheme) {
            String schemeText = scheme.getText();
            scheme.setStyleName("input_box");

            if (schemeText.trim().isEmpty()) {
                scheme.setStyleName("entry_input_error");
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * populates {@link SampleStorage}
     *
     * @return null if sample does not pass validation else the populated object
     */
    public SampleStorage populateSample() {
        if (!validates())
            return null;

        SampleInfo info = new SampleInfo();
        info.setLabel(sampleLabel.getText());
        info.setNotes(sampleNotes.getText());
        info.setDepositor(depositor.getText());
        info.setCreationTime(new Date());

        String location = locationOptions.getValue(locationOptions.getSelectedIndex());
        info.setLocationId(location);

        LinkedList<StorageInfo> storageInfos = new LinkedList<StorageInfo>();

        for (TextBox scheme : sampleLocationScheme) {
            StorageInfo storageInfo = new StorageInfo();
            String schemeText = scheme.getText();

            storageInfo.setDisplay(schemeText);
            storageInfos.add(storageInfo);
        }

        return new SampleStorage(info, storageInfos);
    }

    /**
     * Label and Notes
     */
    private void addFirstColumn() {
        // TODO use .css
        String html = "<span>Name</span><span class=\"required\">*</span>&nbsp;<span id=\"sample_label\"></span>"
                + "<br><span style=\"vertical-align: top;\">Notes</b>&nbsp; <span id=\"sample_notes\"></span></div>";
        HTMLPanel panel = new HTMLPanel(html);
        sampleLabel = new TextBox();
        sampleLabel.setWidth("180px");
        sampleLabel.setStyleName("input_box");
        panel.add(sampleLabel, "sample_label");

        sampleNotes = new TextArea();
        sampleNotes.setStyleName("input_box");
        sampleNotes.setVisibleLines(4);
        sampleNotes.setWidth("180px");

        panel.add(sampleNotes, "sample_notes");
        table.setWidget(0, 0, panel);
        table.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
    }

    private void addSecondColumn() {

        String html = "<span>Location</span>&nbsp;<span id=\"storage_options\"></span>"
                + "<div id=\"location_data\" style=\"padding-left: 56px\"></div>";
        HTMLPanel panel = new HTMLPanel(html);
        final VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("140px");
        panel.add(locationOptions, "storage_options");
        panel.add(vPanel, "location_data");

        table.setWidget(0, 1, panel);
        table.getFlexCellFormatter().setVerticalAlignment(0, 1, HasAlignment.ALIGN_TOP);

        for (SampleInfo location : sampleLocation.getLocations()) {
            locationOptions.addItem(location.getLocation(), location.getLocationId());
        }

        String value = locationOptions.getValue(0);
        ArrayList<String> list = sampleLocation.getListForLocation(value);
        if (list == null) {
            sampleLocationScheme.clear();
            return;
        }

        for (final String item : list) {
            final TextBox shelf = new TextBox();
            shelf.getElement().setAttribute("placeholder", item);
            shelf.setStyleName("input_box");
            vPanel.add(shelf);
            sampleLocationScheme.add(shelf);
        }

        locationOptions.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {

                vPanel.clear();
                sampleLocationScheme.clear();

                int index = locationOptions.getSelectedIndex();
                String value = locationOptions.getValue(index);
                ArrayList<String> list = sampleLocation.getListForLocation(value);
                if (list == null)
                    return;

                for (final String item : list) {
                    final TextBox shelf = new TextBox();
                    shelf.getElement().setAttribute("placeholder", item);
                    shelf.setStyleName("input_box");
                    vPanel.add(shelf);
                    sampleLocationScheme.add(shelf);
                }
            }
        });
    }

    private void addThirdColumn() {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.appendHtmlConstant("<span>");
        builder.appendEscaped(formatDate(new Date()));
        builder.appendHtmlConstant("</span><br /><span>by<span class=\"required\">*</span>");

        depositor = new TextBox();
        depositor.setStyleName("input_box");
        depositor.setText(AppController.accountInfo.getEmail());
        depositor.setEnabled(false);
        String html = builder.toSafeHtml().asString() + "<span id=\"sample_depositor\"></span>";
        HTMLPanel panel = new HTMLPanel(html);

        panel.add(depositor, "sample_depositor");

        table.setWidget(0, 2, panel);
//        table.getFlexCellFormatter().setStyleName(0, 2, "font-85em");
        table.getFlexCellFormatter().setVerticalAlignment(0, 2, HasAlignment.ALIGN_TOP);
    }

    protected String formatDate(Date date) {
        if (date == null)
            return "";

        DateTimeFormat format = DateTimeFormat.getFormat("EEE MMM d, y h:m a");
        return format.format(date);
    }
}
