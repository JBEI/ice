package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.*;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryFactory;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.PlainParser;
import org.jbei.ice.lib.parsers.fasta.FastaParser;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;
import org.jbei.ice.lib.parsers.sbol.SBOLParser;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Biological part with associated sequence information
 *
 * @author Hector Plahar
 */
public class PartSequence extends HasEntry {

    private final Entry entry;
    private final String userId;
    private SequenceDAO sequenceDAO;
    private EntryAuthorization entryAuthorization;

    /**
     * Constructor for creating a new part to associate a sequence with
     *
     * @param userId unique identifier for user creating new part
     * @param type   type of part to create.
     */
    public PartSequence(String userId, EntryType type) {
        this.userId = userId;
        Entry newEntry = EntryFactory.buildEntry(type);

        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        String entryName = account.getFullName();
        String entryEmail = account.getEmail();
        newEntry.setOwner(entryName);
        newEntry.setOwnerEmail(entryEmail);
        newEntry.setCreator(entryName);
        newEntry.setCreatorEmail(entryEmail);
        newEntry.setVisibility(Visibility.DRAFT.getValue());
        EntryCreator creator = new EntryCreator();
        entry = creator.createEntry(account, newEntry, null);
        this.sequenceDAO = DAOFactory.getSequenceDAO();
    }

    public PartSequence(String userId, String entryId) {
        this.entry = super.getEntry(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve entry with identifier " + entryId);
        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.entryAuthorization = new EntryAuthorization();
        this.userId = userId;
    }

    public FeaturedDNASequence get() {
        entryAuthorization.expectRead(userId, entry);
        boolean canEdit = entryAuthorization.canWrite(userId, entry);
        return getFeaturedSequence(entry, canEdit);
    }

    /**
     * Parses a sequence in a file and associates it with the current entry
     *
     * @param inputStream      input stream of bytes representing the file
     * @param fileName         name of file being parsed
     * @param extractHierarchy for SBOL2 sequences only. If set to <code>true</code>, creates a hierarchy of ICE entries
     *                         as needed
     * @return wrapper around the internal model used to represent sequence information
     * @throws IOException on Exception parsing the contents of the file
     */
    public SequenceInfo parseSequenceFile(InputStream inputStream, String fileName, boolean extractHierarchy)
            throws IOException {
        try {
            AbstractParser parser;
            String sequenceString = Utils.getString(inputStream);

            switch (detectFormat(sequenceString)) {
                case GENBANK:
                    parser = new GenBankParser();
                    break;

                case SBOL2:
                    SBOLParser sbolParser = new SBOLParser(this.userId, Long.toString(this.entry.getId()), extractHierarchy);
                    return sbolParser.parseToEntry(sequenceString, fileName);

                case FASTA:
                    parser = new FastaParser();
                    break;

                default:
                case PLAIN:
                    parser = new PlainParser();
                    break;
            }

            // parse actual sequence
            String entryType = this.entry.getRecordType();
            DNASequence sequence = parser.parse(sequenceString, entryType);
            return save(sequence, sequenceString, fileName, entryType);
        } catch (InvalidFormatParserException e) {
            Logger.error(e);
            throw new IOException(e);
        }
    }

    /**
     * Updates the sequence information associated with this part with the referenced one.
     * <br>
     * Write privileges on the entry are required
     *
     * @param dnaSequence new sequence to associate with this part
     * @return updated sequence information
     */
    public FeaturedDNASequence update(FeaturedDNASequence dnaSequence) {
        entryAuthorization.expectWrite(userId, entry);

        // convert sequence wrapper to sequence storage model
        Sequence sequence = dnaSequenceToSequence(dnaSequence);

        // sometimes the whole sequence is sent in the string portion (when there are no features)
        if (sequence.getSequenceFeatures() == null || sequence.getSequenceFeatures().isEmpty()) {
            DNASequence parsedSequence = GeneralParser.parse(dnaSequence.getSequence());
            if (parsedSequence != null)
                sequence = dnaSequenceToSequence(parsedSequence);
        }

        if (sequence == null)
            return null;

        // delete existing sequence for entry
        Sequence existingSequence = sequenceDAO.getByEntry(this.entry);
        if (existingSequence != null)
            deleteSequence(existingSequence);

        // associated entry with new one
        sequence.setEntry(this.entry);
        sequence = sequenceDAO.saveSequence(sequence);
        if (sequence == null)
            return null;

        // rebuild blast
        BlastPlus.scheduleBlastIndexRebuildTask(true);

        // rebuild the trace sequence alignments
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();
        sequenceAnalysisController.rebuildAllAlignments(entry);
        return sequenceToDNASequence(sequence);
    }

    public boolean delete() {
        entryAuthorization.expectWrite(userId, entry);
        Sequence sequence = sequenceDAO.getByEntry(entry);
        if (sequence == null)
            return true;
        deleteSequence(sequence);
        return true;
    }

    protected void deleteSequence(Sequence sequence) {
        sequenceDAO.deleteSequence(sequence);

        String tmpDir = new ConfigurationController().getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        Path pigeonPath = Paths.get(tmpDir, sequence.getFwdHash() + ".png");

        try {
            Files.deleteIfExists(pigeonPath);
        } catch (IOException e) {
            // ok to ignore
            Logger.info("Error deleting pigeon folder " + pigeonPath.toString());
        }

        BlastPlus.scheduleBlastIndexRebuildTask(true);
    }

    protected SequenceInfo save(DNASequence dnaSequence, String sequenceString, String fileName, String entryType) {
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setSequenceUser(sequenceString);
        sequence.setEntry(entry);
        if (!StringUtils.isBlank(fileName))
            sequence.setFileName(fileName);
        Sequence result;
        if (EntryType.PROTEIN.getName().equalsIgnoreCase(entryType)) {
            result = sequenceDAO.saveProtein(sequence);
        } else {
            result = sequenceDAO.saveSequence(sequence);
        }

        BlastPlus.scheduleBlastIndexRebuildTask(true);
        SequenceInfo info = result.toDataTransferObject();
        info.setSequence(dnaSequence);
        return info;
    }

    /**
     * Attempts to detect the sequence format from the first line of a stream using the following heuristics
     * <ul>
     * <li>If line starts with <code>LOCUS</code> then assumed to be a genbank file</li>
     * <li>If line starts with <code>></code> then assumed to be a fasta file</li>
     * <li>If line starts with <code><</code> then assumed to be an sbol file</li>
     * <li>Anything else is assumed to be plain nucleotides</li>
     * </ul>
     *
     * @param sequenceString input sequence
     * @return detected format
     * @throws IOException
     */
    protected SequenceFormat detectFormat(String sequenceString) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(sequenceString));
        String line = reader.readLine();
        if (line == null)
            throw new IOException("Could not obtain line from document");

