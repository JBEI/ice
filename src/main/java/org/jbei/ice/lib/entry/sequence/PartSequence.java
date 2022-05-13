package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationSettings;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.Entries;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.entry.sequence.analysis.TraceSequences;
import org.jbei.ice.lib.executor.IceExecutorService;
import org.jbei.ice.lib.parsers.AbstractParser;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.parsers.PlainParser;
import org.jbei.ice.lib.parsers.fasta.FastaParser;
import org.jbei.ice.lib.parsers.genbank.GenBankParser;
import org.jbei.ice.lib.parsers.sbol.SBOLParser;
import org.jbei.ice.lib.search.blast.Action;
import org.jbei.ice.lib.search.blast.RebuildBlastIndexTask;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.FeatureDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceFeatureAttributeDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceFeatureDAO;
import org.jbei.ice.storage.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static org.jbei.ice.lib.entry.sequence.SequenceFormat.SBOL2;

/**
 * Sequence information for a biological part in ICE
 *
 * @author Hector Plahar
 */
public class PartSequence {

    private final Entry entry;
    private final String userId;
    private final SequenceDAO sequenceDAO;
    private final SequenceFeatureDAO sequenceFeatureDAO;
    private final FeatureDAO featureDAO;
    private final EntryAuthorization entryAuthorization;
    public static final String SEQUENCE_FOLDER_NAME = "sequences";
    public static final float MB_FILE_SIZE = 1000000f;

    /**
     * Constructor for creating a new part to associate with a sequence
     *
     * @param userId unique identifier for user creating new part
     * @param type   type of part to create.
     */
    public PartSequence(String userId, EntryType type) {
        long partId = createNewPart(userId, type);
        this.entry = DAOFactory.getEntryDAO().get(partId);
        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.sequenceFeatureDAO = DAOFactory.getSequenceFeatureDAO();
        this.featureDAO = DAOFactory.getFeatureDAO();
        this.entryAuthorization = new EntryAuthorization();
        this.userId = userId;
    }

