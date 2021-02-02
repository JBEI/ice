package org.jbei.ice.lib.entry.sequence.composers.formatters;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.storage.model.AnnotationLocation;
import org.jbei.ice.storage.model.Sequence;
import org.jbei.ice.storage.model.SequenceFeature;

import java.io.IOException;
import java.io.OutputStream;
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
    public void format(Sequence sequence, OutputStream outputStream) throws IOException {
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

        String sequenceId = sequence.getEntry().getPartNumber();
        sequenceId = sequenceId.replaceAll("[^a-zA-Z0-9.:^*$@!+_?-|]", "_");
        Set<SequenceFeature> featureSet = sequence.getSequenceFeatures();
        if (featureSet != null) {
            for (SequenceFeature sequenceFeature : featureSet) {
                String featureType = sequenceFeature.getFeature() != null && !StringUtils.isEmpty(sequenceFeature.getFeature().getGenbankType()) ? sequenceFeature.getFeature().getGenbankType() : sequenceFeature.getGenbankType();
                String featureLine = sequenceFeature.getName() + " ICE " + featureType;

                // location
                for (AnnotationLocation location : sequenceFeature.getAnnotationLocations()) {
                    builder.append(sequenceId).append("\t")
                            .append(".").append("\t")
                            .append(getColumn3(sequenceFeature.getGenbankType())).append("\t")
                            .append(location.getGenbankStart()).append("\t")
                            .append(location.getEnd()).append("\t")
                            .append(".").append("\t")
                            .append(sequenceFeature.getStrand() == 1 ? "+" : "-").append("\t")
                            .append(".").append("\t")
                            .append("ID=").append(featureLine);
                    builder.append(System.lineSeparator());
                }
            }
        }

        outputStream.write(builder.toString().getBytes());
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
