package org.jbei.ice.client.profile;

import java.util.HashMap;

import org.jbei.ice.client.profile.widget.UserOption;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Menu for the profile page
 *
 * @author Hector Plahar
 */
public class ProfileViewMenu extends Composite {

    private FlexTable layout;
    private final HashMap<Integer, UserOption> rowOption;
    private int currentRowSelection;
    private SingleSelectionModel<UserOption> selectionModel;

    public ProfileViewMenu() {
        layout = new FlexTable();
        layout.setStyleName("profile_menu");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        initWidget(layout);
        rowOption = new HashMap<Integer, UserOption>();
        selectionModel = new SingleSelectionModel<UserOption>();

        createMenu();

        layout.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HTMLTable.Cell cell = layout.getCellForEvent(event);
                if (cell == null)
                    return;

                layout.getCellFormatter().removeStyleName(currentRowSelection, 0, "selected");
                currentRowSelection = cell.getRowIndex();
                layout.getCellFormatter().setStyleName(currentRowSelection, 0, "selected");
                UserOption selectedOption = rowOption.get(cell.getRowIndex());
                selectionModel.setSelected(selectedOption, true);
            }
        });
    }

    public SingleSelectionModel<UserOption> getSelectionModel() {
        return this.selectionModel;
    }

    protected void createMenu() {
        int row = 1;
        currentRowSelection = row;
        for (UserOption option : UserOption.values()) {
            String html = "<i style=\"width: 24px; float: left; color: #777\" class=\""
                    + option.getIcon().getStyleName() + " font-awesome font-90em\"></i>" + option.toString();
            layout.setHTML(row, 0, html);
            if (option == UserOption.PROFILE) {
                layout.getCellFormatter().setStyleName(row, 0, "selected");
                selectionModel.setSelected(option, true);
            }
            rowOption.put(row, option);
            row += 1;
        }
    }
}
