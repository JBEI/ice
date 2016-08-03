package org.jbei.ice.lib.net;

import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.EntryField;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.entry.EntryFields;
import org.jbei.ice.lib.entry.EntryUtil;
import org.jbei.ice.lib.entry.PartDataUtil;
import org.jbei.ice.lib.entry.sequence.ByteArrayWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Represents remote entries from web partners in csv form
 *
 * @author Hector Plahar
 */
public class RemoteEntriesAsCSV {

    private final RemotePartnerDAO dao;
    private final RemoteEntries remoteEntries;
    private Path csvPath;
    private boolean includeLocal;

    public RemoteEntriesAsCSV(boolean includeLocal) {
        dao = DAOFactory.getRemotePartnerDAO();
        remoteEntries = new RemoteEntries();
        this.includeLocal = includeLocal;
    }

    public boolean getEntries(int offset, int limit) {
        List<RemotePartner> partners = dao.getRegistryPartners();
        if (partners.isEmpty())
            return false;

        try {
            return writeList(partners);
        } catch (IOException ioe) {
            Logger.error(ioe);
            return false;
        }
    }

    protected String[] getCSVHeaders(List<EntryField> fields) {
        String[] headers = new String[fields.size() + 4];
        headers[0] = "Registry";
        headers[1] = "Created";
        headers[2] = "Part ID";

        int i = 2;
        for (EntryField field : fields) {
            i += 1;
            headers[i] = field.getLabel();
        }
        headers[i + 1] = "Sequence File";
        return headers;
    }

    private boolean writeList(List<RemotePartner> partners) throws IOException {
        Path tmpPath = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        File tmpFile = File.createTempFile("remote-ice-", ".csv", tmpPath.toFile());
        csvPath = tmpFile.toPath();

        FileWriter fileWriter = new FileWriter(tmpFile);
        List<EntryField> fields = getEntryFields();
        String[] headers = getCSVHeaders(fields);

        // csv file headers
        File tmpZip = File.createTempFile("zip-", ".zip", tmpPath.toFile());
        FileOutputStream fos = new FileOutputStream(tmpZip);

        try (CSVWriter writer = new CSVWriter(fileWriter); ZipOutputStream zos = new ZipOutputStream(fos)) {
            writer.writeNext(headers);

            // go through partners
            for (RemotePartner partner : partners) {
                try {
                    Logger.info("Retrieving from " + partner.getUrl());
                    WebEntries webEntries = remoteEntries.getPublicEntries(partner.getId(), 0, Integer.MAX_VALUE, null, true);
                    if (webEntries == null || webEntries.getEntries() == null) {
                        Logger.error("Could not retrieve entries for " + partner.getUrl());
                        continue;
                    }
                    Logger.info("Obtained " + webEntries.getCount() + " from " + partner.getUrl());

                    // go through entries for each partner and write to the zip file
                    writeDataEntries(partner, webEntries.getEntries(), fields, writer, zos);

                } catch (Exception e) {
                    Logger.warn("Exception retrieving entries " + e.getMessage());
                }
            }

            // write local entries
            if (this.includeLocal) {
                Logger.info("Retrieving local public entries");
                Group publicGroup = new GroupController().createOrRetrievePublicGroup();
                Set<Group> groups = new HashSet<>();
                groups.add(publicGroup);

                EntryDAO entryDAO = DAOFactory.getEntryDAO();
                Set<Entry> results = entryDAO.retrieveVisibleEntries(null, groups, ColumnField.CREATED, true, 0, Integer.MAX_VALUE, null);
                writeLocalEntries(results, fields, writer, zos);
            }

            // write the csv file to the zip
            writeZip(tmpZip, zos);
        }
        return true;
    }

