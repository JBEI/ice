package org.jbei.ice.client.admin.group;

import org.jbei.ice.client.admin.IAdminPanel;
import org.jbei.ice.client.common.group.GroupPanel;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;

/**
 * Panel for managing groups. Also acts as the view
 *
 * @author Hector Plahar
 */
public class AdminGroupPanel extends GroupPanel implements IAdminPanel {

    public AdminGroupPanel() {
        super(true);
    }

    public UserGroup getNewGroup() {
        return groupsWidget.getNewGroup(GroupType.PUBLIC);
    }
}
