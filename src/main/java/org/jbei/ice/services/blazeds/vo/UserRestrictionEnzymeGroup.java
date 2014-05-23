package org.jbei.ice.services.blazeds.vo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Value object to store user's restriction enzyme group for flex apps.
 *
 * @author Zinovii Dmytriv
 */
public class UserRestrictionEnzymeGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String groupName;
    private ArrayList<String> enzymeNames;

    public UserRestrictionEnzymeGroup() {
    }

    public UserRestrictionEnzymeGroup(String groupName, ArrayList<String> enzymeNames) {
        this.groupName = groupName;
        this.enzymeNames = enzymeNames;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ArrayList<String> getEnzymeNames() {
        return enzymeNames;
    }

    public void setEnzymeNames(ArrayList<String> enzymeNames) {
        this.enzymeNames = enzymeNames;
    }
}
