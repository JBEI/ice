package org.jbei.ice.storage.model;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.jbei.ice.lib.dto.entry.*;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Store Arabidopsis Seed specific fields.
 * <p>
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

    @Column(name = "homozygosity", nullable = false)
    @Field
    private String homozygosity;

    @Column(name = "ecotype", nullable = false)
    @Field
    private String ecotype;

    @Column(name = "harvest_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date harvestDate;

    @Column(name = "parents", nullable = false)
    @Field
    private String parents;

    @Column(name = "generation", nullable = false)
    @Enumerated(EnumType.STRING)
    @Field(analyze = Analyze.NO)
    private Generation generation;

    @Column(name = "plant_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Field(analyze = Analyze.NO)
    private PlantType plantType;

    @Column(name = "sentToABRC")
    private Boolean sentToABRC = Boolean.FALSE;

    public ArabidopsisSeed() {
        super();
        setRecordType(EntryType.ARABIDOPSIS.getName());
        setGeneration(Generation.UNKNOWN);
        setPlantType(PlantType.EMS);
        setEcotype("");
        setHomozygosity("");
        setParents("");
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

    @Override
    public PartData toDataTransferObject() {
        PartData data = super.toDataTransferObject();
        ArabidopsisSeedData seedData = new ArabidopsisSeedData();
        seedData.setEcotype(this.ecotype);
        seedData.setGeneration(this.generation);
        if (this.harvestDate != null) {
            DateFormat format = new SimpleDateFormat("MM/dd/YYYY");
            String dateFormat = format.format(this.harvestDate);
            seedData.setHarvestDate(dateFormat);
        }
        seedData.setHomozygosity(this.homozygosity);
        seedData.setSeedParents(this.parents);
        seedData.setPlantType(this.plantType);
        seedData.setSentToAbrc(this.sentToABRC);
        data.setArabidopsisSeedData(seedData);
        return data;
    }
}
