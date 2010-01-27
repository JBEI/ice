package org.jbei.ice.services.blazeds.VectorEditor.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserRestrictionEnzymes implements Serializable {
    private static final long serialVersionUID = -4363772779483195780L;

    private Set<RestrictionEnzymeGroup> groups;
    private Set<RestrictionEnzyme> activeGroup;

    // Constructors
    public UserRestrictionEnzymes() {
        super();

        groups = new HashSet<RestrictionEnzymeGroup>();
        activeGroup = new HashSet<RestrictionEnzyme>();
    }

    public UserRestrictionEnzymes(Set<RestrictionEnzymeGroup> groups,
            Set<RestrictionEnzyme> activeGroup) {
        super();

        this.groups = groups;
        this.activeGroup = activeGroup;
    }

    // Properties
    public Set<RestrictionEnzymeGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<RestrictionEnzymeGroup> value) {
        groups = value;
    }

    public Set<RestrictionEnzyme> getActiveGroup() {
        return activeGroup;
    }

    public void setActiveGroup(Set<RestrictionEnzyme> value) {
        activeGroup = value;
    }
}
