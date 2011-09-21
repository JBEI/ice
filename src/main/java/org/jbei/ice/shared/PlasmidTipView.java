package org.jbei.ice.shared;

public class PlasmidTipView extends EntryData {

    private static final long serialVersionUID = 1L;

    private String markers;
    private String backbone;
    private String origin;
    private String promoters;
    private String strains;

    public PlasmidTipView() {

    }

    public String getMarkers() {
        return markers;
    }

    public void setMarkers(String markers) {
        this.markers = markers;
    }

    public String getBackbone() {
        return backbone;
    }

    public void setBackbone(String backbone) {
        this.backbone = backbone;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getPromoters() {
        return promoters;
    }

    public void setPromoters(String promoters) {
        this.promoters = promoters;
    }

    public String getStrains() {
        return strains;
    }

    public void setStrains(String strains) {
        this.strains = strains;
    }

}
