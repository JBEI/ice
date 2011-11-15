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
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.SequenceFeatureAttribute;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.DNAFeatureNote;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

/**
 * SeqXML serializer/deserializer.
 * <p>
 * See seq.xsd in the /docs directory for xml schema.
 * 
 * @author Timothy Ham
 * 
 */
public class SeqXmlSerializer {

    private static final String SEQ_HASH = "seqHash";
    private static final String QUOTED = "quoted";
    private static final String NAME = "name";
    private static final String ATTRIBUTE = "attribute";
    private static final String END = "end";
    private static final String GENBANK_START = "genbankStart";
    private static final String LOCATION = "location";
    private static final String SEQUENCE = "sequence";
    private static final String LABEL = "label";
    private static final String CIRCULAR = "circular";
    private static final String FEATURE = "feature";
    private static final String FEATURES = "features";
    private static final String COMPLEMENT = "complement";
    public static Namespace seqNamespace = new Namespace("seq", "http://jbei.org/sequence");
    public static Namespace xsiNamespace = new Namespace("xsi",
            "http://www.w3.org/2001/XMLSchema-instance");

    /**
     * Generate seq-xml from the given {@link Sequence}.
     * 
     * @param sequence
     *            - Sequence to sereialize.
     * @return Xml document.
     * @throws UtilityException
     */
    public static Document serializeToSeqXml(Sequence sequence) throws UtilityException {
        Document document = DocumentHelper.createDocument();

        document.setRootElement(serializeToSeqXmlAsElement(sequence));

        return document;
    }

