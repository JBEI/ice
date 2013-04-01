package org.jbei.ice.client.profile.group;

import org.jbei.ice.client.common.group.GroupPanel;
import org.jbei.ice.client.profile.widget.IUserProfilePanel;
import org.jbei.ice.shared.dto.group.GroupInfo;
import org.jbei.ice.shared.dto.group.GroupType;

/**
 * Panel for managing private groups on the User page
 *
 * @author Hector Plahar
 */
public class UserGroupPanel extends GroupPanel implements IUserProfilePanel {

    public GroupInfo getNewGroup() {
        return groupsWidget.getNewGroup(GroupType.PRIVATE);
    }
}
