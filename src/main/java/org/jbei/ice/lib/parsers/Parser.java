package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import org.jbei.ice.lib.models.Sequence;
import org.jbei.ice.lib.utils.FileUtils;
import org.jbei.ice.lib.utils.SequenceUtils;

public class Parser {
    public static Sequence parseGenbank(File file) throws ParserException {
        Sequence sequence = null;

        try {
            String genbankSequence = FileUtils.readFileToString(file);

            sequence = parseGenbank(genbankSequence);
        } catch (FileNotFoundException e) {
            throw new ParserException("File not found: " + file.getAbsolutePath(), e);
        } catch (IOException e) {
            throw new ParserException("IOException: " + file.getAbsolutePath(), e);
        } catch (Exception e) {
            throw new ParserException(e);
        }

        return sequence;
    }

    public static Sequence parseGenbank(String genbankSequence) throws ParserException {
        Sequence sequence = null;

        try {
            sequence = GenbankParser.parseGenbankDNAFile(new BufferedReader(new StringReader(genbankSequence)));

            sequence.setSequenceUser(genbankSequence);
            sequence.setFwdHash(SequenceUtils.calculateSequenceHash(sequence.getSequence()));
            sequence.setRevHash(SequenceUtils.calculateSequenceHash(SequenceUtils.reverseComplement(sequence
                    .getSequence())));
        } catch (Exception e) {
            throw new ParserException(e);
        }

        return sequence;
    }

    public static void main(String[] args) {
        try {
            Sequence sequence = parseGenbank(new File("/home/zenyk/1.gb"));

            // sequence.entry has to be assigned manually

            System.out.print(sequence.toString());
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }
}
