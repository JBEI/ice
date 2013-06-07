package org.jbei.ice.client.entry.view.view;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import org.jbei.ice.client.Delegate;
import org.jbei.ice.client.common.widget.Dialog;
import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PopupHandler;
import org.jbei.ice.client.entry.view.model.FlagEntry;

import java.util.Arrays;

/**
 * Edit / Delete / Flag widget for entries
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
    private CellList<FlagEntry.FlagOption> options;
    private SingleSelectionModel<FlagEntry.FlagOption> optionSelection;
    private Delegate<FlagEntry> delegate;

    interface EntryActionResource extends CellList.Resources {

        static EntryActionResource INSTANCE = GWT.create(EntryActionResource.class);

        @Source("org/jbei/ice/client/resource/css/EntryActionWidget.css")
        CellList.Style cellListStyle();
    }

    public EntryActionWidget() {
        initComponents();
        layout.setWidget(0, 0, edit);
        layout.getFlexCellFormatter().setStyleName(0, 0, "pad-left-40");
        layout.setWidget(0, 1, pipe1);
        layout.setWidget(0, 2, delete);
        layout.setWidget(0, 3, pipe2);
        layout.setWidget(0, 4, flag);
        initWidget(layout);

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
        flag = new HTML("<i class=\"" + FAIconType.FLAG_CHECKERED.getStyleName()
                + "\"></i> <span class=\"font-80em\">Flag</span> <i class=\"font-70em "
                + FAIconType.CARET_DOWN.getStyleName() + "\"></i>");
        flag.setStyleName("flag_icon");
    }

    protected void addFlagClickHandler() {
        // renderer for options list
        options = new CellList<FlagEntry.FlagOption>(new AbstractCell<FlagEntry.FlagOption>() {

            @Override
            public void render(Context context, FlagEntry.FlagOption value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<span>" + value.toString() + "</span>");
            }
        }, EntryActionResource.INSTANCE);

        final PopupHandler popupHandler = new PopupHandler(options, flag.getElement(), false);
        flag.addClickHandler(popupHandler);
        optionSelection = new SingleSelectionModel<FlagEntry.FlagOption>();
        optionSelection.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                FlagEntry.FlagOption selected = optionSelection.getSelectedObject();
                if (selected == null)
                    return;
                popupHandler.hidePopup();
                optionSelection.setSelected(selected, false);
                if (delegate == null)
                    return;

                switch (selected) {
                    case ALERT:
                        TextArea area = new TextArea();
                        area.setStyleName("input_box");
                        area.getElement().setAttribute("placeHolder", "Enter Problem Description");
                        area.setCharacterWidth(70);
                        area.setVisibleLines(4);
                        Dialog dialog = new Dialog(area);
                        dialog.showDialog(true);
                        dialog.setSubmitHandler(createDialogSubmitHandler(area, dialog));
                        break;

                    case REQUEST_SAMPLE:
                        VerticalPanel panel = new VerticalPanel();
                        panel.setStyleName("font-80em");
                        panel.setWidth("350px");
                        RadioButton culture = new RadioButton("sample", "Liquid Culture");
                        culture.setValue(true);
                        RadioButton streak = new RadioButton("sample", "Streak on Agar Plate");
                        panel.add(culture);
                        panel.add(streak);

                        dialog = new Dialog(panel, "400px", "Request Sample in the form of:");
                        dialog.showDialog(true);
                        dialog.setSubmitHandler(createDialogSampleSubmitHandler(culture, dialog));
                        break;

                    default:
                        delegate.execute(new FlagEntry(selected, ""));
                }
            }
        });

        options.setSelectionModel(optionSelection);
    }

    private ClickHandler createDialogSampleSubmitHandler(final RadioButton culture, final Dialog dialog) {
        return new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String msg = "Streak on Agar Plate";
                if (culture.getValue().booleanValue()) {
                    msg = "Liquid Culture";
                }
                delegate.execute(new FlagEntry(FlagEntry.FlagOption.REQUEST_SAMPLE, msg));
                dialog.showDialog(false);
            }
        };
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
                delegate.execute(new FlagEntry(FlagEntry.FlagOption.ALERT, msg));
                dialog.showDialog(false);
            }
        };
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

    @Override
    public void setVisible(boolean visible) {
        edit.setVisible(visible);
        pipe1.setVisible(visible);
        delete.setVisible(visible);
        pipe2.setVisible(visible);
        flag.setVisible(visible);
    }

    public void setHasSample(boolean hasSample) {
        if (hasSample)
            options.setRowData(Arrays.asList(FlagEntry.FlagOption.values()));
        else
            options.setRowData(Arrays.asList(FlagEntry.FlagOption.ALERT));
    }
}
