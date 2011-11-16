package org.jbei.ice.lib.vo;

import java.util.Date;

/**
 * Value object to hold VectorEditor project files.
 * 
 * @author Zinovii Dmytriv
 * 
 */
public class VectorEditorProject extends Project {
    private static final long serialVersionUID = 1L;

    private FeaturedDNASequence featuredDNASequence;

    public VectorEditorProject() {
    }

    public VectorEditorProject(String name, String description, String uuid, String ownerEmail,
            String ownerName, Date creationTime, Date modificationTime,
            FeaturedDNASequence featuredDNASequence) {
        super(name, description, uuid, ownerEmail, ownerName, creationTime, modificationTime);

        this.featuredDNASequence = featuredDNASequence;
    }

    public FeaturedDNASequence getFeaturedDNASequence() {
        return featuredDNASequence;
    }

    public void setFeaturedDNASequence(FeaturedDNASequence featuredDNASequence) {
        this.featuredDNASequence = featuredDNASequence;
    }

    @Override
    public String typeName() {
        return "vector-editor";
    }
}