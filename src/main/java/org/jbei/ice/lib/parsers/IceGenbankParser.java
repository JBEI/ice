package org.jbei.ice.lib.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.lib.vo.DNAFeature;
import org.jbei.ice.lib.vo.DNAFeatureLocation;
import org.jbei.ice.lib.vo.DNAFeatureNote;
import org.jbei.ice.lib.vo.FeaturedDNASequence;
import org.jbei.ice.lib.vo.IDNASequence;

/**
 * Genbank parser and generator.
 * The Genbank file format is defined in gbrel.txt located at
 * ftp://ftp.ncbi.nlm.nih.gov/genbank/gbrel.txt
 * 
 * This parser also handles some incorrectly formatted and obsolete genbank files.
 * 
 * @author Timothy Ham
 * 
 * */
public class IceGenbankParser extends AbstractParser {
    private static final String ICE_GENBANK_PARSER = "IceGenbank";

    // genbank tags
    public static final String LOCUS_TAG = "LOCUS";
    public static final String DEFINITION_TAG = "DEFINITION";
    public static final String ACCESSION_TAG = "ACCESSION";
    public static final String VERSION_TAG = "VERSION";
    public static final String NID_TAG = "NID";
    public static final String PROJECT_TAG = "PROJECT";
    public static final String DBLINK_TAG = "DBLINK";
    public static final String KEYWORDS_TAG = "KEYWORDS";
    public static final String SEGMENT_TAG = "SEGMENT";
    public static final String SOURCE_TAG = "SOURCE";
    public static final String ORGANISM_TAG = "ORGANISM";
    public static final String REFERENCE_TAG = "REFERENCE";
    public static final String COMMENT_TAG = "COMMENT";
    public static final String FEATURES_TAG = "FEATURES";
    public static final String BASE_COUNT_TAG = "BASE COUNT";
    public static final String CONTIG_TAG = "CONTIG";
    public static final String ORIGIN_TAG = "ORIGIN";
    public static final String END_TAG = "//";
    // tags only under reference tag
    public static final String AUTHORS_TAG = "AUTHORS";
    public static final String CONSRTM_TAG = "CONSRTM";
    public static final String TITLE_TAG = "TITLE";
    public static final String JOURNAL_TAG = "JOURNAL";
    public static final String MEDLINE_TAG = "MEDLINE";
    public static final String PUBMED_TAG = "PUBMED";
    public static final String REMARK_TAG = "REMARK";
    // obsolete tags
    public static final String BASE_TAG = "BASE";
    private static final String[] NORMAL_TAGS = { LOCUS_TAG, DEFINITION_TAG, ACCESSION_TAG,
            VERSION_TAG, NID_TAG, PROJECT_TAG, DBLINK_TAG, KEYWORDS_TAG, SEGMENT_TAG, SOURCE_TAG,
            ORGANISM_TAG, REFERENCE_TAG, COMMENT_TAG, FEATURES_TAG, BASE_COUNT_TAG, CONTIG_TAG,
            ORIGIN_TAG, END_TAG, BASE_TAG };
    private static final String[] REFERENCE_TAGS = { AUTHORS_TAG, CONSRTM_TAG, TITLE_TAG,
            JOURNAL_TAG, MEDLINE_TAG, PUBMED_TAG, REMARK_TAG };
    private static final String[] IGNORE_TAGS = { BASE_TAG, };
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    private static final Pattern startStopPattern = Pattern.compile("[<>]*(\\d+)\\.\\.[<>]*(\\d+)");
    private static final Pattern startOnlyPattern = Pattern.compile("\\d+");

    private Boolean hasErrors = false;
    private List<String> errors = new ArrayList<String>();

    @Override
    public String getName() {
        return ICE_GENBANK_PARSER;
    }

