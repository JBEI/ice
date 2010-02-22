package org.jbei.ice.lib.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.FileUtils;

public abstract class AbstractParser {
    public abstract String getName();

    public Sequence parse(File file) throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        Sequence sequence = null;

        String textSequence = FileUtils.readFileToString(file);

        sequence = parse(textSequence);

        return sequence;
    }

    public abstract Sequence parse(String textSequence) throws InvalidFormatParserException;
}
