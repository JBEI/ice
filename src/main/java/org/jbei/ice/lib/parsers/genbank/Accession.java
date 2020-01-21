package org.jbei.ice.lib.parsers.genbank;

import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.utils.Utils;

import java.util.Arrays;

public class Accession extends Tag {

    public Accession(FeaturedDNASequence sequence) {
        super(sequence);
    }

    @Override
    public void process(String line) {
        String value = "";
        final String[] lines = line.split("\n");
        final String[] firstLine = lines[0].split(" +");
        if (firstLine.length == 1) {
            // empty value'
            sequence.setIdentifier("");
        } else {
            firstLine[0] = "";
            value = Utils.join(" ", Arrays.asList(firstLine));
            lines[0] = "";
            for (int i = 1; i < lines.length; i++) {
                lines[i] = lines[i].trim();
            }
            value = value + " " + Utils.join(" ", Arrays.asList(lines));
        }
        sequence.setIdentifier(value.trim());
    }
}
