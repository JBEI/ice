package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.*;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.entry.sequence.composers.formatters.*;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.storage.DAOException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * ABI to manipulate {@link Sequence}s.
 *
 * @author Hector Plahar, Timothy Ham, Zinovii Dmytriv
 */
public class SequenceController extends HasEntry {

    private final SequenceDAO dao;
    private final EntryDAO entryDAO;
    private final EntryAuthorization authorization;

    public SequenceController() {
        dao = DAOFactory.getSequenceDAO();
        entryDAO = DAOFactory.getEntryDAO();
        authorization = new EntryAuthorization();
    }

    /**
     * Save the given {@link Sequence} into the database, with the option to rebuild the search
     * index.
     *
     * @param userId   unique identifier of user saving sequence
     * @param sequence sequence to save
     * @return Saved Sequence
     */
    public Sequence save(String userId, Sequence sequence) {
        authorization.expectWrite(userId, sequence.getEntry());
        Sequence result = dao.saveSequence(sequence);
        BlastPlus.scheduleBlastIndexRebuildTask(true);
        return result;
    }

    public FeaturedDNASequence updateSequence(String userId, String entryId, FeaturedDNASequence featuredDNASequence,
                                              boolean addFeatures) {
        Entry entry = super.getEntry(entryId);
        if (entry == null) {
            return null;
        }

        authorization.expectRead(userId, entry);

        if (addFeatures) {
            // expect existing sequence
            Sequence existingSequence = dao.getByEntry(entry);
            FeaturedDNASequence dnaSequence = sequenceToDNASequence(existingSequence);
            featuredDNASequence.getFeatures().addAll(dnaSequence.getFeatures());
            featuredDNASequence.setSequence(dnaSequence.getSequence());
        }

        Sequence sequence = dnaSequenceToSequence(featuredDNASequence);
        if (sequence.getSequenceFeatures() == null || sequence.getSequenceFeatures().isEmpty()) {
            DNASequence dnaSequence = GeneralParser.parse(featuredDNASequence.getSequence());
            sequence = dnaSequenceToSequence(dnaSequence);
        }
        sequence.setEntry(entry);
        if (!deleteSequence(userId, entryId))
            return null;

        sequence = save(userId, sequence);
        if (sequence == null)
            return null;

        BlastPlus.scheduleBlastIndexRebuildTask(true);
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();
        sequenceAnalysisController.rebuildAllAlignments(entry);
        return sequenceToDNASequence(sequence);
    }

    public boolean deleteSequence(String requester, String partId) {
        Entry entry = getEntry(partId);
        authorization.expectWrite(requester, entry);

        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return true;
        deleteSequence(sequence);
        return true;
    }

