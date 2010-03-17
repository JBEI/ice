package org.jbei.ice.controllers;

import java.util.ArrayList;

import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqException;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqParser;
import org.jbei.ice.lib.parsers.bl2seq.Bl2SeqResult;
import org.jbei.ice.lib.search.blast.Blast;
import org.jbei.ice.lib.search.blast.BlastException;
import org.jbei.ice.lib.search.blast.BlastResult;
import org.jbei.ice.lib.search.blast.ProgramTookTooLongException;

public class BlastController {
    public static void rebuildBlastDatabase() {
        try {
            Logger.info("Rebuilding Blast database ...");

            Blast blast = new Blast();
            blast.rebuildDatabase();

            Logger.info("Rebuild Blast database complete");
        } catch (Exception e) {
            Logger.error(BlastControllerException.FAILED_TO_REBUILD_BLAST_DATABASE, e);
        }
    }

    public static ArrayList<BlastResult> query(String query, String program)
            throws ProgramTookTooLongException {

        ArrayList<BlastResult> results = null;

        try {
            Logger.info(String.format("Blast '%s' searching for %s", program, query));

            Blast blast = new Blast();

            results = blast.query(query, program);

            Logger.info(String.format("Blast found %d results", (results == null) ? 0 : results
                    .size()));
        } catch (BlastException e) {
            Logger.error(BlastControllerException.BLAST_QUERY_FAILED, e);
        } catch (ProgramTookTooLongException e) {
            throw new ProgramTookTooLongException(e);
        } catch (Exception e) {
            Logger.error(BlastControllerException.BLAST_QUERY_FAILED, e);
        }

        return results;
    }

    public static String alignSequences(String query, String subject)
            throws ProgramTookTooLongException {
        String result = "";

        try {
            Blast blast = new Blast();
            result = blast.runBl2Seq(query, subject);
        } catch (BlastException e) {
            Logger.error(BlastControllerException.FAILED_TO_ALIGN_SEQUENCES, e);
        } catch (ProgramTookTooLongException e) {
            throw new ProgramTookTooLongException(e);
        } catch (Exception e) {
            Logger.error(BlastControllerException.FAILED_TO_ALIGN_SEQUENCES, e);
        }

        return result;
    }

    public static ArrayList<Bl2SeqResult> alignSequencesAndParse(String query, String subject)
            throws ProgramTookTooLongException {

        String bl2seqAlignment = alignSequences(query, subject);
        if (bl2seqAlignment == null || bl2seqAlignment.isEmpty()) {
            return null;
        }

        ArrayList<Bl2SeqResult> results = null;

        try {
            results = Bl2SeqParser.parse(bl2seqAlignment);
        } catch (Bl2SeqException e) {
            Logger.error(BlastControllerException.FAILED_TO_PARSE_SEQUENCE_ALIGNMENT, e);
        } catch (Exception e) {
            Logger.error(BlastControllerException.FAILED_TO_PARSE_SEQUENCE_ALIGNMENT, e);
        }

        return results;
    }

    public class BlastControllerException extends Exception {
        private static final long serialVersionUID = 1L;

        public static final String FAILED_TO_REBUILD_BLAST_DATABASE = "Failed to rebuild Blast database!";
        public static final String FAILED_TO_ALIGN_SEQUENCES = "Failed to align sequences!";
        public static final String FAILED_TO_PARSE_SEQUENCE_ALIGNMENT = "Failed to parse sequence alignment!";
        public static final String BLAST_QUERY_FAILED = "Blast query failed!";
    }
}
