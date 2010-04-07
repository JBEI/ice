package org.jbei.ice.lib.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jbei.ice.lib.utils.FileUtils;
import org.jbei.ice.lib.vo.FeaturedDNASequence;

public abstract class AbstractParser {
    public abstract String getName();

    public FeaturedDNASequence parse(File file) throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        FeaturedDNASequence sequence = null;

        String textSequence = FileUtils.readFileToString(file);

        sequence = parse(textSequence);

        return sequence;
    }

    public abstract FeaturedDNASequence parse(String textSequence)
            throws InvalidFormatParserException;
}
