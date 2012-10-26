package org.jbei.ice.client.common.header;

import org.jbei.ice.client.Page;
import org.jbei.ice.client.common.widget.PullDownOptions;
import org.jbei.ice.client.profile.widget.UserOption;

import com.google.gwt.user.client.History;
import com.google.gwt.view.client.SelectionChangeEvent;

/**
 * @author Hector Plahar
 */
public class UserOptionsWidget extends PullDownOptions<UserOption> {

    private final long userId;

    public UserOptionsWidget(String userName, long userId) {
        super(userName);
        this.userId = userId;
        setOptions(UserOption.quickAccessList());
        setSelectionHandler();
    }

    protected String renderCell(UserOption value) {
        return "<i style=\"display: inline-block; width: 1.3em; text-align: left\" class=\""
                + value.getIcon().getStyleName()
                + "\"></i><span>" + value.toString() + "</span>";
    }

    public void setSelectionHandler() {
        getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                UserOption selected = optionSelection.getSelectedObject();
                if (selected == null)
                    return;

                optionSelection.setSelected(selected, false);
                History.newItem(Page.PROFILE.getLink() + ";id=" + userId + ";s=" + selected.getUrl());
            }
        });
    }
}
