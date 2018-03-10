package org.jbei.ice.lib.parsers.genbank;

/**
 * @author Hector Plahar
 */
public class AccessionTag extends Tag {

    public AccessionTag(String value) {
        super(Type.ACCESSION);
        this.setValue(value);
    }
}
