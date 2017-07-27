package org.jbei.ice.lib.entry.sample;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.StorageLocation;
import org.jbei.ice.lib.dto.sample.PartSample;
import org.jbei.ice.lib.dto.sample.SampleType;
import org.jbei.ice.lib.entry.HasEntry;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    protected PartSample createSample(String entryId, String barcode, String wellLocation, String plateName, String sample) {
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