    /**
     * Generate seq xml {@link Element} from the give {@link Sequence}.
     * 
     * @param sequence
     *            - Sequence to serialize.
     * @return Xml Element.
     * @throws UtilityException
     */
    public static Element serializeToSeqXmlAsElement(Sequence sequence) throws UtilityException {
        Element seq = new DefaultElement("seq", seqNamespace);

        HashSet<String> validGenbankTypes = new HashSet<String>();
        initializeValidGenbankTypes(validGenbankTypes);

        seq.add(seqNamespace);
        seq.add(xsiNamespace);
        seq.addAttribute(new QName("schemaLocation", xsiNamespace),
            "http://jbei.org/sequence seq.xsd");
        Entry entry = sequence.getEntry();

        seq.add(new DefaultElement(NAME, seqNamespace).addText(entry.getNamesAsString()));

        Boolean circular = false;
        if (entry.getRecordType().equals("plasmid")) {
            Plasmid plasmid = (Plasmid) entry;
            circular = plasmid.getCircular();
        }

        seq.add(new DefaultElement(CIRCULAR, seqNamespace).addText(circular.toString()));

        seq.add(new DefaultElement(SEQUENCE, seqNamespace).addText(sequence.getSequence()));

        if (sequence.getSequenceFeatures().size() != 0) {
            DefaultElement features = new DefaultElement(FEATURES, seqNamespace);

            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                DefaultElement feature = new DefaultElement(FEATURE, seqNamespace);
                feature.add(new DefaultElement(LABEL, seqNamespace).addText(sequenceFeature
                        .getName()));
                String complement;
                if (sequenceFeature.getStrand() == -1) {
                    complement = "true";
                } else {
                    complement = "false";
                }
                feature.add(new DefaultElement(COMPLEMENT, seqNamespace).addText(complement));
                if (validGenbankTypes.contains(sequenceFeature.getGenbankType())) {
                    feature.add(new DefaultElement("type", seqNamespace).addText(sequenceFeature
                            .getGenbankType()));
                } else {
                    feature.add(new DefaultElement("type", seqNamespace).addText("misc_feature"));
                }

                for (AnnotationLocation location : sequenceFeature.getAnnotationLocations()) {
                    DefaultElement locations = new DefaultElement(LOCATION, seqNamespace);
                    locations.add(new DefaultElement(GENBANK_START, seqNamespace).addText(String
                            .valueOf(location.getGenbankStart())));
                    locations.add(new DefaultElement(END, seqNamespace).addText(String
                            .valueOf(location.getEnd())));
                    feature.add(locations);
                }
                for (SequenceFeatureAttribute sequenceFeatureAttribute : sequenceFeature
                        .getSequenceFeatureAttributes()) {
                    DefaultElement newAttribute = new DefaultElement(ATTRIBUTE, seqNamespace);
                    newAttribute.addText(sequenceFeatureAttribute.getValue());
                    newAttribute.addAttribute(NAME, sequenceFeatureAttribute.getKey());
                    newAttribute.addAttribute(QUOTED, sequenceFeatureAttribute.getQuoted()
                            .toString());
                    feature.add(newAttribute);
                }
                sequence.getSequence();
                String tempSequence = sequenceFeature.getFeature().getSequence();
                int genbankStart = sequenceFeature.getUniqueGenbankStart();
                int end = sequenceFeature.getUniqueEnd();

                // this is needed because sometimes sequence stored in this local database
                // is the reverse complement of what's being exported, and thus must be recalculated.
                if (genbankStart > end) {
                    tempSequence = sequence.getSequence().trim()
                            .substring(genbankStart - 1, sequence.getSequence().length());
                    tempSequence += sequence.getSequence().trim().substring(0, end);
                } else {
                    tempSequence = sequence.getSequence().trim().substring(genbankStart - 1, end);
                }
                if ("true".equals(complement)) {
                    tempSequence = SequenceUtils.reverseComplement(tempSequence);
                }

                String seqHash = Utils.encryptSha256(tempSequence);
                feature.add(new DefaultElement(SEQ_HASH, seqNamespace).addText(seqHash));
                features.add(feature);
            }
            seq.add(features);
        }
        return seq;
    }

    /**
     * Deserialize given xml to {@link Sequence} object.
     * 
     * @param xml
     *            - xml to parse.
     * @return Sequence object.
     * @throws UtilityException
     */
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

    /**
     * Deserialize given seq xml {@link Element} to {@link Sequence} object.
     * 
     * @param seqElement
     *            - xml Element to parse.
     * @return Sequence object.
     * @throws UtilityException
     */
    public static Sequence parseSeqXml(Element seqElement) throws UtilityException {

        FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence();
        featuredDNASequence.setSequence(seqElement.elementText(SEQUENCE));

        String circular = seqElement.elementText(CIRCULAR);
        featuredDNASequence.setIsCircular(CIRCULAR.equals(circular) ? true : false);

        if (seqElement.element(FEATURES) != null) {
            for (Object feature : seqElement.element(FEATURES).elements(FEATURE)) {
                DNAFeature dnaFeature = new DNAFeature();

                Element featureElement = (Element) feature;

                dnaFeature.setName(featureElement.elementText(LABEL));
                if ("true".equals(featureElement.elementText(COMPLEMENT))) {
                    dnaFeature.setStrand(-1);
                } else {
                    dnaFeature.setStrand(1);
                }
                dnaFeature.setType(featureElement.elementText("type"));

                @SuppressWarnings("unchecked")
                List<Element> locations = featureElement.elements(LOCATION);
                int genbankStart = 0;
                int end = 0;
                List<DNAFeatureLocation> dnaFeatureLocations = dnaFeature.getLocations();
                for (Element location : locations) {
                    try {
                        genbankStart = Integer.parseInt(location.elementText(GENBANK_START));
                        end = Integer.parseInt(location.elementText(END));
                        dnaFeatureLocations.add(new DNAFeatureLocation(genbankStart, end));
                    } catch (NumberFormatException e) {
                        throw new UtilityException(e);
                    }
                }

                @SuppressWarnings("unchecked")
                List<Element> attributes = featureElement.elements(ATTRIBUTE);
                for (Element attribute : attributes) {
                    DNAFeatureNote featureNote = new DNAFeatureNote();
                    featureNote.setName(attribute.attributeValue(NAME));
                    featureNote.setValue(attribute.getText());
                    if ("true".equals(attribute.attributeValue(QUOTED))) {
                        featureNote.setQuoted(true);
                    } else {
                        featureNote.setQuoted(false);
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

    /**
     * Add valid Genbank feature type keywords to the given HashSet.
     * 
     * @param validGenbankTypes
     */
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