    protected void writeDataEntries(RemotePartner partner, List<PartData> entries, List<EntryField> fields,
                                    CSVWriter writer, ZipOutputStream zos) {
        if (entries == null)
            return;

        for (PartData partData : entries) {
            String[] line = new String[fields.size() + 4];
            line[0] = partner.getUrl();
            line[1] = new Date(partData.getCreationTime()).toString();
            line[2] = partData.getPartId();

            int i = 2;
            for (EntryField field : fields) {
                line[i + 1] = PartDataUtil.entryFieldToValue(partData, field);
                i += 1;
            }

            // write sequence to zip file
            if (partData.isHasSequence()) {
                try {

                    // get remote sequence
                    FeaturedDNASequence featuredDNASequence = remoteEntries.getPublicEntrySequence(partner.getId(),
                            partData.getId());

                    if (featuredDNASequence != null) {
                        String name = partData.getPartId() + ".gb";

                        // write sequence to zip
                        line[i + 1] = name;
                        Sequence sequence = SequenceController.dnaSequenceToSequence(featuredDNASequence);
                        GenbankFormatter genbankFormatter = new GenbankFormatter(name);
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        genbankFormatter.format(sequence, byteStream);
                        ByteArrayWrapper wrapper = new ByteArrayWrapper(byteStream.toByteArray(), name);
                        putZipEntry(wrapper, zos);
                    } else {
                        line[i + 1] = "";
                    }
                } catch (Exception e) {
                    line[i + 1] = "";
                }
            } else {
                line[i + 1] = "";
            }

            writer.writeNext(line);
        }
    }

    protected void writeLocalEntries(Set<Entry> entries, List<EntryField> fields,
                                     CSVWriter writer, ZipOutputStream zos) {
        if (entries == null)
            return;

        SequenceDAO sequenceDAO = DAOFactory.getSequenceDAO();
        Configuration configuration = DAOFactory.getConfigurationDAO().get(ConfigurationKey.URI_PREFIX);
        String thisUrl = configuration == null ? "" : configuration.getValue();

        for (Entry entry : entries) {
            String[] line = new String[fields.size() + 4];
            line[0] = thisUrl;
            line[1] = entry.getCreationTime().toString();
            line[2] = entry.getPartNumber();

            int i = 2;
            for (EntryField field : fields) {
                line[i + 1] = EntryUtil.entryFieldToValue(entry, field);
                i += 1;
            }

            // write sequence to zip file
            long entryId = entry.getId();
            if (sequenceDAO.hasSequence(entryId)) {
                String name = entry.getPartNumber() + ".gb";

                try {
                    Sequence sequence = sequenceDAO.getByEntry(entry);
                    line[i + 1] = name;

                    GenbankFormatter genbankFormatter = new GenbankFormatter(name);
                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    genbankFormatter.format(sequence, byteStream);
                    ByteArrayWrapper wrapper = new ByteArrayWrapper(byteStream.toByteArray(), name);
                    putZipEntry(wrapper, zos);
                } catch (Exception e) {
                    line[i + 1] = "";
                }
            } else {
                line[i + 1] = "";
            }

            writer.writeNext(line);
        }
    }

    protected List<EntryField> getEntryFields() {
        List<EntryField> fields = EntryFields.getCommonFields();

        EntryFields.addArabidopsisSeedHeaders(fields);
        EntryFields.addStrainHeaders(fields);
        EntryFields.addPlasmidHeaders(fields);

        return fields;
    }

    private boolean writeZip(File tmpZip, ZipOutputStream zos) {
        try {
            // write the csv file
            FileInputStream fis = new FileInputStream(csvPath.toFile());
            ByteArrayWrapper wrapper = new ByteArrayWrapper(IOUtils.toByteArray(fis), "entries.csv");
            putZipEntry(wrapper, zos);
            zos.close();
            csvPath = tmpZip.toPath();
            return true;
        } catch (Exception e) {
            Logger.error(e);
            return false;
        }
    }

    protected void putZipEntry(ByteArrayWrapper wrapper, ZipOutputStream zos) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(wrapper.getBytes())) {
            byte[] buffer = new byte[1024];

            zos.putNextEntry(new ZipEntry(wrapper.getName()));

            int length;
            while ((length = bis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public Path getFilePath() {
        return csvPath;
    }
}
