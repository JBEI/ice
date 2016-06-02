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
import org.jbei.ice.lib.entry.PartDataUtil;
import org.jbei.ice.lib.entry.sequence.ByteArrayWrapper;
import org.jbei.ice.lib.entry.sequence.SequenceController;
import org.jbei.ice.lib.entry.sequence.composers.formatters.GenbankFormatter;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.RemotePartnerDAO;
import org.jbei.ice.storage.model.RemotePartner;
import org.jbei.ice.storage.model.Sequence;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

    public RemoteEntriesAsCSV() {
        dao = DAOFactory.getRemotePartnerDAO();
        remoteEntries = new RemoteEntries();
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

    private boolean writeList(List<RemotePartner> partners) throws IOException {
        Path tmpPath = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY));
        File tmpFile = File.createTempFile("remote-ice-", ".csv", tmpPath.toFile());
        csvPath = tmpFile.toPath();

        FileWriter fileWriter = new FileWriter(tmpFile);
        List<EntryField> fields = getEntryFields();

        // csv file headers
        String[] headers = new String[fields.size() + 3];
        headers[0] = "Registry";
        headers[1] = "Part ID";

        int i = 1;
        for (EntryField field : fields) {
            i += 1;
            headers[i] = field.getLabel();
        }
        headers[i + 1] = "Sequence File";

        File tmpZip = File.createTempFile("zip-", ".zip", tmpPath.toFile());
        FileOutputStream fos = new FileOutputStream(tmpZip);

        try (CSVWriter writer = new CSVWriter(fileWriter); ZipOutputStream zos = new ZipOutputStream(fos)) {
            writer.writeNext(headers);

            // go through partners
            for (RemotePartner partner : partners) {
                WebEntries webEntries = remoteEntries.getPublicEntries(partner.getId(), 0, Integer.MAX_VALUE, null, true);
                if (webEntries == null || webEntries.getEntries() == null) {
                    Logger.error("Could not retrieve entries for " + partner.getUrl());
                    continue;
                }

                // go through entries for each partner
                for (PartData partData : webEntries.getEntries()) {
                    String[] line = new String[fields.size() + 3];
                    line[0] = partner.getUrl();
                    line[1] = partData.getPartId();

                    i = 1;
                    for (EntryField field : fields) {
                        line[i + 1] = PartDataUtil.entryFieldToValue(partData, field);
                        i += 1;
                    }

                    // write sequence to zip file
                    if (partData.isHasSequence()) {
                        try {

//                        // get remote sequence
                            FeaturedDNASequence featuredDNASequence = remoteEntries.getPublicEntrySequence(partner.getId(),
                                    partData.getId());

                            if (featuredDNASequence != null) {
                                String name = featuredDNASequence.getName() + ".gb";

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

            // write the csv file to the zip
            writeZip(tmpZip, zos);
        }
        return true;
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
