package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.FileUtils;

public abstract class AbstractParser {
    public Sequence parse(File file) throws FileNotFoundException, IOException {
        Sequence sequence = null;

        String textSequence = FileUtils.readFileToString(file);

        sequence = parse(textSequence);

        return sequence;
    }

    public Sequence parse(String textSequence) {
        Sequence sequence = parse(new BufferedReader(new StringReader(textSequence)));

        if (sequence != null) {
            sequence.setSequenceUser(textSequence);
        }

        return sequence;
    }

    public abstract Sequence parse(BufferedReader br);
}
