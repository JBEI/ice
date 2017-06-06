package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.*;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.EntryCreator;
import org.jbei.ice.lib.entry.EntryFactory;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.PlainParser;
import org.jbei.ice.lib.parsers.fasta.FastaParser;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;
import org.jbei.ice.lib.parsers.sbol.SBOLParser;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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
        if (!new PermissionsController().isPubliclyVisible(entry))
            entryAuthorization.expectRead(userId, entry);

        boolean canEdit = entryAuthorization.canWrite(userId, entry);
        return getFeaturedSequence(entry, canEdit);
    }

    /**
     * Parses a sequence in a file and associates it with the current entry
     *
     * @param inputStream input stream of bytes representing the file
     * @param fileName    name of file being parsed
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
            DNASequence sequence = parser.parse(sequenceString);
            return save(sequence, sequenceString, fileName);
        } catch (InvalidFormatParserException e) {
            Logger.error(e);
            throw new IOException(e);
        }
    }

    protected SequenceInfo save(DNASequence dnaSequence, String sequenceString, String fileName) {
        Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
        sequence.setSequenceUser(sequenceString);
        sequence.setEntry(entry);
        if (!StringUtils.isBlank(fileName))
            sequence.setFileName(fileName);

        Sequence result = sequenceDAO.saveSequence(sequence);
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
}
