package org.jbei.ice.lib.shared.dto.entry;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.lib.shared.dto.IDTOModel;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ArabidopsisSeedData extends PartData {

    private static final long serialVersionUID = 1L;

    public enum Generation implements IDTOModel {
        UNKNOWN, M0, M1, M2, T0, T1, T2, T3, T4, T5;

        public static ArrayList<String> getDisplayList() {
            ArrayList<String> list = new ArrayList<String>();
            for (Generation option : Generation.values()) {
                list.add(option.name());
            }
            return list;
        }

        public static Object displayToEnum(String value) {
            for (Generation option : Generation.values()) {
                if (value.equalsIgnoreCase(option.name()))
                    return option;
            }
            return null;
        }
    }

    public enum PlantType implements IsSerializable {
        EMS("EMS"), OVER_EXPRESSION("Over Expression"), RNAI("RNAi"), REPORTER("Reporter"), T_DNA(
                "T-DNA"), OTHER("Other"), NULL("");

        private String display;

        PlantType() {
        }

        PlantType(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return this.display;
        }

        public static ArrayList<String> getDisplayList() {
            ArrayList<String> list = new ArrayList<String>();
            for (PlantType option : PlantType.values()) {
                list.add(option.display);
            }
            return list;
        }

        public static Object displayToEnum(String value) {

            for (PlantType option : PlantType.values()) {
                if (value.equalsIgnoreCase(option.toString()))
                    return option;
            }
            return null;
        }
    }

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