    protected void deleteSequence(Sequence sequence) {
        dao.deleteSequence(sequence);

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

    /**
     * Generate a formatted text of a given {@link IFormatter} from the given {@link Sequence}.
     *
     * @param sequence
     * @param formatter
     * @return Text of a formatted sequence.
     */
    protected String compose(Sequence sequence, IFormatter formatter) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            formatter.format(sequence, byteStream);
        } catch (FormatterException | IOException e) {
            Logger.error(e);
        }
        return byteStream.toString();
    }

    // responds to remote requested entry sequence
    public FeaturedDNASequence getRequestedSequence(RegistryPartner requestingPartner, String remoteUserId,
                                                    String token, String entryId, long folderId) {
        Entry entry = getEntry(entryId);
        if (entry == null)
            return null;

        // see folderContents.getRemoteSharedContents
        Folder folder = DAOFactory.getFolderDAO().get(folderId);      // folder that the entry is contained in
        RemotePartner remotePartner = DAOFactory.getRemotePartnerDAO().getByUrl(requestingPartner.getUrl());

        // check that the remote user has the right token
        RemoteShareModel shareModel = DAOFactory.getRemoteShareModelDAO().get(remoteUserId, remotePartner, folder);
        if (shareModel == null) {
            Logger.error("Could not retrieve share model");
            return null;
        }

        Permission permission = shareModel.getPermission(); // folder must match
        if (permission.getFolder().getId() != folderId) {
            String msg = "Shared folder does not match folder being requested";
            Logger.error(msg);
            throw new PermissionException(msg);
        }

        // validate access token
        TokenHash tokenHash = new TokenHash();
        String secret = tokenHash.encrypt(folderId + remotePartner.getUrl() + remoteUserId, token);
        if (!secret.equals(shareModel.getSecret())) {
            throw new PermissionException("Secret does not match");
        }

        // check that entry id is contained in folder
        return getFeaturedSequence(entry, permission.isCanWrite());
    }

    public FeaturedDNASequence retrievePartSequence(String userId, String recordId) {
        Entry entry = getEntry(recordId);
        if (entry == null)
            throw new IllegalArgumentException("The part " + recordId + " could not be located");

        if (entry.getVisibility() == Visibility.REMOTE.getValue()) {
            WebEntries webEntries = new WebEntries();
            return webEntries.getSequence(recordId);
        }

        if (!new PermissionsController().isPubliclyVisible(entry))
            authorization.expectRead(userId, entry);

        boolean canEdit = authorization.canWrite(userId, entry);
        return getFeaturedSequence(entry, canEdit);
    }

    protected FeaturedDNASequence getFeaturedSequence(Entry entry, boolean canEdit) {
        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null) {
            FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence();
            featuredDNASequence.setName(entry.getName());
            return featuredDNASequence;
        }

        FeaturedDNASequence featuredDNASequence = sequenceToDNASequence(sequence);
        featuredDNASequence.setCanEdit(canEdit);
        featuredDNASequence.setIdentifier(entry.getPartNumber());
        String uriPrefix = DAOFactory.getConfigurationDAO().get(ConfigurationKey.URI_PREFIX).getValue();
        if (!StringUtils.isEmpty(uriPrefix)) {
            featuredDNASequence.setUri(uriPrefix + "/entry/" + entry.getId());
        }
        return featuredDNASequence;
    }

    /**
     * Generate a {@link FeaturedDNASequence} from a given {@link Sequence} object.
     *
     * @param sequence sequence object to convert
     * @return FeaturedDNASequence
     */
    public FeaturedDNASequence sequenceToDNASequence(Sequence sequence) {
        if (sequence == null) {
            return null;
        }

        List<DNAFeature> features = new LinkedList<>();

        if (sequence.getSequenceFeatures() != null && sequence.getSequenceFeatures().size() > 0) {
            for (SequenceFeature sequenceFeature : sequence.getSequenceFeatures()) {
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
    public static Sequence dnaSequenceToSequence(DNASequence dnaSequence) {
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

    public ByteArrayWrapper getSequenceFile(String userId, long partId, SequenceFormat format) {
        Entry entry = entryDAO.get(partId);
        authorization.expectRead(userId, entry);

        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return new ByteArrayWrapper(new byte[]{'\0'}, "no_sequence");

        // if requested format is the same as the original format (if original exist) then get the original instead
        if (sequence.getFormat() == format && DAOFactory.getSequenceDAO().hasOriginalSequence(partId))
            format = SequenceFormat.ORIGINAL;

        String name;
        String sequenceString;

        try {
            switch (format) {
                case ORIGINAL:
                    sequenceString = sequence.getSequenceUser();
                    name = sequence.getFileName();
                    if (StringUtils.isEmpty(name))
                        name = entry.getPartNumber() + ".gb";
                    break;

                case GENBANK:
                default:
                    GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getName());
                    genbankFormatter.setCircular((entry instanceof Plasmid) ? ((Plasmid) entry).getCircular() : false);
                    sequenceString = compose(sequence, genbankFormatter);
                    name = entry.getPartNumber() + ".gb";
                    break;

                case FASTA:
                    FastaFormatter formatter = new FastaFormatter();
                    sequenceString = compose(sequence, formatter);
                    name = entry.getPartNumber() + ".fasta";
                    break;

                case SBOL1:
                    sequenceString = compose(sequence, new SBOLFormatter());
                    name = entry.getPartNumber() + ".xml";
                    break;

                case SBOL2:
                    sequenceString = compose(sequence, new SBOL2Formatter());
                    name = entry.getPartNumber() + ".xml";
                    break;

                case PIGEONI:
                    try {
                        URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
                        byte[] bytes = IOUtils.toByteArray(uri.toURL().openStream());
                        return new ByteArrayWrapper(bytes, entry.getPartNumber() + ".png");
                    } catch (Exception e) {
                        Logger.error(e);
                        return new ByteArrayWrapper(new byte[]{'\0'}, "sequence_error");
                    }

                case PIGEONS:
                    sequenceString = PigeonSBOLv.generatePigeonScript(sequence);
                    name = entry.getPartNumber() + ".txt";
                    break;
            }
        } catch (Exception e) {
            Logger.error("Failed to generate genbank file for download!", e);
            return new ByteArrayWrapper(new byte[]{'\0'}, "sequence_error");
        }

        return new ByteArrayWrapper(sequenceString.getBytes(), name);
    }

    public SequenceInfo parseSequence(InputStream inputStream, String fileName) throws InvalidFormatParserException {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            String ext = fileName.substring(dotIndex + 1);

            // unique case for sbol since it can result in multiple entries created
            if ("rdf".equalsIgnoreCase(ext) || "xml".equalsIgnoreCase(ext) || "sbol".equalsIgnoreCase(ext)) {
//                PartData partData = ModelToInfoFactory.getInfo(entry);
//                SBOLParser sbolParser = new SBOLParser(partData);
//                return sbolParser.parse(inputStream, fileName);
                // todo : cannot parse sbol yet
                return null;
            }
        }

        // parse actual sequence
        try {
            String sequenceString = IOUtils.toString(inputStream, Charset.defaultCharset());
            DNASequence dnaSequence = GeneralParser.parse(sequenceString);
            if (dnaSequence == null)
                throw new InvalidFormatParserException("Could not parse sequence string");

            Sequence sequence = SequenceController.dnaSequenceToSequence(dnaSequence);
            sequence.setSequenceUser(sequenceString);
            if (!StringUtils.isBlank(fileName))
                sequence.setFileName(fileName);

            SequenceInfo info = sequence.toDataTransferObject();
            info.setSequence(dnaSequence);
            return info;
        } catch (IOException e) {
            throw new InvalidFormatParserException(e);
        }
    }

    /**
     * Bulk update sequences based on uploaded zip file
     * containing sequences, where the sequence name is the (unique) part name
     *
     * @param userId          userId of user making request
     * @param fileInputStream input stream of zip file
     */
    public List<String> bulkUpdate(String userId, InputStream fileInputStream) throws IOException {
        if (!new AccountController().isAdministrator(userId))
            throw new PermissionException("Must have admin privileges to use this feature");

        List<String> errors = new ArrayList<>();
        SequenceAnalysisController sequenceAnalysisController = new SequenceAnalysisController();

        try (ZipInputStream stream = new ZipInputStream(fileInputStream)) {
            ZipEntry zipEntry;

            while ((zipEntry = stream.getNextEntry()) != null) {
                if (zipEntry.isDirectory())
                    continue;

                String name = zipEntry.getName();
                if (name.contains("/"))
                    name = name.substring(name.lastIndexOf("/") + 1);

                if (name.startsWith(".") || name.startsWith("_"))
                    continue;

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int len;
                byte data[] = new byte[1024];

                while ((len = stream.read(data)) > 0) {
                    out.write(data, 0, len);
                }
                stream.closeEntry();

                String entryName = name.substring(0, name.indexOf('.'));
                List<Entry> entries = DAOFactory.getEntryDAO().getByName(entryName);

                // todo : allowing multiple entries update for now
                if (entries == null || entries.isEmpty()) {
                    errors.add(name);
                    continue;
                }

                String sequenceString = new String(out.toByteArray());
                DNASequence dnaSequence = GeneralParser.parse(sequenceString);
                if (dnaSequence == null) {
                    Logger.error("Could not parse sequence for " + name);
                    errors.add(name);
                    continue;
                }

                for (Entry entry : entries) {
                    Logger.info("Updating sequence for entry " + entry.getPartNumber());
                    Sequence sequence = dao.getByEntry(entry);
                    if (sequence != null) {
                        dao.deleteSequence(sequence);
                    }

                    sequence = dnaSequenceToSequence(dnaSequence);
                    sequence.setEntry(entry);
                    sequence = dao.saveSequence(sequence);
                    if (sequence == null)
                        throw new DAOException("Could not save sequence");

                    sequenceAnalysisController.rebuildAllAlignments(entry);
//                    sequenceToDNASequence(sequence);
                }
            }

            BlastPlus.scheduleBlastIndexRebuildTask(true);
        }

        return errors;
    }
}
