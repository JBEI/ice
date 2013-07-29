package org.jbei.ice.client.entry.display.view;

import java.util.ArrayList;

import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.common.widget.Dialog;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.entry.display.model.FlagEntry;
import org.jbei.ice.client.entry.display.model.SampleStorage;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Edit / Delete / Alert widget for entries
 *
 * @author Hector Plahar
 */
public class EntryActionWidget extends Composite {

    private HTML edit;
    private HTML pipe1;
    private HTML delete;
    private HTML pipe2;
    private HTML flag;
    private HandlerRegistration deleteRegistration;
    private HandlerRegistration editRegistration;
    private FlexTable layout;
    private Delegate<FlagEntry> delegate;
    private FlexTable dialogLayout;
    private ListBox sampleOptions;
    private HTMLPanel samplePanel;

    public EntryActionWidget() {
        initComponents();
        layout.setWidget(0, 0, edit);
        layout.getFlexCellFormatter().setStyleName(0, 0, "pad-left-40");
        layout.setWidget(0, 1, pipe1);
        layout.setWidget(0, 2, delete);
        layout.setWidget(0, 3, pipe2);
        layout.setWidget(0, 4, flag);
        initWidget(layout);

        sampleOptions = new ListBox();
        sampleOptions.setStyleName("pull_down");
        sampleOptions.setVisible(false);

        addFlagClickHandler();
    }

    private void initComponents() {
        layout = new FlexTable();
        layout.setCellPadding(0);
        layout.setCellSpacing(5);

        String html = "<i class=\"" + FAIconType.EDIT.getStyleName() + "\"></i> <span class=\"font-80em\">Edit</span>";
        edit = new HTML(html);
        edit.setStyleName("edit_icon");
        edit.setTitle("Edit Entry");

        pipe1 = new HTML("<span style=\"color: #ccc\">&nbsp;|&nbsp;</span>");

        delete = new HTML(
                "<i class=\"" + FAIconType.TRASH.getStyleName() + "\"></i> <span class=\"font-80em\">Delete</span>");
        delete.setStyleName("delete_icon");
        delete.setTitle("Delete");
        pipe2 = new HTML("<span style=\"color: #ccc\">&nbsp;|&nbsp;</span>");
        flag = new HTML("<i class=\"" + FAIconType.WARNING_SIGN.getStyleName()
                                + "\"></i> <span class=\"font-80em\">Alert</span>");
        flag.setStyleName("flag_icon");
    }

    protected void addFlagClickHandler() {
        flag.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                TextArea area = new TextArea();
                area.setStyleName("input_box");
                area.getElement().setAttribute("placeHolder", "Enter Problem Description");
                area.setCharacterWidth(70);
                area.setVisibleLines(4);

                String html = "<b class=\"font-75em\" style=\"vertical-align: top\">Affected Samples</b> &nbsp;"
                        + "<span  id=\"sample_selection\"></span>";
                samplePanel = new HTMLPanel(html);
                samplePanel.add(sampleOptions, "sample_selection");

                dialogLayout = new FlexTable();
                dialogLayout.setWidth("100%");
                dialogLayout.setWidget(0, 0, area);
                dialogLayout.setWidget(1, 0, samplePanel);
                samplePanel.setVisible(false);

                Dialog dialog = new Dialog(dialogLayout, "500px", "Entry Problem Alert");
                dialog.showDialog(true);
                dialog.setSubmitHandler(createDialogSubmitHandler(area, dialog));
            }
        });
    }

    public void setSampleOptions(ArrayList<SampleStorage> samples) {
        sampleOptions.clear();
        sampleOptions.addItem("None");
        sampleOptions.addItem("All");

        if (samples == null || samples.isEmpty()) {
            sampleOptions.setEnabled(false);
            return;
        }

        for (SampleStorage storage : samples) {
            sampleOptions.addItem(storage.getPartSample().getLabel());
        }
        sampleOptions.setVisible(true);
        samplePanel.setVisible(true);
    }

    private ClickHandler createDialogSubmitHandler(final TextArea area, final Dialog dialog) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String msg = area.getText().trim();
                if (msg.isEmpty()) {
                    area.setStyleName("input_box_error");
                    area.setFocus(true);
                    return;
                }

                area.setStyleName("input_box");
                msg = getUserInput(msg);
                delegate.execute(new FlagEntry(FlagEntry.FlagOption.ALERT, msg));
                dialog.showDialog(false);
            }
        };
    }

    private String getUserInput(String areaMessage) {
        String selected = sampleOptions.getItemText(sampleOptions.getSelectedIndex());
        if ("None".equalsIgnoreCase(selected)) {
            return areaMessage;
        }

        if ("All".equalsIgnoreCase(selected)) {
            String txt = "";
            for (int i = 0; i < sampleOptions.getItemCount() - 1; i += 1) {
                String text = sampleOptions.getItemText(i);
                if ("None".equals(text) || "All".equals(text))
                    continue;

                txt += (text + ",");
            }
            txt += sampleOptions.getItemText(sampleOptions.getItemCount() - 1);
            return "<b>Affected Samples: </b>" + txt + "<br><br>" + areaMessage;
        }

        String txt = sampleOptions.getItemText(sampleOptions.getSelectedIndex());
        return "<b>Affected Sample: </b>" + txt + "<br><br>" + areaMessage;
    }

    public void addDeleteEntryHandler(ClickHandler handler) {
        if (deleteRegistration != null)
            deleteRegistration.removeHandler();

        deleteRegistration = delete.addClickHandler(handler);
    }

    public void addEditButtonHandler(ClickHandler handler) {
        if (editRegistration != null)
            editRegistration.removeHandler();
        editRegistration = edit.addClickHandler(handler);
    }

    public void setFlagDelegate(Delegate<FlagEntry> delegate) {
        this.delegate = delegate;
    }

    public void showEdit(boolean showEdit) {
        edit.setVisible(showEdit);
        pipe1.setVisible(showEdit);
        delete.setVisible(showEdit);
        pipe2.setVisible(showEdit);
    }
}