        if (line.startsWith("LOCUS"))
            return SequenceFormat.GENBANK;

        if (line.startsWith(">"))
            return SequenceFormat.FASTA;

        if (line.startsWith("<"))
            return SequenceFormat.SBOL2;

        return SequenceFormat.PLAIN;
    }

    protected FeaturedDNASequence getFeaturedSequence(Entry entry, boolean canEdit) {
        Sequence sequence = sequenceDAO.getByEntry(entry);
        if (sequence == null)
            return null;

        FeaturedDNASequence featuredDNASequence = sequenceToDNASequence(sequence);
        featuredDNASequence.setCanEdit(canEdit);
        featuredDNASequence.setIdentifier(entry.getPartNumber());
        Configuration configuration = DAOFactory.getConfigurationDAO().get(ConfigurationKey.URI_PREFIX);

        if (configuration != null) {
            String uriPrefix = configuration.getValue();
            featuredDNASequence.setUri(uriPrefix + "/entry/" + entry.getId());
        }
        return featuredDNASequence;
    }

    protected FeaturedDNASequence sequenceToDNASequence(Sequence sequence) {
        if (sequence == null) {
            return null;
        }

        List<DNAFeature> features = new LinkedList<>();
        List<SequenceFeature> sequenceFeatures = DAOFactory.getSequenceFeatureDAO().getEntrySequenceFeatures(this.entry);

        if (sequenceFeatures != null && sequenceFeatures.size() > 0) {
            for (SequenceFeature sequenceFeature : sequenceFeatures) {
                DNAFeature dnaFeature = new DNAFeature();
                dnaFeature.setUri(sequenceFeature.getUri());

                for (SequenceFeatureAttribute attribute : sequenceFeature.getSequenceFeatureAttributes()) {
                    String key = attribute.getKey();
                    String value = attribute.getValue();
                    DNAFeatureNote dnaFeatureNote = new DNAFeatureNote(key, value);
                    dnaFeatureNote.setQuoted(attribute.getQuoted());
                    dnaFeature.addNote(dnaFeatureNote);
                }

                Set<AnnotationLocation> locations = sequenceFeature.getAnnotationLocations();
                for (AnnotationLocation location : locations) {
                    dnaFeature.getLocations().add(
                            new DNAFeatureLocation(location.getGenbankStart(), location.getEnd()));
                }

                dnaFeature.setId(sequenceFeature.getId());
                dnaFeature.setType(sequenceFeature.getGenbankType());
                dnaFeature.setName(sequenceFeature.getName());
                dnaFeature.setStrand(sequenceFeature.getStrand());

                if (sequenceFeature.getAnnotationType() != null) {
                    dnaFeature.setAnnotationType(sequenceFeature.getAnnotationType().toString());
                }

                features.add(dnaFeature);
            }
        }

        boolean circular = false;
        Entry entry = sequence.getEntry();
        if (entry.getRecordType().equalsIgnoreCase(EntryType.PLASMID.name()))
            circular = ((Plasmid) sequence.getEntry()).getCircular();
        FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence(
                sequence.getSequence(), entry.getName(), circular, features, "");
        featuredDNASequence.setUri(sequence.getUri());

        return featuredDNASequence;
    }

    /**
     * Create a {@link Sequence} object from an {@link DNASequence} object.
     *
     * @param dnaSequence object to convert
     * @return Translated Sequence object.
     */
    protected Sequence dnaSequenceToSequence(DNASequence dnaSequence) {
        if (dnaSequence == null) {
            return null;
        }

        String fwdHash = "";
        String revHash = "";

        String sequenceString = dnaSequence.getSequence();
        if (!StringUtils.isEmpty(sequenceString)) {
            fwdHash = SequenceUtils.calculateSequenceHash(sequenceString);
            try {
                revHash = SequenceUtils.calculateSequenceHash(SequenceUtils.reverseComplement(sequenceString));
            } catch (UtilityException e) {
                revHash = "";
            }
        }

        Sequence sequence = new Sequence(sequenceString, "", fwdHash, revHash, null);
        Set<SequenceFeature> sequenceFeatures = sequence.getSequenceFeatures();

        if (dnaSequence instanceof FeaturedDNASequence) {
            FeaturedDNASequence featuredDNASequence = (FeaturedDNASequence) dnaSequence;
            sequence.setUri(featuredDNASequence.getUri());
            sequence.setComponentUri(featuredDNASequence.getDcUri());
            sequence.setIdentifier(featuredDNASequence.getIdentifier());

            if (featuredDNASequence.getFeatures() != null && !featuredDNASequence.getFeatures().isEmpty()) {
                for (DNAFeature dnaFeature : featuredDNASequence.getFeatures()) {
                    List<DNAFeatureLocation> locations = dnaFeature.getLocations();
                    String featureSequence = "";

                    for (DNAFeatureLocation location : locations) {
                        int genbankStart = location.getGenbankStart();
                        int end = location.getEnd();

                        if (genbankStart < 1) {
                            genbankStart = 1;
                        } else if (genbankStart > featuredDNASequence.getSequence().length()) {
                            genbankStart = featuredDNASequence.getSequence().length();
                        }

                        if (end < 1) {
                            end = 1;
                        } else if (end > featuredDNASequence.getSequence().length()) {
                            end = featuredDNASequence.getSequence().length();
                        }

                        if (genbankStart > end) { // over zero case
                            featureSequence = featuredDNASequence.getSequence().substring(
                                    genbankStart - 1, featuredDNASequence.getSequence().length());
                            featureSequence += featuredDNASequence.getSequence().substring(0, end);
                        } else { // normal
                            featureSequence = featuredDNASequence.getSequence().substring(genbankStart - 1, end);
                        }

                        if (dnaFeature.getStrand() == -1) {
                            try {
                                featureSequence = SequenceUtils.reverseComplement(featureSequence);
                            } catch (UtilityException e) {
                                featureSequence = "";
                            }
                        }
                    }

                    SequenceFeature.AnnotationType annotationType = null;
                    if (dnaFeature.getAnnotationType() != null && !dnaFeature.getAnnotationType().isEmpty()) {
                        annotationType = SequenceFeature.AnnotationType.valueOf(dnaFeature.getAnnotationType());
                    }

                    String name = dnaFeature.getName().length() < 127 ? dnaFeature.getName()
                            : dnaFeature.getName().substring(0, 123) + "...";
                    Feature feature = new Feature(name, dnaFeature.getIdentifier(), featureSequence,
                            dnaFeature.getType());
                    if (dnaFeature.getLocations() != null && !dnaFeature.getLocations().isEmpty())
                        feature.setUri(dnaFeature.getLocations().get(0).getUri());

                    SequenceFeature sequenceFeature = new SequenceFeature(sequence, feature,
                            dnaFeature.getStrand(), name,
                            dnaFeature.getType(), annotationType);
                    sequenceFeature.setUri(dnaFeature.getUri());

                    for (DNAFeatureLocation location : locations) {
                        int start = location.getGenbankStart();
                        int end = location.getEnd();
                        AnnotationLocation annotationLocation = new AnnotationLocation(start, end, sequenceFeature);
                        sequenceFeature.getAnnotationLocations().add(annotationLocation);
                    }

                    ArrayList<SequenceFeatureAttribute> sequenceFeatureAttributes = new ArrayList<>();
                    if (dnaFeature.getNotes() != null && dnaFeature.getNotes().size() > 0) {
                        for (DNAFeatureNote dnaFeatureNote : dnaFeature.getNotes()) {
                            SequenceFeatureAttribute sequenceFeatureAttribute = new SequenceFeatureAttribute();
                            sequenceFeatureAttribute.setSequenceFeature(sequenceFeature);
                            sequenceFeatureAttribute.setKey(dnaFeatureNote.getName());
                            sequenceFeatureAttribute.setValue(dnaFeatureNote.getValue());
                            sequenceFeatureAttribute.setQuoted(dnaFeatureNote.isQuoted());
                            sequenceFeatureAttributes.add(sequenceFeatureAttribute);
                        }
                    }

                    sequenceFeature.getSequenceFeatureAttributes().addAll(sequenceFeatureAttributes);
                    sequenceFeatures.add(sequenceFeature);
                }
            }
        }

        return sequence;
    }

}
