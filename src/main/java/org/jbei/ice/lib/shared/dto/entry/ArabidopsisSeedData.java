package org.jbei.ice.lib.shared.dto.entry;

import java.util.Date;

public class ArabidopsisSeedData extends PartData {

    private static final long serialVersionUID = 1L;

    private String homozygosity;
    private String ecotype;
    private Date harvestDate;
    private String parents;
    private Generation generation;
    private PlantType plantType;
    private Boolean sentToAbrc;

    public ArabidopsisSeedData() {
        super(EntryType.ARABIDOPSIS);
    }

    // getters and setters
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

    public Date getHarvestDate() {
        return harvestDate;
    }

    public void setHarvestDate(Date harvestDate) {
        this.harvestDate = harvestDate;
    }

    public String getParents() {
        return parents;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public Generation getGeneration() {
        return generation;
    }

    public void setGeneration(Generation generation) {
        this.generation = generation;
    }

    public void setPlantType(PlantType plantType) {
        this.plantType = plantType;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public Boolean isSentToAbrc() {
        return sentToAbrc;
    }

    public void setSentToAbrc(Boolean sentToAbrc) {
        this.sentToAbrc = sentToAbrc;
    }
}
