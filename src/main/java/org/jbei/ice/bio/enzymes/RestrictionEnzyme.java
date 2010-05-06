package org.jbei.ice.bio.enzymes;

import java.io.Serializable;

public class RestrictionEnzyme implements Serializable {
    private static final long serialVersionUID = 5641976664745014957L;

    private String name;
    private String site;
    private int cutType;
    private String forwardRegex;
    private String reverseRegex;
    private int dsForward;
    private int dsReverse;
    private int usForward;
    private int usReverse;

    // Constructors
    public RestrictionEnzyme() {
    }

    public RestrictionEnzyme(String name, String site, int cutType, String forwardRegex,
            String reverseRegex, int dsForward, int dsReverse, int usForward, int usReverse) {
        super();
        this.name = name;
        this.site = site;
        this.cutType = cutType;
        this.forwardRegex = forwardRegex;
        this.reverseRegex = reverseRegex;
        this.dsForward = dsForward;
        this.dsReverse = dsReverse;
        this.usForward = usForward;
        this.usReverse = usReverse;
    }

    // Properties
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getCutType() {
        return cutType;
    }

    public void setCutType(int cutType) {
        this.cutType = cutType;
    }

    public String getForwardRegex() {
        return forwardRegex;
    }

    public void setForwardRegex(String forwardRegex) {
        this.forwardRegex = forwardRegex;
    }

    public String getReverseRegex() {
        return reverseRegex;
    }

    public void setReverseRegex(String reverseRegex) {
        this.reverseRegex = reverseRegex;
    }

    public int getDsForward() {
        return dsForward;
    }

    public void setDsForward(int dsForward) {
        this.dsForward = dsForward;
    }

    public int getDsReverse() {
        return dsReverse;
    }

    public void setDsReverse(int dsReverse) {
        this.dsReverse = dsReverse;
    }

    public int getUsForward() {
        return usForward;
    }

    public void setUsForward(int usForward) {
        this.usForward = usForward;
    }

    public int getUsReverse() {
        return usReverse;
    }

    public void setUsReverse(int usReverse) {
        this.usReverse = usReverse;
    }
}
