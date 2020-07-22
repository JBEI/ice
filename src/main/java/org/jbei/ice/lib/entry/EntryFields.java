package org.jbei.ice.lib.entry;

import org.jbei.ice.lib.dto.entry.EntryFieldLabel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class EntryFields {

    public static List<EntryFieldLabel> getCommonFields() {
        List<EntryFieldLabel> list = new ArrayList<>();
        list.add(EntryFieldLabel.PI);
        list.add(EntryFieldLabel.PI_EMAIL);
        list.add(EntryFieldLabel.FUNDING_SOURCE);
        list.add(EntryFieldLabel.IP);
        list.add(EntryFieldLabel.BIO_SAFETY_LEVEL);
        list.add(EntryFieldLabel.NAME);
        list.add(EntryFieldLabel.ALIAS);
        list.add(EntryFieldLabel.KEYWORDS);
        list.add(EntryFieldLabel.SUMMARY);
        list.add(EntryFieldLabel.NOTES);
        list.add(EntryFieldLabel.REFERENCES);
        list.add(EntryFieldLabel.LINKS);
        list.add(EntryFieldLabel.STATUS);
        list.add(EntryFieldLabel.CREATOR);
        list.add(EntryFieldLabel.CREATOR_EMAIL);
        return list;
    }

    public static void addStrainHeaders(List<EntryFieldLabel> list) {
        list.add(EntryFieldLabel.HOST);
        list.add(EntryFieldLabel.GENOTYPE_OR_PHENOTYPE);
        list.add(EntryFieldLabel.SELECTION_MARKERS);
    }

    public static void addPlasmidHeaders(List<EntryFieldLabel> list) {
        list.add(EntryFieldLabel.CIRCULAR);
        list.add(EntryFieldLabel.BACKBONE);
        list.add(EntryFieldLabel.PROMOTERS);
        list.add(EntryFieldLabel.REPLICATES_IN);
        list.add(EntryFieldLabel.ORIGIN_OF_REPLICATION);
        list.add(EntryFieldLabel.SELECTION_MARKERS);
    }

    public static void addArabidopsisSeedHeaders(List<EntryFieldLabel> list) {
        list.add(EntryFieldLabel.HOMOZYGOSITY);
        list.add(EntryFieldLabel.HARVEST_DATE);
        list.add(EntryFieldLabel.ECOTYPE);
        list.add(EntryFieldLabel.PARENTS);
        list.add(EntryFieldLabel.GENERATION);
        list.add(EntryFieldLabel.PLANT_TYPE);
        list.add(EntryFieldLabel.SELECTION_MARKERS);
        list.add(EntryFieldLabel.SENT_TO_ABRC);
    }

    public static void addProteinHeaders(List<EntryFieldLabel> list) {
        list.add(EntryFieldLabel.ORGANISM);
        list.add(EntryFieldLabel.FULL_NAME);
        list.add(EntryFieldLabel.GENE_NAME);
        list.add(EntryFieldLabel.UPLOADED_FROM);
    }
}
