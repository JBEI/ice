package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.entry.sequence.SequenceFormat;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.PlainParser;
import org.jbei.ice.lib.parsers.fasta.FastaParser;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;
import org.jbei.ice.storage.model.AnnotationLocation;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Set;

/**
 * Formatter for GFF3 Format
 *
 * @author Hector Plahar
 */
public class GFF3Formatter extends AbstractFormatter {

    private final String[] HEADERS = new String[]{
            "seqid", "source", "type", "start", "end", "score", "strand", "phase", "attributes"
    };

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException, IOException {
        if (sequence == null)
            throw new IllegalArgumentException("Cannot write null sequence");

        StringBuilder builder = new StringBuilder();

        builder.append("##gff-version 3.2.1").append(System.lineSeparator());

        // add headers
        builder.append("##");
        for (String header : HEADERS) {
            builder.append(header).append("\t");
        }
        builder.append(System.lineSeparator());

        String sequenceId = getSequenceId(sequence);
        sequenceId = sequenceId.replaceAll("[^a-zA-Z0-9.:^*$@!+_?-|]", "_");
        Set<SequenceFeature> featureSet = sequence.getSequenceFeatures();
        if (featureSet != null) {
            for (SequenceFeature sequenceFeature : featureSet) {
                String featureLine = sequenceFeature.getName() + " ICE " + sequenceFeature.getGenbankType();

                // location
                for (AnnotationLocation location : sequenceFeature.getAnnotationLocations()) {
                    builder.append(sequenceId).append("\t")
                            .append(".").append("\t")
                            .append(getColumn3(sequenceFeature.getGenbankType())).append("\t")
                            .append(location.getGenbankStart()).append("\t")
                            .append(location.getEnd()).append("\t")
                            .append(".").append("\t")
                            .append(sequenceFeature.getStrand() == 1 ? "+ " : "- ").append("\t")
                            .append(".").append("\t")
                            .append("ID=").append(featureLine);
                    builder.append(System.lineSeparator());
                }
            }
        }

        outputStream.write(builder.toString().getBytes());
    }

    private String getSequenceId(Sequence sequence) {
        if (!StringUtils.isEmpty(sequence.getIdentifier()))
            return sequence.getIdentifier();

        if (StringUtils.isEmpty(sequence.getSequenceUser()))
            return sequence.getEntry().getPartNumber();

        try {
            AbstractParser parser;
            String sequenceString = sequence.getSequenceUser();

            switch (detectFormat(sequenceString)) {
                case GENBANK:
                    parser = new GenBankParser();
                    break;

                case FASTA:
                    parser = new FastaParser();
                    break;

                default:
                case PLAIN:
                    parser = new PlainParser();
                    break;
            }

            // parse actual sequence
            FeaturedDNASequence dnaSequence = parser.parse(sequenceString);
            return dnaSequence.getIdentifier();
        } catch (Exception e) {
            Logger.error(e);
            return sequence.getEntry().getPartNumber();
        }

    }

    protected SequenceFormat detectFormat(String sequenceString) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(sequenceString));
        String line = reader.readLine();
        if (line == null)
            throw new IOException("Could not obtain line from document");

        if (line.startsWith("LOCUS"))
            return SequenceFormat.GENBANK;

        if (line.startsWith(">"))
            return SequenceFormat.FASTA;

        if (line.startsWith("<"))
            return SequenceFormat.SBOL2;

        return SequenceFormat.PLAIN;
    }

    protected String getColumn3(String genbankType) {
        if (StringUtils.isEmpty(genbankType))
            return "region";

        switch (genbankType.toLowerCase()) {
            case "mutation":
                return "sequence variant obs";
            case "gene":
                return "gene";

            default:
                return "region";
        }
    }
}
