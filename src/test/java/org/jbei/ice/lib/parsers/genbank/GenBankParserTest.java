package org.jbei.ice.lib.parsers.genbank;

import org.jbei.ice.lib.dto.DNASequence;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Hector Plahar
 */
public class GenBankParserTest {

    @Test
    public void testParse() throws Exception {
        GenBankParser parser = new GenBankParser();
        DNASequence sequence = parser.parse(genbank);
        Assert.assertNotNull(sequence);
        FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) sequence;
        Assert.assertEquals(3, featuredDNASequence.getFeatures().size());
        Assert.assertEquals(936, featuredDNASequence.getSequence().length());

        // parse genbank 2
        FeaturedDNASequence sequence2 = (FeaturedDNASequence) parser.parse(getGenbank2);
        Assert.assertNotNull(sequence2);
        Assert.assertEquals(3, sequence2.getFeatures().size());
    }

    @Test
    public void testParseFeaturesTag() throws Exception {
        GenBankParser parser = new GenBankParser();
        Tag tag = new Tag(Tag.Type.REGULAR);
        tag.setKey("FEATURES");
        tag.setRawBody(features);
        FeaturesTag featuresTag = parser.parseFeaturesTag(tag);
        Assert.assertNotNull(featuresTag);
        Assert.assertEquals(3, featuresTag.getFeatures().size());

        tag.setRawBody(features2);
        featuresTag = parser.parseFeaturesTag(tag);
        Assert.assertNotNull(featuresTag);
        Assert.assertEquals(3, featuresTag.getFeatures().size());
        Assert.assertEquals(8, featuresTag.getFeatures().get(0).getNotes().size());
        Assert.assertEquals(1, featuresTag.getFeatures().get(1).getNotes().size());
        Assert.assertEquals(7, featuresTag.getFeatures().get(2).getNotes().size());

        // parse features 3
        tag.setRawBody(features3);
        featuresTag = parser.parseFeaturesTag(tag);
        Assert.assertNotNull(featuresTag);
        Assert.assertEquals(4, featuresTag.getFeatures().size());
        Assert.assertEquals(2, featuresTag.getFeatures().get(0).getNotes().size());
        Assert.assertEquals(2, featuresTag.getFeatures().get(1).getNotes().size());
        Assert.assertEquals(1, featuresTag.getFeatures().get(2).getNotes().size());
        Assert.assertEquals(5, featuresTag.getFeatures().get(3).getNotes().size());
    }

    public static String features =
            "FEATURES             Location/Qualifiers\n" +
                    "     RBS             1..12\n" +
                    "     CDS             19..774\n" +
                    "     terminator      808..936\n";

    public static String features2 =
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

    public static String features3 =
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

    public static String genbank =
            "LOCUS       BBa_I0462                936 bp    DNA     linear   UNK 11-May-2016\n" +
                    "DEFINITION  LuxR protein generator\n" +
                    "ACCESSION   BBa_I0462\n" +
                    "FEATURES             Location/Qualifiers\n" +
                    "     RBS             1..12\n" +
                    "     CDS             19..774\n" +
                    "     terminator      808..936\n" +
                    "ORIGIN\n" +
                    "        1 aaagaggaga aatactagat gaaaaacata aatgccgacg acacatacag aataattaat\n" +
                    "       61 aaaattaaag cttgtagaag caataatgat attaatcaat gcttatctga tatgactaaa\n" +
                    "      121 atggtacatt gtgaatatta tttactcgcg atcatttatc ctcattctat ggttaaatct\n" +
                    "      181 gatatttcaa tcctagataa ttaccctaaa aaatggaggc aatattatga tgacgctaat\n" +
                    "      241 ttaataaaat atgatcctat agtagattat tctaactcca atcattcacc aattaattgg\n" +
                    "      301 aatatatttg aaaacaatgc tgtaaataaa aaatctccaa atgtaattaa agaagcgaaa\n" +
                    "      361 acatcaggtc ttatcactgg gtttagtttc cctattcata cggctaacaa tggcttcgga\n" +
                    "      421 atgcttagtt ttgcacattc agaaaaagac aactatatag atagtttatt tttacatgcg\n" +
                    "      481 tgtatgaaca taccattaat tgttccttct ctagttgata attatcgaaa aataaatata\n" +
                    "      541 gcaaataata aatcaaacaa cgatttaacc aaaagagaaa aagaatgttt agcgtgggca\n" +
                    "      601 tgcgaaggaa aaagctcttg ggatatttca aaaatattag gttgcagtga gcgtactgtc\n" +
                    "      661 actttccatt taaccaatgc gcaaatgaaa ctcaatacaa caaaccgctg ccaaagtatt\n" +
                    "      721 tctaaagcaa ttttaacagg agcaattgat tgcccatact ttaaaaatta ataacactga\n" +
                    "      781 tagtgctagt gtagatcact actagagcca ggcatcaaat aaaacgaaag gctcagtcga\n" +
                    "      841 aagactgggc ctttcgtttt atctgttgtt tgtcggtgaa cgctctctac tagagtcaca\n" +
                    "      901 ctggctcacc ttcgggtggg cctttctgcg tttata\n" +
                    "//\n";


    public static String getGenbank2 =
            "LOCUS       AB648464                 452 bp    DNA     linear   BCT 05-SEP-2013\n" +
                    "DEFINITION  Escherichia coli mdh gene for malate dehydrogenase, partial cds,\n" +
                    "            serovar: OUT:H34, isolate: EC04-81.\n" +
                    "ACCESSION   AB648464\n" +
                    "VERSION     AB648464.1  GI:374345322\n" +
                    "KEYWORDS    .\n" +
                    "SOURCE      Escherichia coli\n" +
                    "  ORGANISM  Escherichia coli\n" +
                    "            Bacteria; Proteobacteria; Gammaproteobacteria; Enterobacteriales;\n" +
                    "            Enterobacteriaceae; Escherichia.\n" +
                    "REFERENCE   1\n" +
                    "  AUTHORS   Ooka,T., Seto,K., Kawano,K., Kobayashi,H., Etoh,Y., Ichihara,S.,\n" +
                    "            Kaneko,A., Isobe,J., Yamaguchi,K., Horikawa,K., Gomes,T.A.,\n" +
                    "            Linden,A., Bardiau,M., Mainil,J.G., Beutin,L., Ogura,Y. and\n" +
                    "            Hayashi,T.\n" +
                    "  TITLE     Clinical significance of Escherichia albertii\n" +
                    "  JOURNAL   Emerging Infect. Dis. 18 (3), 488-492 (2012)\n" +
                    "   PUBMED   22377117\n" +
                    "  REMARK    DOI:10.3201/eid1803.111401\n" +
                    "REFERENCE   2  (bases 1 to 452)\n" +
                    "  AUTHORS   Hayashi,T. and Ooka,T.\n" +
                    "  TITLE     Direct Submission\n" +
                    "  JOURNAL   Submitted (22-JUL-2011) Contact:Tetsuya Hayashi University of\n" +
                    "            Miyazaki, Division of Bioenvironmental Science, Frontier Science\n" +
                    "            Research Center; Kihara 5200, Kiyotake, Miyazaki 889-1692, Japan\n" +
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
                    "                     VTILPLLSQVPGVSFTEQEVADLTKRIQNAGTEVVEAKAGGGSATLSMG\"\n" +
                    "ORIGIN      \n" +
                    "        1 ggcgtagcgc gtaaaccggg tatggatcgt tccgacctgt ttaacgttaa cgccggcatc\n" +
                    "       61 gtgaaaaacc tggtacagca agttgcgaaa acctgcccga aagcgtgcat tggtattatt\n" +
                    "      121 actaacccgg ttaacactac agttgcgatt gctgctgaag tgctgaaaaa agccggtgtt\n" +
                    "      181 tatgacaaaa acaaactgtt cggcgttacc acgctggata tcattcgttc caacaccttt\n" +
                    "      241 gttgcggaac tgaaaggcaa acagccaggc gaagttgaag tgccggttat tggtggtcac\n" +
                    "      301 tctggtgtta ccattctgcc gctgctgtca caggttcctg gcgttagttt taccgagcag\n" +
                    "      361 gaagtggctg atctgaccaa acgtatccag aacgcgggta ctgaggtggt tgaagcgaaa\n" +
                    "      421 gccggtggcg ggtctgcaac cctgtctatg gg\n" +
                    "//\n";
}