package org.jbei.ice.lib.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jbei.ice.lib.vo.IDNASequence;

public interface IDNAParser {
    String getName();

    IDNASequence parse(byte[] bytes) throws InvalidFormatParserException;

    IDNASequence parse(File file) throws FileNotFoundException, IOException,
            InvalidFormatParserException;

    IDNASequence parse(String textSequence) throws InvalidFormatParserException;
}
