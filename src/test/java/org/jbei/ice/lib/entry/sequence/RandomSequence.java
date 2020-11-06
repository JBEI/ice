package org.jbei.ice.lib.entry.sequence;

import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.DNAFeatureLocation;
import org.jbei.ice.lib.dto.FeaturedDNASequence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RandomSequence {

    private static final char[] xters = {'A', 'C', 'G', 'T'};

    private static final String[] tags = {"tag", "Fragment", "iDNA", "terminator", "protein_bind", "ORF", "repeat_unit", "-10_signal", "rRNA", "Site", "prim_transcript", "Cyp716A12", "Other", "mRNA", "attL1", "PCR_primer", "modified_base", "backbone", "coverage_below", "homology", "Promoter", "CdS", "Vector", "TATA_signal", "CDS_term", "allele", "CDS", "N_region", "Replicaton_ori", "protospacer", "polyA_signal", "sig_peptide", "site", "PCR_frame", "polyA_site", "coverage_above", "", "Primer", "tRNA", "transit_peptide", "mat_peptide", "operon", "enhancer", "stem_loop", "Gene", "3'UTR", "mutation", "loci", "transcript", "-35_signal", "V_region ", "CDSd", "mobile_element", "LZ_domain", "insert", "unsure", "misc_structure", "misc_RNA", "SAD1", "CBM", "vector", "misc_feature", "attenuator", "motif", "Rep_Origin", "intron", "misc_marker", "transposon", "Region", "LTR", "Reporter", "5'UTR", "source", "tmRNA", "regulatory", "aSDomain", "CDS_motif", "primer", "noncoding", "gap", "conflict", "selfdistr.gRNA", "plasmid", "primer_bind", "insertion_seq", "gene", "CYP51H10", "Cyp72A154", "S_region", "Marker", "ori_T", "attL2", "old_sequence", "variation", "Cyp72A65", "exon", "polylinker", "misc_binding", "erse", "centromere", "misc_signal", "CYP88D6", "target", "oriT", "rep_origin", "Cyp93E1", "linker", "prot_bind", "repeat_region", "ncRNA", "protein", "coverage_one", "misc_recomb", "promoter", "STS", "RBS", "misc_difference", "PCR_product", "J_segment", "ori"};

    private final long length;
    private final FeaturedDNASequence sequence;

    public RandomSequence(int length) {
        this.length = length;
        this.sequence = new FeaturedDNASequence();
    }

    public FeaturedDNASequence generate(int numberOfFeatures) {
        for (int i = 0; i < this.length; i += 1) {
            int index = ThreadLocalRandom.current().nextInt(0, xters.length);
            char c = xters[index];
            sequence.setSequence(sequence.getSequence() + c);
        }

        // generate 30 features
        for (int i = 0; i < numberOfFeatures; i += 1)
            generateRandomFeature();

        return this.sequence;
    }

    private void generateRandomFeature() {
        DNAFeature feature = new DNAFeature();
        int index = ThreadLocalRandom.current().nextInt(0, tags.length);
        feature.setType(tags[index]);
        String identifier = UUID.randomUUID().toString();
        feature.setName(identifier);
        feature.setIdentifier(identifier);
        int strand = ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
        feature.setStrand(strand);

        int start = (int) ThreadLocalRandom.current().nextLong(this.length);
        int end = (int) ThreadLocalRandom.current().nextLong(this.length);

        List<DNAFeatureLocation> locations = new ArrayList<>();
        DNAFeatureLocation location = new DNAFeatureLocation();
        location.setGenbankStart(start);
        location.setEnd(end);
        locations.add(location);

        feature.setLocations(locations);
        this.sequence.getFeatures().add(feature);
    }
}
