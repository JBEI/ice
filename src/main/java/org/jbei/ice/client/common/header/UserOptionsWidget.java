package org.jbei.ice.client.common.header;

import org.jbei.ice.client.common.widget.PullDownOptions;
import org.jbei.ice.client.profile.widget.UserOption;

/**
 * @author Hector Plahar
 */
public class UserOptionsWidget extends PullDownOptions<UserOption> {

//    private final String userName;
//    private final String emailHash;

    public UserOptionsWidget(String userName, String emailHash) {
        super(userName);
//        this.userName = userName;
//        this.emailHash = emailHash;
        setOptions(UserOption.quickAccessList());
    }

    protected String renderCell(UserOption value) {
        return "<i style=\"display: inline-block; width: 1.3em; text-align: left\" class=\""
                + value.getIcon().getStyleName()
                + "\"></i><span>" + value.toString() + "</span>";
    }
}
