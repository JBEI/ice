package org.jbei.ice.client.common.header;

import java.util.Arrays;

import org.jbei.ice.client.common.widget.FAIconType;
import org.jbei.ice.client.common.widget.PullDownOptions;

/**
 * @author Hector Plahar
 */
public class UserOptionsWidget extends PullDownOptions<UserOptionsWidget.UserOptions> {

//    private final String userName;
//    private final String emailHash;

    public UserOptionsWidget(String userName, String emailHash) {
        super(userName);
//        this.userName = userName;
//        this.emailHash = emailHash;
        setOptions(Arrays.asList(UserOptions.values()));
    }

    protected String renderCell(UserOptions value) {
        return "<i style=\"display: inline-block; width: 1.3em; text-align: left\" class=\""
                + value.getIcon().getStyleName()
                + "\"></i><span>" + value.toString() + "</span>";
    }

    public enum UserOptions {
        PROFILE(FAIconType.USER, "Profile"),
        PREFERENCES(FAIconType.COG, "Preferences"),
        GROUPS(FAIconType.GROUP, "Groups"),
        MESSAGES(FAIconType.ENVELOPE_ALT, "Messages");

        private final String display;
        private final FAIconType icon;

        UserOptions(FAIconType icon, String display) {
            this.display = display;
            this.icon = icon;
        }

        public String toString() {
            return this.display;
        }

        public FAIconType getIcon() {
            return this.icon;
        }
    }
}