    /**
     * Constructor for associating sequence with existing entry
     *
     * @param userId  identifier for user
     * @param entryId identifier for entry
     */
    public PartSequence(String userId, String entryId) {
        this.entry = new HasEntry().getEntry(entryId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve entry with identifier " + entryId);

        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.sequenceFeatureDAO = DAOFactory.getSequenceFeatureDAO();
        this.featureDAO = DAOFactory.getFeatureDAO();
        this.entryAuthorization = new EntryAuthorization();
        this.userId = userId;
    }

    private long createNewPart(String userId, EntryType type) {
        Account account = DAOFactory.getAccountDAO().getByEmail(userId);
        String entryName = account.getFullName();
        String entryEmail = account.getEmail();

        PartData partData = new PartData(type);
        partData.setOwner(entryName);
        partData.setOwnerEmail(entryEmail);
        partData.setCreator(entryName);
        partData.setCreatorEmail(entryEmail);
        partData.setVisibility(Visibility.DRAFT);
        Entries entries = new Entries(userId);
        partData = entries.create(partData);
        return partData.getId();
    }

    /**
     * Retrieves the sequence information for current part
     *
     * @param includeAllAnnotations whether to include all annotations (true) or limit to the first 20 (false)
     * @return found sequence for current part
     */
    public FeaturedDNASequence get(boolean includeAllAnnotations) {
        entryAuthorization.expectRead(userId, entry);

//        if (entry.getVisibility() == Visibility.REMOTE.getValue()) {
//            WebEntries webEntries = new WebEntries();
//            return webEntries.getSequence(recordId);
//        }

        boolean canEdit = entryAuthorization.canWrite(userId, entry);
        return getFeaturedSequence(entry, canEdit, includeAllAnnotations);
    }

    /**
     * Parses a sequence in a file and associates it with the current entry
     *
     * @param inputStream      input stream of bytes representing the file
     * @param fileName         name of file being parsed as uploaded by the user
     * @param extractHierarchy for SBOL2 sequences only. If set to <code>true</code>, creates a hierarchy of ICE entries
     *                         as needed
     * @return wrapper around the internal model used to represent sequence information
     * @throws IOException on Exception parsing the contents of the file
     */
    public SequenceInfo parseSequenceFile(InputStream inputStream, String fileName, boolean extractHierarchy)
            throws IOException {
        AbstractParser parser;

        // write sequence file to disk (tmp)
        ConfigurationSettings configurationSettings = new ConfigurationSettings();
        String tmpDir = configurationSettings.getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        if (StringUtils.isEmpty(tmpDir))
            throw new IllegalArgumentException("Cannot parse sequence without valid tmp directory");

        Path tmpPath = Paths.get(tmpDir);
        if (!Files.isDirectory(tmpPath) || !Files.isWritable(tmpPath))
            throw new IllegalArgumentException("Cannot write to tmp directory: " + tmpPath);

        Path sequencePath = Paths.get(tmpPath.toString(), UUID.randomUUID() + "-" + fileName);
        Files.copy(inputStream, sequencePath, StandardCopyOption.REPLACE_EXISTING);

        // detect sequence
        SequenceFormat format;

        try (InputStream fileInputStream = Files.newInputStream(sequencePath);
             LineIterator iterator = IOUtils.lineIterator(fileInputStream, StandardCharsets.UTF_8)) {
            if (!iterator.hasNext())
                throw new IOException("Cannot read stream for " + fileName);

            String firstLine = iterator.next();
            format = SequenceUtil.detectFormat(firstLine);
        }

        // handle SBOL . TODO : use new python sbol handler
        try {
            if (format == SBOL2) {
                SBOLParser sbolParser = new SBOLParser(this.userId, Long.toString(this.entry.getId()), extractHierarchy);
                return sbolParser.parseToEntry(Files.newInputStream(sequencePath), fileName);
            }
        } catch (Exception e) {
            Logger.error(e);
            return null;
        }

        // store the file in the data directory and avoid parsing if file size is above 500kb
        if ((sequencePath.toFile().length() / MB_FILE_SIZE) > 0.5) {
            String dataDirectoryString = configurationSettings.getPropertyValue(ConfigurationKey.DATA_DIRECTORY);
            String sequenceFileName = UUID.randomUUID().toString();
            Path dataSequencePath = Paths.get(dataDirectoryString, SEQUENCE_FOLDER_NAME, sequenceFileName);
            Files.move(sequencePath, dataSequencePath, StandardCopyOption.REPLACE_EXISTING);

            // create sequence object
            Sequence sequence = new Sequence();
            sequence.setSequenceUser(sequenceFileName);
            sequence.setFileName(fileName);
            sequence.setFormat(format);
            sequence.setEntry(this.entry);
            sequence = sequenceDAO.create(sequence);
            return sequence.toDataTransferObject();
        }

        // special handling for sbol format
        try {
            switch (format) {
                case GENBANK:
                    parser = new GenBankParser();
                    break;

                case FASTA:
                    parser = new FastaParser();
                    break;

                default:
                case PLAIN:
                    parser = new PlainParser();
                    break;
            }

            LineIterator iterator = IOUtils.lineIterator(Files.newInputStream(sequencePath), StandardCharsets.UTF_8);
            SequenceFile sequenceFile = new SequenceFile();
            String entryType = this.entry.getRecordType();

            // special handling for SBOL (todo: clean this up in future release)
            FeaturedDNASequence dnaSequence = parser.parse(iterator, entryType);
            Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
            if (sequence == null)
                throw new IOException("Could not create sequence object");

            // copy original sequence file to file system
            try {
                Files.copy(sequencePath, sequenceFile.getFilePath(), StandardCopyOption.REPLACE_EXISTING);
                sequence.setSequenceUser(sequenceFile.getFileName());
            } catch (Exception e) {
                // ok to ignore. Can get back sequence as long as sequence object is saved. cannot download original
                Logger.warn("Exception writing sequence to file: " + e.getMessage());
            }

            sequence.setFileName(fileName);
            sequence.setFormat(format);
            sequence = saveSequenceObject(sequence);

            SequenceInfo info = sequence.toDataTransferObject();
            info.setSequence(dnaSequence);
            return info;
        } catch (InvalidFormatParserException ifpe) {
            Logger.error(ifpe);
            return null;
        } finally {
            Files.deleteIfExists(sequencePath);
        }
    }

    // creates a new sequence and associates it with entry
    public void save(FeaturedDNASequence dnaSequence) {
        entryAuthorization.expectWrite(userId, entry);

        // check if there is already an existing sequence
        if (sequenceDAO.getByEntry(this.entry) != null)
            throw new IllegalArgumentException("Entry already has a sequence associated with it. Please delete first");

        // update raw sequence if no sequence is passed
//        if ((dnaSequence.getFeatures() == null || dnaSequence.getFeatures().isEmpty())) {
//            // sometimes the whole sequence is sent in the string portion (when there are no features)
//            // no features to add
//            dnaSequence = GeneralParser.parse(dnaSequence.getSequence());
//        }

        // convert sequence wrapper to sequence storage model
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
        if (sequence == null)
            return;

        saveSequenceObject(sequence);
    }

    private Sequence saveSequenceObject(Sequence sequence) {
        sequence.setEntry(this.entry);
        Set<SequenceFeature> sequenceFeatureSet = null;
//        sequence = SequenceUtil.normalizeAnnotationLocations(sequence);
//        if (sequence == null)
//            throw new IllegalArgumentException("Could not normalize sequence");

        if (sequence.getSequenceFeatures() != null) {
            sequenceFeatureSet = new HashSet<>(sequence.getSequenceFeatures());
            sequence.setSequenceFeatures(null);
        }

        // create sequence
        sequence = sequenceDAO.create(sequence);

        // separate out sequence features and uniquely create features
        if (sequenceFeatureSet != null) {
            for (SequenceFeature sequenceFeature : sequenceFeatureSet) {
                createSequenceFeature(sequenceFeature, sequence);
            }
        }

        scheduleBlastIndexRebuildTask(Action.CREATE, sequence.getEntry().getPartNumber());
        return sequence;
    }

    private void scheduleBlastIndexRebuildTask(Action action, String partId) {
        RebuildBlastIndexTask task = new RebuildBlastIndexTask(action, partId);
        IceExecutorService.getInstance().runTask(task);
    }

    /**
     * Updates the sequence information associated with this part with the referenced one.
     * <br>
     * Write privileges on the entry are required
     *
     * @param updatedSequence new sequence to associate with this part
     */
    public void update(FeaturedDNASequence updatedSequence, boolean parseSequence) {
        entryAuthorization.expectWrite(userId, entry);
        Sequence existing = sequenceDAO.getByEntry(this.entry);

        // update with raw sequence if no sequence object is passed
        if (parseSequence) {
            // sometimes the whole sequence is sent in the string portion (when there are no features)
            // no features to add
            updatedSequence = GeneralParser.parse(updatedSequence.getSequence());
        } else if (updatedSequence.getSequence().isEmpty() && StringUtils.isNotBlank(existing.getSequence())) {
            updatedSequence.setSequence(existing.getSequence());
        }

        // convert sequence wrapper to sequence storage model
        Sequence sequence = SequenceUtil.dnaSequenceToSequence(updatedSequence);
        if (sequence == null)
            return;

        // todo : commenting out for now
        if (existing != null) {

            SequenceVersionHistory history = new SequenceVersionHistory(userId, existing.getId());

            // get features for existing entry
            existing.setSequenceFeatures(new HashSet<>(sequenceFeatureDAO.getEntrySequenceFeatures(this.entry)));

            // 1. check sequence string to see if it has changed
            checkSequenceString(history, existing, sequence);

            // 2. check features
            checkForNewFeatures(existing, sequence);

            // 3. check for removed features
            checkRemovedFeatures(existing, sequence);

            // 4. check if any existing features are updated
            checkForUpdatedFeatures(existing, sequence);

            // rebuild the trace sequence alignments // todo : this might not be needed for all updates
            new TraceSequences().rebuildAllAlignments(entry);

            // rebuild blast
            scheduleBlastIndexRebuildTask(Action.UPDATE, this.entry.getPartNumber());
        } else {
            save(updatedSequence);
        }
    }

    public void delete() {
        List<SequenceFeature> features = sequenceFeatureDAO.getEntrySequenceFeatures(entry);
        for (SequenceFeature feature : features)
            deleteSequenceFeature(feature);

        Sequence sequence = sequenceDAO.getByEntry(this.entry);
        sequence.setEntry(null);
        sequence.setSequenceFeatures(null);
        sequenceDAO.delete(sequence);
        scheduleBlastIndexRebuildTask(Action.DELETE, this.entry.getPartNumber());
    }

    // features in existing which are not part of new sequence passed and therefore need to be deleted
    private void checkRemovedFeatures(Sequence existing, Sequence sequence) {
        if (existing == null || existing.getSequenceFeatures() == null)
            return;

        // for each existing feature check that it is in new sequence
        List<SequenceFeature> toRemoveFeatures = new ArrayList<>();

        for (SequenceFeature sequenceFeature : existing.getSequenceFeatures()) {
            SequenceFeature foundFeature = checkFeature(sequence, sequenceFeature);
            if (foundFeature == null) {
                // remove feature
                Logger.info("Feature " + sequenceFeature.getName() + " was removed by user");
                deleteSequenceFeature(sequenceFeature);
                toRemoveFeatures.add(sequenceFeature);

                // todo : check if feature is not referenced by any sequence features then delete it
            }
        }

        toRemoveFeatures.forEach(existing.getSequenceFeatures()::remove);
    }

    private void checkForUpdatedFeatures(Sequence existing, Sequence updated) {
        if (existing == null || existing.getSequenceFeatures() == null || updated.getSequenceFeatures() == null)
            return;

        final SequenceFeatureAttributeDAO attributeDAO = DAOFactory.getSequenceFeatureAttributeDAO();

        // for each existing feature, check with updated for difference in name and type and notes
        for (SequenceFeature existingSequenceFeature : existing.getSequenceFeatures()) {

            SequenceFeature updatedSequenceFeature = getUpdatedEquivalent(existingSequenceFeature, updated);
            if (updatedSequenceFeature == null)
                continue;

            Feature updatedFeature = updatedSequenceFeature.getFeature();
            Feature existingFeature = existingSequenceFeature.getFeature();

            // check if existing feature name/type needs to be updated
            if (!updatedFeature.getName().equals(existingFeature.getName()) ||
                    !updatedFeature.getGenbankType().equals(existingFeature.getGenbankType())) {
                existingFeature.setName(updatedFeature.getName());
                existingFeature.setGenbankType(updatedFeature.getGenbankType());
                featureDAO.update(existingFeature);
            }

            // check notes for existing feature TODO : if key same but value is different, update instead
            // first build cache of updated
            Map<String, List<String>> updatedCache = new HashMap<>();
            for (SequenceFeatureAttribute updatedAttribute : updatedSequenceFeature.getSequenceFeatureAttributes()) {
                String key = updatedAttribute.getKey();
                String value = updatedAttribute.getValue();

                List<String> values = updatedCache.computeIfAbsent(key, k -> new ArrayList<>());
                values.add(value);
            }

            Iterator<SequenceFeatureAttribute> iterator = existingSequenceFeature.getSequenceFeatureAttributes().iterator();

            // remove notes that are not in updated list
            while (iterator.hasNext()) {
                SequenceFeatureAttribute attribute = iterator.next();
                List<String> values = updatedCache.get(attribute.getKey());
                if (values == null || !values.contains(attribute.getValue())) {
                    attributeDAO.delete(attribute);
                    iterator.remove();
                }
            }

            // add notes from updated list not present in existing
            for (SequenceFeatureAttribute updatedAttribute : updatedSequenceFeature.getSequenceFeatureAttributes()) {
                // check if attribute is available on existing
                boolean isAvailable = false;
                for (SequenceFeatureAttribute existingAttribute : existingSequenceFeature.getSequenceFeatureAttributes()) {
                    isAvailable = updatedAttribute.getKey().equals(existingAttribute.getKey()) &&
                            updatedAttribute.getValue().equals(existingAttribute.getValue());
                    if (isAvailable)
                        break;
                }

                // if not available add to list
                if (!isAvailable) {
                    updatedAttribute.setSequenceFeature(existingSequenceFeature);
                    updatedAttribute = attributeDAO.create(updatedAttribute);
                    existingSequenceFeature.getSequenceFeatureAttributes().add(updatedAttribute);
                }
            }
        }
    }

    // todo : can cache using hash as key (hash -> sequence) for performance improvements
    private SequenceFeature getUpdatedEquivalent(SequenceFeature existing, Sequence updatedSequence) {
        if (updatedSequence.getSequenceFeatures() == null)
            return null;

        for (SequenceFeature updated : updatedSequence.getSequenceFeatures()) {
            if (existing.getFeature().getHash().equals(updated.getFeature().getHash()))
                return updated;
        }
        // no match found
        return null;
    }

    private void deleteSequenceFeature(SequenceFeature sequenceFeature) {
        sequenceFeature.setSequence(null);
        sequenceFeature.setFeature(null);
        sequenceFeatureDAO.delete(sequenceFeature);
    }

    private void checkSequenceString(SequenceVersionHistory history, Sequence existing, Sequence sequence) {
        if (!existing.getFwdHash().equals(sequence.getFwdHash()) || !existing.getRevHash().equals(sequence.getRevHash())) {
            existing.setSequence(sequence.getSequence()); // hashes are updated in here (probably not a good method name)
            existing.setSequenceFeatures(null);
            sequenceDAO.update(existing);
            history.add(sequence.getSequence());
        }
    }

    // create new feature for the existing sequence
    private void createSequenceFeature(SequenceFeature sequenceFeature, Sequence existing) {
        Feature feature = sequenceFeature.getFeature();
        if (feature == null)
            return;

//        existing = SequenceUtil.normalizeAnnotationLocations(existing);
        if (existing == null)
            throw new IllegalArgumentException("cannot normalize sequence");

        Optional<Feature> optionalFeature = featureDAO.getByFeatureSequence(feature.getSequence());
        Feature existingFeature;
        if (optionalFeature.isPresent()) {
            existingFeature = optionalFeature.get();
            if (!sameFeatureUri(optionalFeature.get(), feature)) {
                existingFeature.setUri(feature.getUri());
            }
        } else {
            existingFeature = featureDAO.create(feature);
        }

        sequenceFeature.setFeature(existingFeature);
        sequenceFeature.setSequence(existing);
        sequenceFeature = sequenceFeatureDAO.create(sequenceFeature);
        if (existing.getSequenceFeatures() != null)
            existing.getSequenceFeatures().add(sequenceFeature);
    }

    private boolean sameFeatureUri(Feature f1, Feature f2) {
        if (f1.getUri() == null && f2.getUri() == null)
            return true;

        if (f1.getIdentification() != null && !f1.getIdentification().equalsIgnoreCase(f2.getIdentification()))
            return false;

        if (f1.getUri() != null && !f1.getUri().equalsIgnoreCase(f2.getUri()))
            return false;

        return f2.getUri().equalsIgnoreCase(f1.getUri());
    }

    private void checkForNewFeatures(Sequence existing, Sequence sequence) {
        // for each new feature, check if it is in existing sequence
        if (sequence.getSequenceFeatures() != null) {
            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
                SequenceFeature matchingFeature = checkFeature(existing, sequenceFeature);
                if (matchingFeature != null)
                    Logger.info("Feature " + sequenceFeature.getName() + " is an existing feature");
                else {
                    Logger.info("Feature " + sequenceFeature.getName() + " is not an existing feature");
                    // create new feature
                    createSequenceFeature(sequenceFeature, existing);
                }
            }
        }
    }

