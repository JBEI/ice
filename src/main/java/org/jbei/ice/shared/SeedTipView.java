package org.jbei.ice.shared;

import java.io.Serializable;

public class SeedTipView extends EntryDataView implements Serializable {

    private static final long serialVersionUID = 1L;

    private String plantType;
    private String generation;
    private String homozygosity;
    private String ecotype;
    private String parents;
    private String harvested;

    public SeedTipView() {
    }

    public String getPlantType() {
        return plantType;
    }

    public void setPlantType(String plantType) {
        this.plantType = plantType;
    }

    public String getGeneration() {
        return generation;
    }

    public void setGeneration(String generation) {
        this.generation = generation;
    }

    public String getHomozygosity() {
        return homozygosity;
    }

    public void setHomozygosity(String homozygosity) {
        this.homozygosity = homozygosity;
    }

    public String getEcotype() {
        return ecotype;
    }

    public void setEcotype(String ecotype) {
        this.ecotype = ecotype;
    }

    public String getParents() {
        return parents;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public String getHarvested() {
        return harvested;
    }

    public void setHarvested(String harvested) {
        this.harvested = harvested;
    }
}