    @Override
    public Boolean hasErrors() {
        return hasErrors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public IDNASequence parse(byte[] bytes) throws InvalidFormatParserException {
        return parse(new String(bytes));
    }

    @Override
    public IDNASequence parse(File file) throws FileNotFoundException, IOException,
            InvalidFormatParserException {
        if (file.canRead()) {
            BufferedReader br = null;

            br = new BufferedReader(new FileReader(file));

            StringBuilder sb = new StringBuilder();
            while (br.ready()) {
                sb.append((char) br.read());
            }
            br.close();

            IceGenbankParser iceGenbankParser = new IceGenbankParser();

            return iceGenbankParser.parse(sb.toString());

        } else {
            return null;
        }
    }

    // TODO parse source feature tag with xdb_ref
    @Override
    public IDNASequence parse(String textSequence) throws InvalidFormatParserException {
        textSequence = cleanSequence(textSequence);

        FeaturedDNASequence sequence = null;
        ArrayList<Tag> tags = splitTags(textSequence, NORMAL_TAGS, IGNORE_TAGS);
        tags = parseTags(tags);

        sequence = new FeaturedDNASequence();
        for (Tag tag : tags) {
            if (tag instanceof LocusTag) {
                sequence.setName(((LocusTag) tag).getLocusName());
                sequence.setIsCircular(((LocusTag) tag).isCircular());
            } else if (tag instanceof OriginTag) {
                sequence.setSequence(((OriginTag) tag).getSequence());
            } else if (tag instanceof FeaturesTag) {
                sequence.setFeatures(((FeaturesTag) tag).getFeatures());
            }
        }
        return sequence;
    }

    private ArrayList<Tag> splitTags(String block, String[] acceptedTags, String[] ignoredTags)
            throws InvalidFormatParserException {
        ArrayList<Tag> result = new ArrayList<Tag>();

        StringBuilder rawBlock = new StringBuilder();
        String[] lines = block.split("\n");
        String[] lineChunks = null;
        Tag currentTag = null;

        // see if first two lines contain the "LOCUS" keyword. If not, don't even bother

        if (lines.length >= 1 && lines[0].indexOf("LOCUS") == -1) {
            if (lines.length == 1 || lines[1].indexOf("LOCUS") == -1) {
                throw new InvalidFormatParserException("Not a valid Genbank format: No Locus line.");
            }
        }

        for (String line : lines) {
            lineChunks = line.trim().split(" +");
            if (lineChunks.length == 0) {
                continue;
            } else {
                String putativeTag = lineChunks[0].trim();
                if (Arrays.asList(acceptedTags).contains(putativeTag)) {
                    if (currentTag != null) { // flush previous tag
                        currentTag.setRawBody(rawBlock.toString());
                        if (!Arrays.asList(ignoredTags).contains(currentTag.getKey())) {
                            result.add(currentTag);
                        }
                    }

                    rawBlock = new StringBuilder();
                    rawBlock.append(line);
                    rawBlock.append("\n");
                    currentTag = new Tag();
                    currentTag.setKey(putativeTag);

                } else {
                    rawBlock.append(line);
                    rawBlock.append("\n");
                }
            }
        }
        currentTag.setRawBody(rawBlock.toString());
        result.add(currentTag); // push the last one

        return result;
    }

    private ArrayList<Tag> parseTags(ArrayList<Tag> tags) throws InvalidFormatParserException {

        for (int i = 0; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (ORIGIN_TAG.equals(tag.getKey())) {
                tags.set(i, parseOriginTag(tag));
            } else if (FEATURES_TAG.equals(tag.getKey())) {
                tags.set(i, parseFeaturesTag(tag));
            } else if (REFERENCE_TAG.equals(tag.getKey())) {
                tags.set(i, parseReferenceTag(tag));
            } else if (LOCUS_TAG.equals(tag.getKey())) {
                tags.set(i, parseLocusTag(tag));
            } else if (SOURCE_TAG.equals(tag.getKey())) {

            } else {
                parseNormalTag(tag);
            }
        }
        return tags;
    }

    private Tag parseNormalTag(Tag tag) {
        String value = "";
        String[] lines = tag.getRawBody().split("\n");
        String[] firstLine = lines[0].split(" +");
        if (firstLine.length == 1) {
            // empty value
            tag.setValue("");
        } else {
            firstLine[0] = "";
            value = Utils.join(" ", Arrays.asList(firstLine));
            lines[0] = "";
            for (int i = 1; i < lines.length; i++) {
                lines[i] = lines[i].trim();
            }
            value = value + " " + Utils.join(" ", Arrays.asList(lines));
        }
        tag.setValue(value.trim());
        return tag;
    }

    private OriginTag parseOriginTag(Tag tag) {
        OriginTag result = new OriginTag();
        String value = "";
        StringBuilder sequence = new StringBuilder();

        String[] lines = tag.getRawBody().split("\n");
        String[] chunks;

        if (lines[0].startsWith(ORIGIN_TAG)) {
            if (lines[0].split(" +").length > 1) { // grab value of origin
                value = lines[0].split(" +")[1];
            }
        }
        for (int i = 1; i < lines.length; i++) {
            chunks = lines[i].trim().split(" +");
            if (chunks[0].matches("\\d*")) { //sometimes sequence block is un-numbered fasta
                chunks[0] = "";
            }
            sequence.append(Utils.join("", Arrays.asList(chunks)).toLowerCase());

        }

        result.setKey(tag.getKey());
        result.setValue(value);
        result.setSequence(sequence.toString());

        return result;
    }

    private FeaturesTag parseFeaturesTag(Tag tag) throws InvalidFormatParserException {
        FeaturesTag result = new FeaturesTag();
        result.setKey(tag.getKey());
        result.setRawBody(tag.getRawBody());

        int apparentFeatureKeyColumn = 0;

        String[] lines = tag.getRawBody().split("\n");
        String[] chunks;

        if (lines.length == 1) {
            // empty features tag
            result.setValue("");
            return result;
        } else {
            // first line should be first feature with location
            chunks = lines[1].trim().split(" +");
            if (chunks.length > 1) {
                apparentFeatureKeyColumn = lines[1].indexOf(chunks[0]);
            } else {
                return result; // could not determine key/value columns
            }
        }

        String line;
        String[] chunk;
        DNAFeature dnaFeature = null;
        StringBuilder qualifierBlock = new StringBuilder();
        String type = null;
        boolean complement = false;

        for (int i = 1; i < lines.length; i++) {
            line = lines[i];
            if (!(' ' == (line.charAt(apparentFeatureKeyColumn)))) {
                // start new key
                if (dnaFeature != null) {
                    dnaFeature = parseQualifiers(qualifierBlock.toString(), dnaFeature);
                    result.getFeatures().add(dnaFeature);
                }
                // start a new feature
                dnaFeature = new DNAFeature();
                qualifierBlock = new StringBuilder();

                /*
                 * Locations are generated differently by different implementations. Given
                 * the following two features:
                 * feature1: (1..3, 5..10) on the + strand |-|.|---->
                 * feature2: (1..3, 5..10) on the - strand <-|.|----|
                 * 
                 *  biojava follows the letter of the standard (gbrel.txt)
                 *  feature1: join(1..3,5..10)
                 *  feature2: complement(join(5..10,1..3))
                 *  
                 *  However, VectorNTI generates the following
                 *  feature1: join(1..3,5..10)
                 *  feature2: complement(1..3,5..10)
                 *  
                 *  This of course is incorrect, but we must parse them. 
                 * 
                 */

                // grab type, genbankStart, end, and strand
                List<GenbankLocation> genbankLocations = null;
                try {
                    chunk = line.trim().split(" +");
                    type = chunk[0].trim();

                    chunk[1] = chunk[1].trim();
                    boolean reversedLocations = false;
                    if (chunk[1].startsWith("complement(join")) {
                        reversedLocations = true; //standard compliant complement(join(location, location))
                    }
                    if (chunk[1].startsWith("complement")) {
                        complement = true;
                        chunk[1] = chunk[1].trim();
                        chunk[1] = chunk[1].substring(11, chunk[1].length() - 1).trim();
                    }

                    genbankLocations = parseGenbankLocation(chunk[1]);
                    if (reversedLocations) {
                        Collections.reverse(genbankLocations);
                    }
                } catch (NumberFormatException e) {
                    getErrors().add("Could not parse feature " + line);
                    System.out.println(line);
                    hasErrors = true;
                    continue;
                }

                LinkedList<DNAFeatureLocation> dnaFeatureLocations = new LinkedList<DNAFeatureLocation>();
                for (GenbankLocation genbankLocation : genbankLocations) {
                    DNAFeatureLocation dnaFeatureLocation = new DNAFeatureLocation(
                            genbankLocation.getGenbankStart(), genbankLocation.getEnd());
                    dnaFeatureLocation.setInBetween(genbankLocation.isInbetween());
                    dnaFeatureLocation.setSingleResidue(genbankLocation.isSingleResidue());
                    dnaFeatureLocations.add(dnaFeatureLocation);
                }

                dnaFeature.getLocations().addAll(dnaFeatureLocations);
                dnaFeature.setType(type);

                if (complement) {
                    dnaFeature.setStrand(-1);
                } else {
                    dnaFeature.setStrand(1);
                }
            } else {
                qualifierBlock.append(line);
                qualifierBlock.append("\n");
            }
        }
        // last qualifier
        dnaFeature = parseQualifiers(qualifierBlock.toString(), dnaFeature);
        result.getFeatures().add(dnaFeature);

        return result;
    }

    private List<GenbankLocation> parseGenbankLocation(String input)
            throws InvalidFormatParserException {
        LinkedList<GenbankLocation> result = new LinkedList<GenbankLocation>();

        int genbankStart = 1;
        int end = 1;

        if (input.startsWith("join")) {
            input = input.substring(5, input.length() - 1).trim();
        }

        String[] chunks = input.split(",");
        for (String chunk : chunks) {
            chunk = chunk.trim();
            Matcher startStopMatcher = startStopPattern.matcher(chunk);
            if (startStopMatcher.find()) {
                if (startStopMatcher.groupCount() == 2) {
                    genbankStart = Integer.parseInt(startStopMatcher.group(1));
                    end = Integer.parseInt(startStopMatcher.group(2));
                    result.add(new GenbankLocation(genbankStart, end));
                }
            } else {
                Matcher startOnlyMatcher = startOnlyPattern.matcher(chunk);
                if (startOnlyMatcher.find()) {
                    genbankStart = Integer.parseInt(startOnlyMatcher.group(0));
                    end = Integer.parseInt(startOnlyMatcher.group(0));
                    result.add(new GenbankLocation(genbankStart, end));
                }
            }
        }

        return result;
    }

    public class GenbankLocation {
        private int genbankStart = -1;
        private int end = -1;
        private boolean inbetween = false;
        private boolean singleResidue = false;

        public GenbankLocation(int genbankStart, int end) {
            super();
            setGenbankStart(genbankStart);
            setEnd(end);
        }

        public int getGenbankStart() {
            return genbankStart;
        }

        public void setGenbankStart(int genbankStart) {
            this.genbankStart = genbankStart;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public void setInbetween(boolean inbetween) {
            this.inbetween = inbetween;
        }

        public boolean isInbetween() {
            return inbetween;
        }

        public void setSingleResidue(boolean singleResidue) {
            this.singleResidue = singleResidue;
        }

        public boolean isSingleResidue() {
            return singleResidue;
        }

    }

    private DNAFeature parseQualifiers(String block, DNAFeature dnaFeature) {
        /* 
         * Qualifiers are interesting beasts. The values can be quoted 
         * or not quoted. They can span multiple lines. Older versions used
         * backslash to indicate space ("\\" -> " "). Oh, and it uses two quotes 
         * in a row to ("") to indicate a literal quote (e.g. "\""). And since each 
         * genbank feature does not have a specified "label" field, the 
         * label can be anything. Some software uses "label", another uses 
         * "notes", and some of the examples in gbrel.txt uses "gene".
         * But really, it could be anything. Qualifer "translation" must be handled
         * differently from other multi-line fields, as they are expected to be
         * concatenated without spaces.
         * 
         * This parser tries to normalize to "label", and preserve quotedness.
         *  
         */

        ArrayList<DNAFeatureNote> notes = new ArrayList<DNAFeatureNote>();
        if ("".equals(block)) {
            return dnaFeature;
        }

        DNAFeatureNote dnaFeatureNote = null;
        String[] lines = block.split("\n");
        String line = null;
        String[] chunk;
        StringBuilder qualifierItem = new StringBuilder();
        String qualifierValue = null;

        int apparentQualifierColumn = lines[0].indexOf("/");

        for (String line2 : lines) {
            line = line2;

            if ('/' == line.charAt(apparentQualifierColumn)) { // new tag starts
                if (dnaFeatureNote != null) { // flush previous note
                    qualifierValue = qualifierItem.toString();
                    if (qualifierValue.startsWith("\"") && qualifierValue.endsWith("\"")) {
                        dnaFeatureNote.setQuoted(true);
                        qualifierValue = qualifierValue.substring(1, qualifierValue.length() - 1);
                    } else {
                        dnaFeatureNote.setQuoted(false);
                    }
                    qualifierValue = qualifierValue.replaceAll("\\\\", " ");
                    qualifierValue = qualifierValue.replaceAll("\"\"", "\"");

                    if ("translation".equals(dnaFeatureNote.getName())) {
                        qualifierValue = Utils.join("", Arrays.asList(qualifierValue.split(" ")))
                                .trim();
                    }
                    dnaFeatureNote.setValue(qualifierValue);
                    notes.add(dnaFeatureNote);
                }

                // start a new note
                dnaFeatureNote = new DNAFeatureNote();
                qualifierItem = new StringBuilder();
                chunk = line.split("=");
                if (chunk.length < 2) {
                    getErrors().add("Skipping bad genbank qualifier " + line);
                    hasErrors = true;
                    dnaFeatureNote = null;
                    continue;
                } else {
                    String putativeName = chunk[0].trim().substring(1);
                    dnaFeatureNote.setName(putativeName);
                    chunk[0] = "";
                    qualifierItem.append(Utils.join(" ", Arrays.asList(chunk)).trim());
                }

            } else {
                qualifierItem.append(" ");
                qualifierItem.append(line.trim());
            }
        }

        // parse and add the last one
        qualifierValue = qualifierItem.toString();
        if (qualifierValue.startsWith("\"") && qualifierValue.endsWith("\"")) {
            dnaFeatureNote.setQuoted(true);
            qualifierValue = qualifierValue.substring(1, qualifierValue.length() - 1);
        } else {
            dnaFeatureNote.setQuoted(false);
        }
        qualifierValue = qualifierValue.replaceAll("\\\\", " ");
        qualifierValue = qualifierValue.replaceAll("\"\"", "\"");
        if ("translation".equals(dnaFeatureNote.getName())) {
            qualifierValue = Utils.join("", Arrays.asList(qualifierValue.split(" "))).trim();
        }
        dnaFeatureNote.setValue(qualifierValue);
        notes.add(dnaFeatureNote);

        dnaFeature.setNotes(notes);
        dnaFeature = populateName(dnaFeature);
        return dnaFeature;
    }

    /**
     * Tries to determine the feature name, from a list of possible qualifier keywords that might
     * contain it.
     * 
     * @param dnaFeature
     * @return
     */
    private DNAFeature populateName(DNAFeature dnaFeature) {
        String LABEL_QUALIFIER = "label";
        String APE_LABEL_QUALIFIER = "apeinfo_label";
        String NOTE_QUALIFIER = "note";
        String GENE_QUALIFIER = "gene";
        String ORGANISM_QUALIFIER = "organism";
        String NAME_QUALIFIER = "name";

        ArrayList<DNAFeatureNote> notes = (ArrayList<DNAFeatureNote>) dnaFeature.getNotes();
        String[] QUALIFIERS = { APE_LABEL_QUALIFIER, NOTE_QUALIFIER, GENE_QUALIFIER,
                ORGANISM_QUALIFIER, NAME_QUALIFIER };
        String newLabel = null;

        if (dnaFeatureContains(notes, LABEL_QUALIFIER) == -1) {
            for (String element : QUALIFIERS) {
                int foundId = dnaFeatureContains(notes, element);
                if (foundId != -1) {
                    newLabel = notes.get(foundId).getValue();
                }
            }
            if (newLabel == null) {
                newLabel = dnaFeature.getType();
            }
        } else {
            newLabel = notes.get(dnaFeatureContains(notes, LABEL_QUALIFIER)).getValue();
        }

        dnaFeature.setName(newLabel);
        return dnaFeature;
    }

    private int dnaFeatureContains(ArrayList<DNAFeatureNote> notes, String key) {
        int result = -1;
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getName().equals(key)) {
                result = i;
                return result;
            }
        }
        return result;
    }

