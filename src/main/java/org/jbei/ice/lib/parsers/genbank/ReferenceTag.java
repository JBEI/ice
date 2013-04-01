package org.jbei.ice.lib.parsers.genbank;

import java.util.ArrayList;

/**
 * @author Hector Plahar
 */
public class ReferenceTag extends Tag {

    private ArrayList<Tag> references = new ArrayList<>();

    public ReferenceTag() {
        super(Type.REFERENCE);
    }

    @SuppressWarnings("unused")
    public void setReferences(ArrayList<Tag> references) {
        this.references = references;
    }

    @SuppressWarnings("unused")
    public ArrayList<Tag> getReferences() {
        return references;
    }

}
