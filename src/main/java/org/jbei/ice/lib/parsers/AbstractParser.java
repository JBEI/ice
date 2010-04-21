package org.jbei.ice.lib.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jbei.ice.lib.vo.IDNASequence;

public abstract class AbstractParser implements IDNAParser {
    public abstract IDNASequence parse(String textSequence) throws InvalidFormatParserException;

    public abstract IDNASequence parse(byte[] bytes) throws InvalidFormatParserException;

    public IDNASequence parse(File file) throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        FileInputStream fileInputStream = new FileInputStream(file);

        int availableBytes = fileInputStream.available();
        byte[] bytes = new byte[availableBytes];
        fileInputStream.read(bytes);
        fileInputStream.close();
        return parse(bytes);
    }

    protected String cleanSequence(String sequence) {
        sequence = sequence.trim();
        sequence = sequence.replace("\n\n", "\n"); // *nix
        sequence = sequence.replace("\n\r\n\r", "\n\r"); // win
        sequence = sequence.replace("\r\r", "\r"); // mac

        return sequence;
    }
}