    // look for sequenceFeature in list of existing sequenceFeatures
    // feature and sequenceFeature are different objects in the database. sequenceFeature is created for each new feature
    // while feature can be reused
    // returns matching existing feature or null if no match
    private SequenceFeature checkFeature(Sequence existing, SequenceFeature sequenceFeature) {
        if (existing.getSequenceFeatures() == null)
            return null;

        // is sequence feature already available
        // important parts are strand and location
        for (SequenceFeature existingSequenceFeature : existing.getSequenceFeatures()) {

            // compare
            if (existingSequenceFeature.getStrand() != sequenceFeature.getStrand())
                continue;

            // check annotation locations (doesnt support modification of annotation locations so exact matches are required)
            if (existingSequenceFeature.getAnnotationLocations().size() != sequenceFeature.getAnnotationLocations().size())
                continue;

            AnnotationLocation location = sequenceFeature.getAnnotationLocations().iterator().next();
            AnnotationLocation location2 = existingSequenceFeature.getAnnotationLocations().iterator().next();

            if (location.getGenbankStart() != location2.getGenbankStart() && location.getEnd() != location2.getEnd())
                continue;

            // check feature
            if (checkSameFeature(existingSequenceFeature, sequenceFeature))
                return existingSequenceFeature;    // sequence found in list of existing

            if (!existingSequenceFeature.getName().equals(sequenceFeature.getName()))
                continue;
//
//            if (isNotEqual(existingSequenceFeature.getGenbankType(), sequenceFeature.getGenbankType()))
//                continue;
//
//            if (isNotEqual(existingSequenceFeature.getUri(), sequenceFeature.getUri()))
//                continue;


            // check sequence feature attributes
            // todo : this is tied to sequenceFeature and so can be modified without creating a new feature
            // sequenceFeature.getSequenceFeatureAttributes()
        }

        return null;
    }

