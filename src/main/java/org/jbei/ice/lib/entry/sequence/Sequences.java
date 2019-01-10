package org.jbei.ice.lib.entry.sequence;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.TokenHash;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.SequenceInfo;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.parsers.GeneralParser;
import org.jbei.ice.lib.parsers.InvalidFormatParserException;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Represents Sequences in ICE
 *
 * @author Hector Plahar
 */
public class Sequences {

    private final SequenceDAO dao;
    private final EntryAuthorization authorization;
    private final HasEntry hasEntry;
    private final String userId;

    public Sequences(String userId) {
        dao = DAOFactory.getSequenceDAO();
        hasEntry = new HasEntry();
        authorization = new EntryAuthorization();
        this.userId = userId;
    }

    // responds to remote requested entry sequence
    public FeaturedDNASequence getRequestedSequence(RegistryPartner requestingPartner, String remoteUserId,
                                                    String token, String entryId, long folderId) {
        Entry entry = hasEntry.getEntry(entryId);
        if (entry == null)
            return null;

        // see folderContents.getRemotelySharedContents
        // todo : fold this into folder authorization and/or entry authorization
        Folder folder = DAOFactory.getFolderDAO().get(folderId);      // folder that the entry is contained in
        RemotePartner remotePartner = DAOFactory.getRemotePartnerDAO().getByUrl(requestingPartner.getUrl());

        // check that the remote user has the right token
        Permission shareModel = DAOFactory.getPermissionDAO().get(remoteUserId, remotePartner, folder);
        if (shareModel == null) {
            Logger.error("Could not retrieve share model");
            return null;
        }

        if (shareModel.getFolder().getId() != folderId) {
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
        return getFeaturedSequence(entry, shareModel.isCanWrite());
    }

    protected FeaturedDNASequence getFeaturedSequence(Entry entry, boolean canEdit) {
        Sequence sequence = dao.getByEntry(entry);
        if (sequence == null) {
            FeaturedDNASequence featuredDNASequence = new FeaturedDNASequence();
            featuredDNASequence.setName(entry.getName());
            return featuredDNASequence;
        }

        List<SequenceFeature> sequenceFeatures = DAOFactory.getSequenceFeatureDAO().getEntrySequenceFeatures(entry);
        FeaturedDNASequence featuredDNASequence = SequenceUtil.sequenceToDNASequence(sequence, sequenceFeatures);
        featuredDNASequence.setCanEdit(canEdit);
        featuredDNASequence.setIdentifier(entry.getPartNumber());


        String uriPrefix = DAOFactory.getConfigurationDAO().get(ConfigurationKey.URI_PREFIX).getValue();
        if (!StringUtils.isEmpty(uriPrefix)) {
            featuredDNASequence.setUri(uriPrefix + "/entry/" + entry.getId());
        }
        return featuredDNASequence;
    }

//    public FeaturedDNASequence retrievePartSequence(String userId, String recordId) {
//        Entry entry = hasEntry.getEntry(recordId);
//        if (entry == null)
//            throw new IllegalArgumentException("The part " + recordId + " could not be located");
//
//        if (entry.getVisibility() == Visibility.REMOTE.getValue()) {
//            WebEntries webEntries = new WebEntries();
//            return webEntries.getSequence(recordId);
//        }
//
//        if (!new PermissionsController().isPubliclyVisible(entry))
//            authorization.expectRead(userId, entry);
//
//        boolean canEdit = authorization.canWrite(userId, entry);
//        return getFeaturedSequence(entry, canEdit);
//    }

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
            FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
            if (dnaSequence == null)
                throw new InvalidFormatParserException("Could not parse sequence string");

            Sequence sequence = SequenceUtil.dnaSequenceToSequence(dnaSequence);
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
                byte[] data = new byte[1024];

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
                FeaturedDNASequence dnaSequence = GeneralParser.parse(sequenceString);
                if (dnaSequence == null) {
                    Logger.error("Could not parse sequence for " + name);
                    errors.add(name);
                    continue;
                }

                for (Entry entry : entries) {
                    Logger.info("Updating sequence for entry " + entry.getPartNumber());
                    PartSequence partSequence = new PartSequence(userId, entry.getPartNumber());
                    partSequence.update(dnaSequence);
                }
            }
        }

        return errors;
    }
}
