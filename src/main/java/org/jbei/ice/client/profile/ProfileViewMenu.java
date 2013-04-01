package org.jbei.ice.client.profile;

import java.util.HashMap;
import java.util.Map;

import org.jbei.ice.client.Page;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
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
    private final String userId;

    public ProfileViewMenu(String id) {
        layout = new FlexTable();
        layout.setStyleName("profile_menu");
        layout.setCellPadding(0);
        layout.setCellSpacing(0);
        initWidget(layout);
        rowOption = new HashMap<Integer, UserOption>();
        selectionModel = new SingleSelectionModel<UserOption>();
        userId = id;

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
                History.newItem(Page.PROFILE.getLink() + ";id=" + userId + ";s=" + selectedOption.getUrl(), false);
            }
        });
    }

    public SingleSelectionModel<UserOption> getSelectionModel() {
        return this.selectionModel;
    }

    public void createMenu(UserOption... menuOptions) {
        int row = 1;
        currentRowSelection = row;
        for (UserOption option : menuOptions) {
            String html = "<i style=\"display: inline; margin-right: 10px;\" class=\""
                    + option.getIcon().getStyleName() + " font-awesome\"></i>" + option.toString();
            layout.setHTML(row, 0, html);
            rowOption.put(row, option);
            row += 1;
        }
    }

    public void showSelected(UserOption option) {
        int i = -1;
        for (Map.Entry<Integer, UserOption> row : rowOption.entrySet()) {
            if (row.getValue() == option) {
                i = row.getKey();
                break;
            }
        }
        if (i == -1)
            return;

        layout.getCellFormatter().removeStyleName(currentRowSelection, 0, "selected");
        currentRowSelection = i;
        layout.getCellFormatter().setStyleName(currentRowSelection, 0, "selected");
        History.newItem(Page.PROFILE.getLink() + ";id=" + userId + ";s=" + option.getUrl(), false);
    }
}