    // TODO 
    private ReferenceTag parseReferenceTag(Tag tag) throws InvalidFormatParserException {

        String lines[] = tag.getRawBody().split("\n");
        String putativeValue = lines[0].split(" +")[1];
        tag.setValue(putativeValue);

        return null;
    }

    private LocusTag parseLocusTag(Tag tag) {
        LocusTag result = new LocusTag();
        result.setRawBody(tag.getRawBody());
        result.setKey(tag.getKey());
        String locusLine = tag.getRawBody();
        String[] locusChunks = locusLine.split(" +");

        if (Arrays.asList(locusChunks).contains("circular")
                || Arrays.asList(locusChunks).contains("CIRCULAR")) {
            result.setCircular(true);
        } else {
            result.setCircular(false);
        }

        String dateString = locusChunks[locusChunks.length - 1].trim();
        try {
            result.setDate(simpleDateFormat.parse(dateString));
        } catch (ParseException e1) {
            getErrors().add("Invalid date format: " + dateString + ". Setting today's date.");
            hasErrors = true;
            result.setDate(new Date());
        }

        if (Arrays.asList(locusChunks).indexOf("bp") == 3) {
            result.setLocusName(locusChunks[1]);
        } else {
            result.setLocusName("undefined");
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        /*
        File file = new File(
                "src/main/java/org/jbei/ice/lib/parsers/examples/AcrR_geneart_badlocus_badsequence.gb");
        //         "src/main/java/org/jbei/ice/lib/parsers/examples/pcI-LasI_ape_no_locusname.ape");
        //        "src/main/java/org/jbei/ice/lib/parsers/examples/pUC19.gb");

        if (file.canRead()) {
            BufferedReader br = null;
            try {

                br = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            StringBuilder sb = new StringBuilder();
            while (br.ready()) {
                sb.append((char) br.read());
            }

            IceGenbankParser igp = new IceGenbankParser();
            try {
                igp.parse(sb.toString());
            } catch (InvalidFormatParserException e) {
                e.printStackTrace();
            }
            
        }
         */
    }

    private class Tag {
        private String key;
        private String rawBody;
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getRawBody() {
            return rawBody;
        }

        public void setRawBody(String rawBody) {
            this.rawBody = rawBody;
        }

        @SuppressWarnings("unused")
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    private class OriginTag extends Tag {
        private String sequence;

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }
    }

    private class ReferenceTag extends Tag {
        private ArrayList<Tag> references = new ArrayList<Tag>();

        @SuppressWarnings("unused")
        public void setReferences(ArrayList<Tag> references) {
            this.references = references;
        }

        @SuppressWarnings("unused")
        public ArrayList<Tag> getReferences() {
            return references;
        }

    }

    private class FeaturesTag extends Tag {
        private List<DNAFeature> features = new ArrayList<DNAFeature>();

        @SuppressWarnings("unused")
        public void setFeatures(List<DNAFeature> features) {
            this.features = features;
        }

        public List<DNAFeature> getFeatures() {
            return features;
        }

    }

    private class LocusTag extends Tag {
        private String locusName = "";
        private boolean isCircular = true;
        private String naType = "DNA";
        private String strandType = "ds";
        private String divisionCode = "";
        private Date date;

        public String getLocusName() {
            return locusName;
        }

        public void setLocusName(String locusName) {
            this.locusName = locusName;
        }

        public boolean isCircular() {
            return isCircular;
        }

        public void setCircular(boolean isCircular) {
            this.isCircular = isCircular;
        }

        @SuppressWarnings("unused")
        public String getNaType() {
            return naType;
        }

        @SuppressWarnings("unused")
        public void setNaType(String naType) {
            this.naType = naType;
        }

        @SuppressWarnings("unused")
        public String getStrandType() {
            return strandType;
        }

        @SuppressWarnings("unused")
        public void setStrandType(String strandType) {
            this.strandType = strandType;
        }

        @SuppressWarnings("unused")
        public String getDivisionCode() {
            return divisionCode;
        }

        @SuppressWarnings("unused")
        public void setDivisionCode(String divisionCode) {
            this.divisionCode = divisionCode;
        }

        @SuppressWarnings("unused")
        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

    }

}
