package org.jbei.ice.lib.entry.sequence.composers.formatters;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.sbolstandard.core.util.SequenceOntology;

/**
 * @author Hector Plahar
 */
public class IceSequenceOntology {

    private static Map<String, String> map = new HashMap<>();

    static {
        map.put("misc_feature", "SO_0000001");
        map.put("misc_structure", "SO_0000002");
        map.put("satellite", "SO_0000005");
        map.put("scRNA", "SO_0000013");
        map.put("stem_loop", "SO_0000019");
        map.put("operator", "SO_0000057");
        map.put("protein", "SO_0000104");
        map.put("primer", "SO_0000112");
        map.put("RBS", "SO_0000139");
        map.put("attenuator", "SO_0000140");
        map.put("terminator", "SO_0000141");
        map.put("exon", "SO_0000147");
        map.put("source", "SO_0000149");
        map.put("plasmid", "SO_0000155");
        map.put("enhancer", "SO_0000165");
        map.put("promoter", "SO_0000167");
        map.put("CAAT_signal", "SO_0000172");
        map.put("GC_signal", "SO_0000173");
        map.put("TATA_signal", "SO_0000174");
        map.put("-10_signal", "SO_0000175");
        map.put("-35_signal", "SO_0000176");
        map.put("precursor_RNA", "SO_0000185");
        map.put("prim_transcript", "SO_0000185");
        map.put("intron", "SO_0000188");
        map.put("5'UTR", "SO_0000204");
        map.put("3'UTR", "SO_0000205");
        map.put("misc_RNA", "SO_0000233");
        map.put("mRNA", "SO_0000234");
        map.put("rRNA", "SO_0000252");
        map.put("tRNA", "SO_0000253");
        map.put("snRNA", "SO_0000274");
        map.put("LTR", "SO_0000286");
        map.put("rep_origin", "SO_0000296");
        map.put("D-loop", "SO_0000297");
        map.put("misc_recomb", "SO_0000298");
        map.put("modified_base", "SO_0000305");
        map.put("CDS", "SO_0000316");
        map.put("start", "SO_0000323");
        map.put("tag", "SO_0000324");
        map.put("stop", "SO_0000327");
        map.put("STS", "SO_0000331");
        map.put("misc_binding", "SO_0000409");
        map.put("protein_bind", "SO_0000410");
        map.put("misc_difference", "SO_0000413");
        map.put("protein_domain", "SO_0000417");
        map.put("sig_peptide", "SO_0000418");
        map.put("mat_peptide", "SO_0000419");
        map.put("D_segment", "SO_0000458");
        map.put("J_region", "SO_0000470");
        map.put("polyA_signal", "SO_0000551");
//        map.put("RBS", "SO_0000552"); // TODO : need to add for RBS and use 0000139 as default
        map.put("polyA_site", "SO_0000553");
        map.put("5'clip", "SO_0000555");
        map.put("3'clip", "SO_0000557");
        map.put("repeat_region", "SO_0000657");
        map.put("gene", "SO_0000704");
        map.put("iDNA", "SO_0000723");
        map.put("transit_peptide", "SO_0000725");
        map.put("repeat_unit", "SO_0000726");
        map.put("conserved", "SO_0000856");
        map.put("s_mutation", "SO_0001017");
        map.put("allele", "SO_0001023");
        map.put("transposon", "SO_0001054");
        map.put("variation", "SO_0001060");
        map.put("misc_marker", "SO_0001645");
        map.put("V_region", "SO_0001833");
        map.put("C_region", "SO_0001834");
        map.put("N_region", "SO_0001835");
        map.put("S_region", "SO_0001836");
        map.put("misc_signal", "SO_0005836");
        map.put("primer_bind", "SO_0005850");
    }

    public static URI getURI(String type) {
        if ("RBS".equalsIgnoreCase(type.trim()))
            return SequenceOntology.type("SO_0000552");

        String soNum = map.get(type);
        if (soNum == null)
            soNum = "SO_0000001";
        return SequenceOntology.type(soNum);
    }

    /**
     * @param ontology sequence ontology e.g,SO_0000298 for "mRNA"
     * @return genbank feature type e.g. "mRNA" using above example
     */
    public static String getFeatureType(String ontology) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(ontology))
                return entry.getKey();
        }

        if ("SO_0000552".equals(ontology))
            return "RBS";

        return "misc_feature";
    }
}
