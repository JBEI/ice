package org.jbei.ice.lib.composers.formatters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.ontology.InvalidTermException;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.RichAnnotation;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleNote;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichLocation.Strand;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichFeature;
import org.biojavax.bio.seq.SimpleRichLocation;
import org.biojavax.bio.seq.SimpleRichSequence;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.SequenceFeatureAttribute;

public class GenbankFormatter extends AbstractFormatter {
    private String name;
    private String accessionNumber;
    private int version = 1;
    private double seqVersion = 1.0;
    private boolean circular = false;
    private String description = "";
    private String division = "";
    private String identifier = "";

    public GenbankFormatter(String name) {
        this(name, name, 1, 1.0);
    }

    public GenbankFormatter(String name, String accessionNumber, int version, double seqVersion) {
        super();

        this.name = name;
        this.accessionNumber = accessionNumber;
        this.version = version;
        this.seqVersion = seqVersion;
    }

    public boolean getCircular() {
        return circular;
    }

    public void setCircular(boolean value) {
        circular = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException,
            IOException {
        if (sequence == null || outputStream == null || sequence.getSequence().isEmpty()) {
            return;
        }

        SimpleRichSequence simpleRichSequence = null;
        try {
            simpleRichSequence = new SimpleRichSequence(getNamespace(), normalizeLocusName(name),
                    accessionNumber, version, DNATools.createDNA(sequence.getSequence()),
                    seqVersion);

            simpleRichSequence.setCircular(getCircular());
            if (getDescription() != null && !getDescription().isEmpty()) {
                simpleRichSequence.setDescription(getDescription());
            }

            if (getDivision() != null && !getDivision().isEmpty()) {
                simpleRichSequence.setDivision(getDivision());
            }

            if (getIdentifier() != null && !getIdentifier().isEmpty()) {
                simpleRichSequence.setIdentifier(getIdentifier());
            }

            if (sequence.getSequenceFeatures() != null && sequence.getSequenceFeatures().size() > 0) {
                Set<Feature> featureSet = new LinkedHashSet<Feature>();

                for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                    if (sequenceFeature.getFeature() == null) {
                        Logger.warn("In sequence with id: " + sequence.getId()
                                + "; SequenceFeature object has no feature assigned to it.");

                        continue;
                    }

                    RichFeature.Template featureTemplate = new RichFeature.Template();
                    featureTemplate.annotation = getAnnotations(sequenceFeature);
                    featureTemplate.location = new SimpleRichLocation(new SimplePosition(
                            sequenceFeature.getGenbankStart()), new SimplePosition(
                            sequenceFeature.getEnd()), 1, getStrand(sequenceFeature));

                    featureTemplate.source = getDefaultFeatureSource();
                    featureTemplate.type = getFeatureType(sequenceFeature);
                    featureTemplate.rankedCrossRefs = new TreeSet<Object>();

                    featureSet.add(new SimpleRichFeature(simpleRichSequence, featureTemplate));
                }

                simpleRichSequence.setFeatureSet(featureSet);
            }
        } catch (IllegalSymbolException e) {
            throw new FormatterException("Failed to create generate genbank file", e);
        } catch (ChangeVetoException e) {
            throw new FormatterException("Failed to create generate genbank file", e);
        } catch (InvalidTermException e) {
            throw new FormatterException("Failed to create generate genbank file", e);
        }

        RichSequence.IOTools.writeGenbank(outputStream, simpleRichSequence, getNamespace());
    }

    protected Strand getStrand(SequenceFeature sequenceFeature) {
        Strand strand;

        if (sequenceFeature.getStrand() == -1) {
            strand = Strand.NEGATIVE_STRAND;
        } else if (sequenceFeature.getStrand() == 1) {
            strand = Strand.POSITIVE_STRAND;
        } else {
            strand = Strand.UNKNOWN_STRAND;
        }

        return strand;
    }

    protected RichAnnotation getAnnotations(SequenceFeature sequenceFeature) {
        RichAnnotation richAnnotation = new SimpleRichAnnotation();

        if (sequenceFeature.getName() != null && !sequenceFeature.getName().isEmpty()) {
            richAnnotation
                    .addNote(new SimpleNote(RichObjectFactory.getDefaultOntology().getOrCreateTerm(
                        "label"), normalizeFeatureValue(sequenceFeature.getName()), 1));
        }

        int i = 0;
        for (SequenceFeatureAttribute attribute : sequenceFeature.getSequenceFeatureAttributes()) {
            String key = attribute.getKey();
            String value = attribute.getValue();

            if (key == null || key.isEmpty() || key.toLowerCase().equals("label")) { // skip invalid or feature with "label" note
                continue;
            }

            richAnnotation.addNote(new SimpleNote(RichObjectFactory.getDefaultOntology()
                    .getOrCreateTerm(key), normalizeFeatureValue(value), i + 2));
            i++;
        }

        return richAnnotation;
    }

    protected String getFeatureType(SequenceFeature sequenceFeature) {
        String featureType;

        if (sequenceFeature.getGenbankType() == null || sequenceFeature.getGenbankType().isEmpty()) {
            Logger.warn("SequenceFeature by id: " + sequenceFeature.getId()
                    + " has invalid genbank type.");

            featureType = "misc_feature";
        } else {
            featureType = sequenceFeature.getGenbankType();
        }

        return featureType;
    }

    protected String getDefaultFeatureSource() {
        return "org.jbei";
    }

    private String normalizeLocusName(String locusName) {
        if (locusName == null || locusName.isEmpty()) {
            return "";
        }

        /* Locus name has to be max 10 characters long */
        String result = locusName;

        if (locusName.length() > 10) {
            result = locusName.substring(0, 10);
        }

        return result;
    }

    private String normalizeFeatureValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String result = value.trim();
        while (true) {
            if (result.length() > 2) {
                if (result.charAt(0) == '"' || result.charAt(result.length() - 1) == '"') {
                    result = result.substring(1, result.length() - 1);

                    result = result.trim();
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        return result;
    }
}