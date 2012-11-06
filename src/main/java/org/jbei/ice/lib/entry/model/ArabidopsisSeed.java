package org.jbei.ice.lib.entry.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jbei.ice.shared.dto.EntryType;

import org.hibernate.search.annotations.Indexed;

/**
 * Store Arabidopsis Seed specific fields.
 * <p/>
 * <ul>
 * <li><b>homozygosity: </b></li>
 * <li><b>ecotype: </b></li>
 * <li><b>harvestDate: </b></li>
 * <li><b>parents:</b></li>
 * <li><b>generation:</b></li>
 * <li><b>plantType</b></li>
 * <li><b>Sent to ABRC</b></li>
 * </ul>
 *
 * @author Timothy Ham, Hector Plahar
 */
@Entity
@Indexed
@PrimaryKeyJoinColumn(name = "entries_id")
@Table(name = "arabidopsis_seed")
public class ArabidopsisSeed extends Entry {

    private static final long serialVersionUID = 1L;

    /**
     * Generation options.
     *
     * @author Timothy Ham
     */
    public enum Generation {
        NULL, M0, M1, M2, T0, T1, T2, T3, T4, T5
    }

    /**
     * Plant types.
     *
     * @author Timothy Ham
     */
    public enum PlantType {
        NULL, EMS, OVER_EXPRESSION, RNAI, REPORTER, T_DNA, OTHER
    }

    @Column(name = "homozygosity", nullable = false)
    private String homozygosity;

    @Column(name = "ecotype", nullable = false)
    private String ecotype;

    @Column(name = "harvest_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date harvestDate;

    @Column(name = "parents", nullable = false)
    private String parents;

    @Column(name = "generation", nullable = false)
    @Enumerated(EnumType.STRING)
    private Generation generation;

    @Column(name = "plant_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlantType plantType;

    @Column(name = "sentToABRC")
    private Boolean sentToABRC = Boolean.FALSE;

    public ArabidopsisSeed() {
        setRecordType(EntryType.ARABIDOPSIS.getName());
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

    public Boolean isSentToABRC() {
        return sentToABRC;
    }

    public void setSentToABRC(Boolean sentToABRC) {
        if (sentToABRC != null)
            this.sentToABRC = sentToABRC;
    }
}
