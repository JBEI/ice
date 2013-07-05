package org.jbei.ice.client.profile.group.widget;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.lib.shared.dto.group.GroupInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Cell widget for editing a group
 *
 * @author Hector Plahar
 */
public class EditGroupCell extends Composite {

    private final HTML submit;
    private final HTML cancel;
    private final CreateGroupCell cell;
    private final GroupInfo info;

    public EditGroupCell(GroupInfo info) {
        cell = new CreateGroupCell();

        this.info = info;
        cell.setGroupName(info.getLabel());
        cell.setGroupDescription(info.getDescription());

        FlexTable panel = new FlexTable();
        panel.setWidth("100%");
        panel.setCellPadding(0);
        panel.setCellSpacing(0);
        initWidget(panel);

        // submit
        submit = new HTML("<i class=\"" + FAIconType.OK.getStyleName() + "\"></i>");
        submit.setStyleName("display-inline");
        submit.addStyleName("add_icon");

        // cancel
        cancel = new HTML("<i class=\"" + FAIconType.REMOVE.getStyleName() + "\"></i>");
        cancel.setStyleName("display-inline");
        cancel.addStyleName("delete_icon");

        panel.setWidget(0, 0, cell);
        panel.getFlexCellFormatter().setWidth(0, 0, "300px");

        HTMLPanel actionPanel = new HTMLPanel("<span id=\"submit_changes\"></span>"
                                                      + "&nbsp;<span style=\"color: #DDD\">|</span>&nbsp;"
                                                      + "<span id=\"cancel_changes\"></span>&nbsp;");
        actionPanel.setStyleName("action_panel");
        actionPanel.add(submit, "submit_changes");
        actionPanel.add(cancel, "cancel_changes");
        panel.setWidget(0, 1, actionPanel);
    }

    public void addCancelHandler(ClickHandler handler) {
        cancel.addClickHandler(handler);
    }

    public void addSubmitHandler(final ClickHandler handler) {
        submit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!cell.validate())
                    return;

                handler.onClick(event);
            }
        });
    }

    public GroupInfo getGroup() {
        info.setLabel(cell.getGroupName());
        info.setDescription(cell.getGroupDescription());
        return info;
    }
}