    // check that the feature object both point to are the same
    // tolerates differences in name etc by only checking the sequence
    private boolean checkSameFeature(SequenceFeature existingSequenceFeature, SequenceFeature newSequenceFeature) {
        Feature existingFeature = existingSequenceFeature.getFeature();
        Feature newFeature = newSequenceFeature.getFeature();
        return existingFeature.getHash().equals(newFeature.getHash()) &&
                existingFeature.getSequence().equals(newFeature.getSequence());
    }

    /**
     * Convert sequence to a byte array of the specified format with the intention of being written to a file
     *
     * @param format      specified format for sequence conversion
     * @param useFileName whether to use the original filename of the sequence if the original uploaded sequence is
     *                    available
     * @return wrapper around the outputstream for the converted format and name
     */
    public InputStreamWrapper toFile(SequenceFormat format, boolean useFileName) {
        entryAuthorization.expectRead(userId, entry);
        Sequence sequence = sequenceDAO.getByEntry(entry);
        if (sequence == null)
            return null;

        // default format of genbank
        if (format == null)
            format = SequenceFormat.GENBANK;

        return new SequenceAsString(format, entry.getId(), useFileName).get();
    }

    private FeaturedDNASequence getFeaturedSequence(Entry entry, boolean canEdit, boolean includeAllFeatures) {
        Sequence sequence = sequenceDAO.getByEntry(entry);
        if (sequence == null) {
            return null;
        }

        String potentialFileId = sequence.getSequenceUser();
        if (!StringUtils.isBlank(potentialFileId)) {
            ConfigurationSettings configurationSettings = new ConfigurationSettings();
            String dataDirectoryString = configurationSettings.getPropertyValue(ConfigurationKey.DATA_DIRECTORY);
            Path dataSequencePath = Paths.get(dataDirectoryString, SEQUENCE_FOLDER_NAME, potentialFileId);

            // get file properties
            if (Files.exists(dataSequencePath)) {
                if ((dataSequencePath.toFile().length() / MB_FILE_SIZE) > 0.5) {
                    return new FeaturedDNASequence();
                }
            } else {
                return null;
            }
        }

        List<SequenceFeature> sequenceFeatures;
        if (includeAllFeatures) {
            sequenceFeatures = sequenceFeatureDAO.getEntrySequenceFeatures(entry);
        } else {
            sequenceFeatures = sequenceFeatureDAO.pageSequenceFeatures(entry, 0, 20);
        }

        FeaturedDNASequence featuredDNASequence = SequenceUtil.sequenceToDNASequence(sequence, sequenceFeatures);
        featuredDNASequence.setCanEdit(canEdit);
        featuredDNASequence.setIdentifier(entry.getPartNumber());
        ConfigurationModel configuration = DAOFactory.getConfigurationDAO().get(ConfigurationKey.URI_PREFIX);

        if (configuration != null) {
            String uriPrefix = configuration.getValue();
            featuredDNASequence.setUri(uriPrefix + "/entry/" + entry.getId());
        }
        return featuredDNASequence;
    }
}
