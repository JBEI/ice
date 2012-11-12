package org.jbei.ice.client.common.header;

import org.jbei.ice.shared.dto.EntryType;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * @author Hector Plahar
 */
public class EntryTypeFilterWidget extends Composite {

    private final CheckBox allCheck;
    private final CheckBox[] typeChecks;

    public EntryTypeFilterWidget() {

        allCheck = new CheckBox();
        typeChecks = new CheckBox[EntryType.values().length];

        String html =
                "<span class=\"font-80em;\" style=\"letter-spacing:-1.8px; color:#777\"><b>SEARCH:</b></span> <label " +
                        "style=\"padding-left:10px;\"><span style=\"position:relative; top: 2px; *overflow: hidden\" " +
                        "id=\"all_check\"></span>All</label>";

        for (int i = 0; i < EntryType.values().length; i += 1) {
            typeChecks[i] = new CheckBox();
            html += "<label style=\" padding-left:10px;\"><span style=\"position:relative; top: " +
                    "2px; *overflow: hidden\" id=\"" + EntryType.values()[i]
                    .getName() + "_check\"></span>" + EntryType.values()[i].getDisplay() + "</label>";
        }

        HTMLPanel htmlPanel = new HTMLPanel(html);
        htmlPanel.setStyleName("font-80em");
        htmlPanel.addStyleName("pad-3");
        initWidget(htmlPanel);

        htmlPanel.add(allCheck, "all_check");

        for (int i = 0; i < EntryType.values().length; i += 1) {
            htmlPanel.add(typeChecks[i], EntryType.values()[i].getName() + "_check");
        }

        addHandlers();

        // all is pre-selected
        allCheck.setValue(Boolean.TRUE, true);
    }

    public void setSelected(EntryType... select) {
        if (select == null || select.length == 0 || select.length == typeChecks.length) {
            allCheck.setValue(true, true);
            return;
        }

        for (int i = 0; i < EntryType.values().length; i += 1) {
            for (int j = 0; j < select.length; j += 1) {
                EntryType type = EntryType.values()[i];
                if (type == select[j]) {
                    typeChecks[i].setValue(true, true);
                    break;
                }
            }
        }
    }

    protected void addHandlers() {
        allCheck.addValueChangeHandler(new CheckBoxHandlers(true));
        CheckBoxHandlers handlers = new CheckBoxHandlers(false);
        for (CheckBox box : typeChecks) {
            box.addValueChangeHandler(handlers);
        }
    }

    public String[] getSelected() {
        String[] selected = new String[]{};

        if (allCheck.getValue().booleanValue()) {
            int i = 0;
            for (EntryType type : EntryType.values()) {
                selected[i] = type.getName();
                i += 1;
            }
        } else {
            for (int i = 0; i < EntryType.values().length; i += 1) {
                if (typeChecks[i].getValue().booleanValue()) {
                    selected[i] = EntryType.values()[i].getName();
                }
            }
        }

        return selected;
    }

    private class CheckBoxHandlers implements ValueChangeHandler<Boolean> {

        private final boolean isAll;

        public CheckBoxHandlers(boolean isAll) {
            this.isAll = isAll;
        }

        @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {

            if (isAll) {
                for (CheckBox box : typeChecks) {
                    box.setValue(allCheck.getValue(), false);
                }
            } else {
                for (CheckBox box : typeChecks) {
                    allCheck.setValue(box.getValue(), false);
                    if (!box.getValue())
                        break;
                }
            }
        }
    }
}
