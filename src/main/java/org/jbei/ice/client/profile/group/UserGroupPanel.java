package org.jbei.ice.client.profile.group;

import org.jbei.ice.client.common.group.GroupPanel;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.lib.shared.dto.group.GroupType;
import org.jbei.ice.lib.shared.dto.group.UserGroup;

/**
 * Panel for managing private groups on the User page
 *
 * @author Hector Plahar
 */
public class UserGroupPanel extends GroupPanel implements IUserProfilePanel {

    public UserGroup getNewGroup() {
        return groupsWidget.getNewGroup(GroupType.PRIVATE);
    }
}
