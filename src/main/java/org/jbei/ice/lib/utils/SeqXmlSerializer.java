package org.jbei.ice.lib.utils;

import java.util.HashSet;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;
import org.jbei.ice.controllers.SequenceController;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureNote;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

/**
 * 
 * @author Timothy Ham
 * 
 */
public class SeqXmlSerializer {

    public static Namespace seqNamespace = new Namespace("seq", "http://jbei.org/sequence");
    public static Namespace xsiNamespace = new Namespace("xsi",
            "http://www.w3.org/2001/XMLSchema-instance");

    public static Document serializeToSeqXml(Sequence sequence) {
        Document document = DocumentHelper.createDocument();

        document.setRootElement(serializeToSeqXmlAsElement(sequence));

        return document;
    }

    public static Element serializeToSeqXmlAsElement(Sequence sequence) {
        Element seq = new DefaultElement("seq", seqNamespace);

        HashSet<String> validGenbankTypes = new HashSet<String>();
        initializeValidGenbankTypes(validGenbankTypes);

        seq.add(seqNamespace);
        seq.add(xsiNamespace);
        seq.addAttribute(new QName("schemaLocation", xsiNamespace),
            "http://jbei.org/sequence seq.xsd");
        Entry entry = sequence.getEntry();

        seq.add(new DefaultElement("name", seqNamespace).addText(entry.getNamesAsString()));

        Boolean circular = false;
        if (entry.getRecordType().equals("plasmid")) {
            Plasmid plasmid = (Plasmid) entry;
            circular = plasmid.getCircular();
        }

        seq.add(new DefaultElement("circular", seqNamespace).addText(circular.toString()));

        seq.add(new DefaultElement("sequence", seqNamespace).addText(sequence.getSequence()));

        if (sequence.getSequenceFeatures().size() != 0) {
            DefaultElement features = new DefaultElement("features", seqNamespace);

            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                DefaultElement feature = new DefaultElement("feature", seqNamespace);
                feature.add(new DefaultElement("label", seqNamespace).addText(sequenceFeature
                        .getName()));
                String complement;
                if (sequenceFeature.getStrand() == -1) {
                    complement = "true";
                } else {
                    complement = "false";
                }
                feature.add(new DefaultElement("complement", seqNamespace).addText(complement));
                String unRecognizedGenbankType = null;
                if (validGenbankTypes.contains(sequenceFeature.getGenbankType())) {
                    feature.add(new DefaultElement("type", seqNamespace).addText(sequenceFeature
                            .getGenbankType()));
                } else {
                    feature.add(new DefaultElement("type", seqNamespace).addText("misc_feature"));
                    unRecognizedGenbankType = sequenceFeature.getGenbankType();
                }

                DefaultElement locations = new DefaultElement("location", seqNamespace);
                locations.add(new DefaultElement("genbankStart", seqNamespace).addText(String
                        .valueOf(sequenceFeature.getGenbankStart())));
                locations.add(new DefaultElement("end", seqNamespace).addText(String
                        .valueOf(sequenceFeature.getEnd())));
                feature.add(locations);

                feature.add(new DefaultElement("attribute", seqNamespace).addText(
                    sequenceFeature.getDescription()).addAttribute("name", "unparsed_attribute"));
                String seqHash = Utils.encryptSha256(sequenceFeature.getFeature().getSequence());
                feature.add(new DefaultElement("seqHash", seqNamespace).addText(seqHash));
                features.add(feature);
            }
            seq.add(features);
        }
        return seq;
    }

    public static Sequence parseSeqXml(String xml) throws UtilityException {
        Document seqDocument = null;

        try {
            seqDocument = DocumentHelper.parseText(xml);
        } catch (DocumentException e) {
            throw new UtilityException(e);
        }

        Element seq = seqDocument.getRootElement();

        return parseSeqXml(seq);

    }

    public static Sequence parseSeqXml(Element seqElement) throws UtilityException {

        FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence();
        featuredDNASequence.setSequence(seqElement.elementText("sequence"));

        if (seqElement.element("features") != null) {
            for (Object feature : seqElement.element("features").elements("feature")) {
                DNAFeature dnaFeature = new DNAFeature();

                Element featureElement = (Element) feature;

                dnaFeature.setName(featureElement.elementText("label"));
                if ("true".equals(featureElement.elementText("complement"))) {
                    dnaFeature.setStrand(-1);
                } else {
                    dnaFeature.setStrand(1);
                }
                dnaFeature.setType(featureElement.elementText("type"));

                // TODO handle multilocation
                @SuppressWarnings("unchecked")
                List<Element> locations = featureElement.elements("location");
                int genbankStart = 0;
                int end = 0;
                Element location = locations.get(0);
                try {
                    genbankStart = Integer.parseInt(location.elementText("genbankStart"));
                    end = Integer.parseInt(location.elementText("end"));
                    dnaFeature.setGenbankStart(genbankStart);
                    dnaFeature.setEnd(end);
                } catch (NumberFormatException e) {
                    throw new UtilityException(e);
                }

                @SuppressWarnings("unchecked")
                List<Element> attributes = featureElement.elements("attribute");
                for (Element attribute : attributes) {
                    DNAFeatureNote featureNote = new DNAFeatureNote();
                    featureNote.setName(attribute.attributeValue("name"));
                    featureNote.setValue(attribute.getText());
                    if ("true".equals(attribute.attributeValue("quoted"))) {
                        featureNote.setValue("\"" + featureNote.getValue() + "\"");
                    }
                    dnaFeature.addNote(featureNote);
                }
                featuredDNASequence.getFeatures().add(dnaFeature);
            } // end feature loop
        }
        Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);
        sequence.setSequenceUser(seqElement.asXML());

        return sequence;
    }

    private static void initializeValidGenbankTypes(HashSet<String> validGenbankTypes) {
        validGenbankTypes.add("allele");
        validGenbankTypes.add("attenuator");
        validGenbankTypes.add("C_region");
        validGenbankTypes.add("CAAT_signal");
        validGenbankTypes.add("CDS");
        validGenbankTypes.add("conflict");
        validGenbankTypes.add("D-loop");
        validGenbankTypes.add("D_segment");
        validGenbankTypes.add("enhancer");
        validGenbankTypes.add("exon");
        validGenbankTypes.add("gene");
        validGenbankTypes.add("GC_signal");
        validGenbankTypes.add("iDNA");
        validGenbankTypes.add("intron");
        validGenbankTypes.add("J_region");
        validGenbankTypes.add("LTR");
        validGenbankTypes.add("mat_peptide");
        validGenbankTypes.add("misc_binding");
        validGenbankTypes.add("misc_difference");
        validGenbankTypes.add("misc_feature");
        validGenbankTypes.add("misc_recomb");
        validGenbankTypes.add("misc_RNA");
        validGenbankTypes.add("misc_signal");
        validGenbankTypes.add("misc_structure");
        validGenbankTypes.add("modified_base");
        validGenbankTypes.add("mRNA");
        validGenbankTypes.add("mutation");
        validGenbankTypes.add("N_region");
        validGenbankTypes.add("old_sequence");
        validGenbankTypes.add("polyA_signal");
        validGenbankTypes.add("polyA_site");
        validGenbankTypes.add("precursor_RNA");
        validGenbankTypes.add("prim_transcript");
        validGenbankTypes.add("primer");
        validGenbankTypes.add("primer_bind");
        validGenbankTypes.add("promoter");
        validGenbankTypes.add("protein_bind");
        validGenbankTypes.add("RBS");
        validGenbankTypes.add("rep_origin");
        validGenbankTypes.add("repeat_region");
        validGenbankTypes.add("repeat_unit");
        validGenbankTypes.add("rRNA");
        validGenbankTypes.add("S_region");
        validGenbankTypes.add("satellite");
        validGenbankTypes.add("scRNA");
        validGenbankTypes.add("sig_peptide");
        validGenbankTypes.add("snRNA");
        validGenbankTypes.add("source");
        validGenbankTypes.add("stem_loop");
        validGenbankTypes.add("STS");
        validGenbankTypes.add("TATA_signal");
        validGenbankTypes.add("terminator");
        validGenbankTypes.add("transit_peptide");
        validGenbankTypes.add("transposon");
        validGenbankTypes.add("tRNA");
        validGenbankTypes.add("unsure");
        validGenbankTypes.add("V_region");
        validGenbankTypes.add("variation");
        validGenbankTypes.add("-10_signal");
        validGenbankTypes.add("-35_signal");
        validGenbankTypes.add("3'clip");
        validGenbankTypes.add("3'UTR");
        validGenbankTypes.add("5'clip");
        validGenbankTypes.add("5'UTR");
    }

}