package org.jbei.ice.services.blazeds.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.jbei.ice.bio.enzymes.RestrictionEnzyme;

/**
 * Value object to store Restriction Enzyme Group information for flex apps.
 *
 * @author Zinovii Dmytriv
 */
public class RestrictionEnzymeGroup implements Serializable {
    private static final long serialVersionUID = 2321365485733702387L;

    private String name;
    private Set<RestrictionEnzyme> enzymes;

    // Constructors

    /**
     * Constructor.
     */
    public RestrictionEnzymeGroup() {
        enzymes = new HashSet<RestrictionEnzyme>();
    }

    /**
     * Constuctor setting the name and the {@link RestrictionEnzyme}s.
     *
     * @param name    name of the group to create.
     * @param enzymes Set of RestrictionEnzymes.
     */
    public RestrictionEnzymeGroup(String name, Set<RestrictionEnzyme> enzymes) {
        super();
        this.name = name;
        this.enzymes = enzymes;
    }

    // Properties
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<RestrictionEnzyme> getEnzymes() {
        return enzymes;
    }

    public void setEnzymes(Set<RestrictionEnzyme> enzymes) {
        this.enzymes = enzymes;
    }

    // Public Methods
    public void addEnzyme(RestrictionEnzyme enzyme) {
        enzymes.add(enzyme);
    }

    public void removeEnzyme(RestrictionEnzyme enzyme) {
        enzymes.remove(enzyme);
    }

    public boolean hasEnzyme(RestrictionEnzyme enzyme) {
        return enzymes.contains(enzyme);
    }
}
