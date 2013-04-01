package org.jbei.ice.lib.parsers.bl2seq;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse output of bl2seq sequence comparison program.
 *
 * @author Zinovii Dmytriv
 */
public class Bl2SeqParser {
    private static int MIN_SCORE = 10;
    private static Pattern SCORE_REGEXP_PATTERN = Pattern.compile("Score = .+\\((\\d+)\\)");
    private static Pattern SEQ_REGEXP_PATTERN = Pattern.compile("(\\d+)\\s+([a-zA-Z-]+)\\s+(\\d+)");
    private static Pattern LAMBDA_REGEXP_PATTERN = Pattern.compile("^Lambda.+");

    public static ArrayList<Bl2SeqResult> parse(String input) throws Bl2SeqException {
        if (input == null || input.isEmpty()) {
            throw new Bl2SeqException("bl2seq parser input is empty!");
        }

        StringTokenizer stringTokenizer = new StringTokenizer(input, "\n");
        if (stringTokenizer.countTokens() == 0) {
            throw new Bl2SeqException("bl2seq parser input is empty!");
        }

        ArrayList<Bl2SeqResult> results = new ArrayList<>();

        try {
            boolean isFirstRun = true;
            int score = 0;
            int orientation = 0;
            ArrayList<String> queryDataSequence = null;
            ArrayList<String> subjectDataSequence = null;

            while (stringTokenizer.hasMoreTokens()) {
                String line = stringTokenizer.nextToken();

                Matcher scoreMatcher = SCORE_REGEXP_PATTERN.matcher(line);
                if (scoreMatcher.find()) {
                    if (!isFirstRun) {
                        if (queryDataSequence != null && score >= MIN_SCORE) {
                            Bl2SeqResult bl2seqResult = new Bl2SeqResult(
                                    score,
                                    new Integer(queryDataSequence.get(0)),
                                    new Integer(queryDataSequence.get(1)),
                                    queryDataSequence.get(2),
                                    new Integer(subjectDataSequence.get(0)),
                                    new Integer(subjectDataSequence.get(1)),
                                    subjectDataSequence.get(2),
                                    orientation);

                            results.add(bl2seqResult);
                        }

                        score = 0;
                        orientation = 0;
                        queryDataSequence = null;
                        subjectDataSequence = null;
                    }

                    // get score
                    score = new Integer((scoreMatcher.group(1))).intValue();

                    line = stringTokenizer.nextToken();
                    line = stringTokenizer.nextToken();

                    // get orientation
                    String orientationLetter = line.substring(17, 18);
                    orientation = (orientationLetter.equals("P")) ? 0 : 1;
                    isFirstRun = false;
                    continue;
                }

                Matcher queryMatcher = SEQ_REGEXP_PATTERN.matcher(line);
                if (queryMatcher.find()) {
                    // get query sequence
                    if (queryDataSequence == null) {
                        queryDataSequence = parseSequenceLine(line);
                    } else {
                        ArrayList<String> queryDataSequencePartial = parseSequenceLine(line);
                        queryDataSequence.set(2, queryDataSequence.get(2).concat(queryDataSequencePartial.get(2)));
                        queryDataSequence.set(1, queryDataSequencePartial.get(1));
                    }

                    // get subject sequence
                    line = stringTokenizer.nextToken();
                    line = stringTokenizer.nextToken();
                    if (subjectDataSequence == null) {
                        subjectDataSequence = parseSequenceLine(line);
                    } else {
                        ArrayList<String> subjectDataSequencePartial = parseSequenceLine(line);

                        subjectDataSequence.set(2,
                                                subjectDataSequence.get(2).concat(subjectDataSequencePartial.get(2)));
                        subjectDataSequence.set(1, subjectDataSequencePartial.get(1));
                    }

                    continue;
                }

                Matcher lambdaMatcher = LAMBDA_REGEXP_PATTERN.matcher(line);
                if (lambdaMatcher.find()) {
                    if (queryDataSequence != null && score >= MIN_SCORE) {
                        Bl2SeqResult bl2seqResult = new Bl2SeqResult(score, new Integer(
                                queryDataSequence.get(0)), new Integer(queryDataSequence.get(1)),
                                                                     queryDataSequence.get(2), new Integer(
                                subjectDataSequence.get(0)), new Integer(subjectDataSequence.get(1)),
                                                                     subjectDataSequence.get(2), orientation);

                        results.add(bl2seqResult);
                    }

                    break;
                }
            }
        } catch (Exception e) {
            throw new Bl2SeqException(e);
        }

        return results;
    }

    private static ArrayList<String> parseSequenceLine(String sequenceLine) {
        //returns start, stop, sequence
        ArrayList<String> result = new ArrayList<>(3);

        Matcher matcher = SEQ_REGEXP_PATTERN.matcher(sequenceLine);
        if (matcher.find()) {
            result.add(matcher.group(1));
            result.add(matcher.group(3));
            result.add(matcher.group(2));
        }

        return result;
    }
}
