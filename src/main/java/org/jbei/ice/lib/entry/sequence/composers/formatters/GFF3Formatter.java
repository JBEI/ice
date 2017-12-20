package org.jbei.ice.lib.entry.sequence.composers.formatters;

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

    @Override
    public void format(Sequence sequence, OutputStream outputStream) throws FormatterException, IOException {
        if (sequence == null)
            throw new IllegalArgumentException("Cannot write null sequence");

        StringBuilder builder = new StringBuilder();

        builder.append("##gff-version 3").append(System.lineSeparator());
        Set<SequenceFeature> featureSet = sequence.getSequenceFeatures();
        if (featureSet != null) {
            for (SequenceFeature sequenceFeature : featureSet) {
                String featureLine = sequenceFeature.getName() + " ICE " + sequenceFeature.getGenbankType();

                // location
                for (AnnotationLocation location : sequenceFeature.getAnnotationLocations()) {
                    builder.append(featureLine).append("\t")
                            .append(location.getGenbankStart()).append("\t")
                            .append(location.getEnd()).append(" ")
                            .append(".\t")
                            .append(sequenceFeature.getStrand() == 1 ? "+ " : "- ")
                            .append(".\t");
                    builder.append(System.lineSeparator());
                }
            }
        }

        outputStream.write(builder.toString().getBytes());
    }
}
