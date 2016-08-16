package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.dto.*;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.entry.sequence.composers.formatters.*;
import org.jbei.ice.lib.entry.sequence.composers.pigeon.PigeonSBOLv;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.search.blast.BlastPlus;
import org.jbei.ice.lib.utils.SequenceUtils;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

    public FeaturedDNASequence updateSequence(String userId, long entryId, FeaturedDNASequence featuredDNASequence,
                                              boolean addFeatures) {
        Entry entry = entryDAO.get(entryId);
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
            DNASequence dnaSequence = GeneralParser.getInstance().parse(featuredDNASequence.getSequence());
            sequence = dnaSequenceToSequence(dnaSequence);
        }
        sequence.setEntry(entry);
        if (!deleteSequence(userId, entryId))
            return null;

//        sequence = update(userId, sequence);
        sequence = save(userId, sequence);
        if (sequence != null)
            return sequenceToDNASequence(sequence);
        return null;
    }

    /**
     * Update the {@link Sequence} in the database, with the option to rebuild the search index.
     *
     * @param userId   unique identifier for user performing action
     * @param sequence sequence to be updated
     * @return Saved Sequence.
     */
    protected Sequence update(String userId, Sequence sequence) {
        authorization.expectWrite(userId, sequence.getEntry());
        Sequence result;

        Entry entry = sequence.getEntry();
        entry.setModificationTime(Calendar.getInstance().getTime());
        Sequence oldSequence = dao.getByEntry(entry);

        if (oldSequence == null) {
            result = dao.create(sequence);
        } else {
            String tmpDir = new ConfigurationController().getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
            if (!StringUtils.isEmpty(tmpDir)) {
                String hash = oldSequence.getFwdHash();
                try {
                    Files.deleteIfExists(Paths.get(tmpDir, hash + ".png"));
                } catch (IOException e) {
                    Logger.warn(e.getMessage());
                }
            }

            oldSequence.setSequenceUser(sequence.getSequenceUser());
            oldSequence.setSequence(sequence.getSequence());
            oldSequence.setFwdHash(sequence.getFwdHash());
            oldSequence.setRevHash(sequence.getRevHash());
            result = dao.updateSequence(oldSequence, sequence.getSequenceFeatures());
        }

        BlastPlus.scheduleBlastIndexRebuildTask(true);
        return result;
    }

    public boolean deleteSequence(String requester, long partId) {
        Entry entry = DAOFactory.getEntryDAO().get(partId);
        authorization.expectWrite(requester, entry);

        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return true;

        String tmpDir = new ConfigurationController().getPropertyValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        dao.deleteSequence(sequence, tmpDir);
//        BlastPlus.scheduleBlastIndexRebuildTask(true);  // todo : update is delete and save which is not right
        return true;
    }

    /**
     * Parse the given String into an {@link DNASequence} object.
     *
     * @param sequence
     * @return parsed DNASequence object.
     */
    public static DNASequence parse(String sequence) {
        return GeneralParser.getInstance().parse(sequence);
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

        if (!new PermissionsController().isPubliclyVisible(entry))
            authorization.expectRead(userId, entry);

        boolean canEdit = authorization.canWriteThoroughCheck(userId, entry);
        return getFeaturedSequence(entry, canEdit);
    }

    protected FeaturedDNASequence getFeaturedSequence(Entry entry, boolean canEdit) {
        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return null;

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
     * @param sequence
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
     * @param dnaSequence
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

    public ByteArrayWrapper getSequenceFile(String userId, long partId, String type) {
        Entry entry = entryDAO.get(partId);
        authorization.expectRead(userId, entry);

        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null)
            return new ByteArrayWrapper(new byte[]{'\0'}, "no_sequence");

        String name;
        String sequenceString;

        try {
            switch (type.toLowerCase()) {
                case "original":
                    sequenceString = sequence.getSequenceUser();
                    name = sequence.getFileName();
                    break;

                case "genbank":
                default:
                    GenbankFormatter genbankFormatter = new GenbankFormatter(entry.getName());
                    // TODO
                    genbankFormatter.setCircular((entry instanceof Plasmid) ? ((Plasmid) entry).getCircular() : false);
                    sequenceString = compose(sequence, genbankFormatter);
                    name = entry.getPartNumber() + ".gb";
                    break;

                case "fasta":
                    FastaFormatter formatter = new FastaFormatter(sequence.getEntry().getName());
                    sequenceString = compose(sequence, formatter);
                    name = entry.getPartNumber() + ".fasta";
                    break;

                case "sbol1":
                    sequenceString = compose(sequence, new SBOL1Formatter());
                    name = entry.getPartNumber() + ".xml";
                    break;

                case "sbol2":
                    sequenceString = compose(sequence, new SBOL2Formatter());
                    name = entry.getPartNumber() + ".xml";
                    break;

                case "pigeoni":
                    try {
                        URI uri = PigeonSBOLv.generatePigeonVisual(sequence);
                        byte[] bytes = IOUtils.toByteArray(uri.toURL().openStream());
                        return new ByteArrayWrapper(bytes, entry.getPartNumber() + ".png");
                    } catch (Exception e) {
                        Logger.error(e);
                        return new ByteArrayWrapper(new byte[]{'\0'}, "sequence_error");
                    }

                case "pigeons":
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
}
