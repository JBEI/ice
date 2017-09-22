package org.jbei.ice.lib.entry.sample;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.lib.entry.HasEntry;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Entry;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maintains information about sample information for a number of entries
 * in a CSV file
 *
 * @author Hector Plahar
 */
public class SampleCSV implements Closeable {

    private HasEntry hasEntry;
    private CSVReader reader;
    private String userId;
    private SampleService sampleService;

    public SampleCSV(String userId, InputStream inputStream) {
        this.hasEntry = new HasEntry();
        this.reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        this.userId = userId;
        this.sampleService = new SampleService();
    }

    public List<String> parse() throws IOException {
        List<String> errors = new ArrayList<>();
        String[] header = reader.readNext();

        for (String[] line : reader) {
            String entryId = line[0];
            if (StringUtils.isEmpty(entryId)) {
                continue;
            }

            if (hasEntry.getEntry(entryId) == null) {
                Logger.error("Could not retrieve entry by id " + entryId);
                errors.add(entryId);
                continue;
            }

            String barcode = line[2];
            String wellLocation = line[1];
            String plate = header[2];

            if (createSample(entryId, barcode, wellLocation, plate, entryId) == null)
                errors.add(entryId);

            if (header.length > 3) {
                for (int i = 3; i < header.length; i += 1) {
                    if (createSample(entryId, line[i], wellLocation, header[i], (entryId + " backup " + (i - 2))) == null) {
                        errors.add(entryId);
                    }
                }
            }
        }
        return errors;
    }

    public String generate() throws IOException {
        String[] header = reader.readNext();
        int headerLength = header.length;
        EntryDAO dao = DAOFactory.getEntryDAO();
        Path path = Paths.get(Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY), UUID.randomUUID().toString());
        FileWriter fileWriter = new FileWriter(path.toFile());
        BufferedWriter writer = new BufferedWriter(fileWriter);
        CSVWriter csvWriter = new CSVWriter(writer);

        csvWriter.writeNext(new String[]{"Well", "PartID (Strain)"});

        for (String[] line : reader) {
            if (line.length != headerLength) {
                Logger.info("Skipping line");
                continue;
            }

            String alias = line[0];
            String name = line[1];
            List<Entry> entries = dao.getMatching(name, alias, EntryType.PLASMID);
            if (entries.size() != 1) {
                Logger.error(entries.size() + " found for " + name + " " + alias);
                continue;
            }

            Entry plasmid = entries.get(0);
            List<Entry> parents = dao.getParents(plasmid.getId());
            if (parents.size() != 1) {
                Logger.error("Found " + parents.size() + " for " + name + " " + alias);
                continue;
            }

            Entry strain = parents.get(0);
            String locationLine = line[headerLength - 1];
            locationLine = locationLine.substring(locationLine.lastIndexOf('-') + 1);
            if (locationLine.length() == 2)
                locationLine = locationLine.charAt(0) + "0" + locationLine.charAt(1);
            csvWriter.writeNext(new String[]{locationLine, strain.getPartNumber()});
        }
        csvWriter.close();
        return path.getFileName().toString();
    }

    protected PartSample createSample(String entryId, String barcode, String wellLocation, String plateName,
                                      String sample) {
        PartSample partSample = new PartSample();

        StorageLocation tube = new StorageLocation();
        tube.setType(SampleType.TUBE);
        tube.setDisplay(barcode);

        StorageLocation well = new StorageLocation();
        well.setType(SampleType.WELL);
        well.setDisplay(wellLocation);
        well.setChild(tube);

        StorageLocation plate = new StorageLocation();
        plate.setType(SampleType.PLATE96);
        plate.setDisplay(plateName);
        plate.setChild(well);

        partSample.setLocation(plate);
        partSample.setLabel(sample);
        return sampleService.createSample(userId, entryId, partSample, null);
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }
}
