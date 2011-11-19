package org.jbei.ice.shared;

public class StrainTipView extends EntryData {

    private String markers;
    private String host;
    private String genPhen;
    private String plasmids;

    public StrainTipView() {
        this.setType(EntryAddType.STRAIN.getDisplay());
    }

    public String getMarkers() {
        return markers;
    }

    public void setMarkers(String markers) {
        this.markers = markers;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getGenPhen() {
        return genPhen;
    }

    public void setGenPhen(String genPhen) {
        this.genPhen = genPhen;
    }

    public String getPlasmids() {
        return plasmids;
    }

    public void setPlasmids(String plasmids) {
        this.plasmids = plasmids;
    }
}
