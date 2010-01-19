package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;

import org.jbei.ice.lib.models.Sequence;

public class Parser {
    public static Sequence parseGenbankFile(String file) throws ParserException {
        Sequence sequence = null;

        try {
            sequence = GenbankParser.parseGenbankDNAFile(new BufferedReader(new FileReader(file)));
        } catch (FileNotFoundException e) {
            throw new ParserException("File not found: " + file, e);
        } catch (Exception e) {
            throw new ParserException(e);
        }

        return sequence;
    }

    public static Sequence parseGenbank(String genbankSequence) throws ParserException {
        Sequence sequence = null;

        try {
            sequence = GenbankParser.parseGenbankDNAFile(new BufferedReader(new StringReader(genbankSequence)));
        } catch (Exception e) {
            throw new ParserException(e);
        }

        return sequence;
    }

    public static void main(String[] args) {
        try {
            Sequence sequence = parseGenbankFile("/home/zenyk/1.gb");

            // Some stuff has to be assigned manually
            // sequence.fwdHash;
            // sequence.revHash;
            // sequence.sequenceUser;
            // sequence.entry;

            System.out.print(sequence.toString());
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }
}
