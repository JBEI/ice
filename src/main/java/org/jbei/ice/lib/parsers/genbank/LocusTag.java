package org.jbei.ice.lib.parsers.genbank;

/**
 * @author Hector Plahar
 */
class LocusTag extends Tag {

    private String locusName = "";
    private boolean isCircular = true;

    public LocusTag() {
        super(Type.LOCUS);
    }

    public String getLocusName() {
        return locusName;
    }

    public void setLocusName(String locusName) {
        this.locusName = locusName;
    }

    public boolean isCircular() {
        return isCircular;
    }

    public void setCircular(boolean isCircular) {
        this.isCircular = isCircular;
    }
}
