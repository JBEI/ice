package org.jbei.ice.lib.parsers.genbank;

import org.jbei.ice.lib.dto.DNAFeature;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.junit.Assert;
import org.junit.Test;

public class FeaturesTagTest {

    private static final String feature =
            "FEATURES             Location/Qualifiers\n" +
                    "CDS             join(691..693,768..1631)\n" +
                    "                     /codon_start=1\n" +
                    "                     /locus_tag=\"PAS_chr1-3_0057\"\n" +
                    "                     /label=SEC13\n" +
                    "                     /db_xref=\"GI:254564565\"\n" +
                    "                     /db_xref=\"GeneID:8197368\"\n" +
                    "                     /protein_id=\"XP_002489393.1\"\n" +
                    "                     /translation=\"MVTIGNAHDDLIHDAVLDYYGRRLATCSSDKTIKIFEIDGENQRL\n" +
                    "                     VETLIGHEGPVWQVAWAHPKFGVILASCSYDGKVLIWKEDNGVWNKVAEHSVHQASVNS\n" +
                    "                     VSWAPHEYGPVLLCASSDGKISIVEFKDGGALEPIVIQGHAIGVNAASWAPISLPDNTR\n" +
                    "                     RFVSGGCDNLVKIWRYDDAAKTFIEEEAFQGHSDWVRDVAWSPSRLSKSYIATASQDRT\n" +
                    "                     VLIWTKDGKSNKWEKQPLTKEKFPDVCWRASWSLSGNVLAISGGDNKVTLWKENIQGKW\n" +
                    "                     ESAGEVDQ\"";

    public static final String features =
            "FEATURES             Location/Qualifiers\n" +
                    "     RBS             1..12\n" +
                    "                     /=\n" +
                    "     CDS             19..774\n" +
                    "     terminator      808..936\n" +
                    "                     /ApEinfo_graphicformat=\"arrow_data {{0 1 2 0 0 -1} {} 0}\n" +
                    "                     width 5 offset 0";
    public static final String features2 =
            "FEATURES             Location/Qualifiers\n" +
                    "     source          1..452\n" +
                    "                     /organism=\"Escherichia coli\"\n" +
                    "                     /mol_type=\"genomic DNA\"\n" +
                    "                     /serovar=\"OUT:H34\"\n" +
                    "                     /isolate=\"EC04-81\"\n" +
                    "                     /isolation_source=\"feces\"\n" +
                    "                     /host=\"Homo sapiens\"\n" +
                    "                     /db_xref=\"taxon:562\"\n" +
                    "                     /country=\"Japan\"\n" +
                    "     gene            <1..>452\n" +
                    "                     /gene=\"mdh\"\n" +
                    "     CDS             <1..>452\n" +
                    "                     /gene=\"mdh\"\n" +
                    "                     /codon_start=1\n" +
                    "                     /transl_table=11\n" +
                    "                     /product=\"malate dehydrogenase\"\n" +
                    "                     /protein_id=\"BAL48036.1\"\n" +
                    "                     /db_xref=\"GI:374345323\"\n" +
                    "                     /translation=\"GVARKPGMDRSDLFNVNAGIVKNLVQQVAKTCPKACIGIITNPV\n" +
                    "                     NTTVAIAAEVLKKAGVYDKNKLFGVTTLDIIRSNTFVAELKGKQPGEVEVPVIGGHSG\n" +
                    "                     VTILPLLSQVPGVSFTEQEVADLTKRIQNAGTEVVEAKAGGGSATLSMG\"\n";
    public static final String features3 =
            "FEATURES             Location/Qualifiers\n" +
                    "     exon            255..457\n" +
                    "                     /number=3\n" +
                    "                     /gene=\"PGAM-M\"\n" +
                    "     intron          order(M55673:2559..>3688,<1..254)\n" +
                    "                     /number=2\n" +
                    "                     /gene=\"PGAM-M\"\n" +
                    "     mRNA            join(M55673:1820..2274,M55673:2378..2558,255..457)\n" +
                    "                     /gene=\"PGAM-M\"\n" +
                    "     CDS             join(M55673:1861..2274,M55673:2378..2558,255..421)\n" +
                    "                     /note=\"muscle-specific isozyme\"\n" +
                    "                     /gene=\"PGAM2\"\n" +
                    "                     /product=\"phosphoglycerate mutase\"\n" +
                    "                     /codon_start=1\n" +
                    "                     /translation=\"MATHRLVMVRHGESTWNQENRFCGWFDAELSEKGTEEAKRGAKA\n" +
                    "                     IKDAKMEFDICYTSVLKRAIRTLWAILDGTDQMWLPVVRTWRLNERHYGGLTGLNKAE\n" +
                    "                     TAAKHGEEQVKIWRRSFDIPPPPMDEKHPYYNSISKERRYAGLKPGELPTCESLKDTI\n" +
                    "                     ARALPFWNEEIVPQIKAGKRVLIAAHGNSLRGIVKHLEGMSDQAIMELNLPTGIPIVY\n" +
                    "                     ELNKELKPTKPMQFLGDEETVRKAMEAVAAQGKAK\"";

