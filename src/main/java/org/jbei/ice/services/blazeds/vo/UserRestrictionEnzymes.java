package org.jbei.ice.services.blazeds.vo;

import java.io.Serializable;
import java.util.List;

/**
 * Value object to store {@link UserRestrictionEnzymeGroup}s, and active enzymes for flex apps.
 *
 * @author Zinovii Dmytriv
 */
public class UserRestrictionEnzymes implements Serializable {
    private static final long serialVersionUID = -4363772779483195780L;

    private List<UserRestrictionEnzymeGroup> groups;
    private List<String> activeEnzymeNames;

    // Constructors
    public UserRestrictionEnzymes() {
        super();
    }

    public UserRestrictionEnzymes(List<UserRestrictionEnzymeGroup> groups,
            List<String> activeEnzymeNames) {
        super();

        this.groups = groups;
        this.activeEnzymeNames = activeEnzymeNames;
    }

    // Properties
    public List<UserRestrictionEnzymeGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<UserRestrictionEnzymeGroup> value) {
        groups = value;
    }

    public List<String> getActiveEnzymeNames() {
        return activeEnzymeNames;
    }

    public void setActiveEnzymeNames(List<String> value) {
        activeEnzymeNames = value;
    }
}