    @Test
    public void process() {
        FeaturedDNASequence sequence = new FeaturedDNASequence();
        FeaturesTag tag = new FeaturesTag(sequence);

        String[] lines = feature.split("\n");
        for (String line : lines)
            tag.process(line);

        Assert.assertNotNull(sequence);
        Assert.assertEquals(sequence.getFeatures().size(), 1);
        DNAFeature feature = sequence.getFeatures().get(0);

        Assert.assertEquals("CDS", feature.getType());
        Assert.assertEquals(2, feature.getLocations().size());
        Assert.assertEquals(7, feature.getNotes().size());
    }

    @Test
    public void processFeatures() {
        FeaturedDNASequence sequence = new FeaturedDNASequence();
        FeaturesTag tag = new FeaturesTag(sequence);
        String[] lines = features.split("\n");
        for (String line : lines)
            tag.process(line);

        Assert.assertNotNull(sequence);
        Assert.assertEquals(3, sequence.getFeatures().size());
    }

    @Test
    public void processFeatures2() {
        FeaturedDNASequence sequence = new FeaturedDNASequence();
        FeaturesTag tag = new FeaturesTag(sequence);
        String[] lines = features2.split("\n");
        for (String line : lines)
            tag.process(line);

        Assert.assertNotNull(sequence);
        Assert.assertEquals(3, sequence.getFeatures().size());

        // source
        DNAFeature feature = sequence.getFeatures().get(0);
        Assert.assertEquals(8, feature.getNotes().size());

        // gene
        feature = sequence.getFeatures().get(1);
        Assert.assertEquals(1, feature.getNotes().size());

        // cds
        feature = sequence.getFeatures().get(2);
        Assert.assertEquals(7, feature.getNotes().size());
    }

    @Test
    public void processFeatures3() {
        FeaturedDNASequence sequence = new FeaturedDNASequence();
        FeaturesTag tag = new FeaturesTag(sequence);
        String[] lines = features3.split("\n");
        for (String line : lines)
            tag.process(line);

        Assert.assertNotNull(sequence);
        Assert.assertEquals(4, sequence.getFeatures().size());

        // exon
        DNAFeature feature = sequence.getFeatures().get(0);
        Assert.assertEquals(2, feature.getNotes().size());

        // intron
        feature = sequence.getFeatures().get(1);
        Assert.assertEquals(2, feature.getNotes().size());

        // mRNA
        feature = sequence.getFeatures().get(2);
        Assert.assertEquals(1, feature.getNotes().size());

        // CDS
        feature = sequence.getFeatures().get(3);
        Assert.assertEquals(5, feature.getNotes().size());
    }
}