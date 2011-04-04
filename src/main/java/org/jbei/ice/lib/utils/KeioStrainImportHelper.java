package org.jbei.ice.lib.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ConfigurationManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Name;
import org.jbei.ice.lib.models.Plasmid;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.lib.models.SelectionMarker;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.models.Strain;
import org.jbei.ice.lib.permissions.PermissionException;

import au.com.bytecode.opencsv.CSVReader;

public class KeioStrainImportHelper {

    private static enum Header {
        CREATOR, CREATOR_EMAIL, PRINCIPAL_INVESTIGATOR, FUNDING_SOURCE, INTELLECTUAL_PROPERTY_INFO, BIO_SAFETY_LEVEL, STRAIN_NAME, STRAIN_ALIAS, STRAIN_URL, STRAIN_STATUS, STRAIN_SELECTION_MARKERS, STRAIN_PARENT, STRAIN_GEN_PHEN, STRAIN_PLASMIDS, STRAIN_KEYWORDS, STRAIN_SUMMARY, STRAIN_NOTES, STRAIN_REFERENCES, STRAIN_SEQUENCE_FILENAME, STRAIN_ATTACHMENT_FILENAME, RACK, BARCODE, RACK_BACKUP1, RACK_BACKUP2
    }

    static class StrainRow {

        private final HashMap<Header, String> row;

        public StrainRow(String[] values) {
            // expect 24
            if (values.length != 24)
                throw new IllegalArgumentException("Expecting 24 values. Got " + values.length);

            row = new HashMap<Header, String>();

            row.put(Header.CREATOR, values[0]);
            row.put(Header.CREATOR_EMAIL, values[1]);
            row.put(Header.PRINCIPAL_INVESTIGATOR, values[2]);
            row.put(Header.FUNDING_SOURCE, values[3]);
            row.put(Header.INTELLECTUAL_PROPERTY_INFO, values[4]);
            row.put(Header.BIO_SAFETY_LEVEL, values[5]);
            row.put(Header.STRAIN_NAME, values[6]);
            row.put(Header.STRAIN_ALIAS, values[7]);
            row.put(Header.STRAIN_URL, values[8]);
            row.put(Header.STRAIN_STATUS, values[9]);
            row.put(Header.STRAIN_SELECTION_MARKERS, values[10]);
            row.put(Header.STRAIN_PARENT, values[11]);
            row.put(Header.STRAIN_GEN_PHEN, values[12]);
            row.put(Header.STRAIN_PLASMIDS, values[13]);
            row.put(Header.STRAIN_KEYWORDS, values[14]);
            row.put(Header.STRAIN_SUMMARY, values[15]);
            row.put(Header.STRAIN_NOTES, values[16]);
            row.put(Header.STRAIN_REFERENCES, values[17]);
            row.put(Header.STRAIN_SEQUENCE_FILENAME, values[18]);
            row.put(Header.STRAIN_ATTACHMENT_FILENAME, values[19]);
            row.put(Header.RACK, values[20]);
            row.put(Header.BARCODE, values[21]);
            row.put(Header.RACK_BACKUP1, values[22]);
            row.put(Header.RACK_BACKUP2, values[23]);
        }

        public String getValueFor(Header header) {
            return row.get(header);
        }
    }

    protected static Storage getStrainRoot() throws UtilityException {
        // get the strain storage root
        try {
            String uuid = ConfigurationManager.get(ConfigurationKey.STRAIN_STORAGE_ROOT).getValue();
            return StorageManager.get(uuid);
        } catch (ManagerException e) {
            throw new UtilityException(e);
        }
    }

    public static Storage createKeioWorkingCopyScheme() throws ManagerException, UtilityException {

        Storage strainSchemeRoot = getStrainRoot();

        ArrayList<Storage> keioSchemes = new ArrayList<Storage>();
        keioSchemes.add(new Storage("Plate", "Keio Copy Plate", StorageType.PLATE96, "", null));
        keioSchemes.add(new Storage("Well", "Keio Copy Well", StorageType.WELL, "", null));
        keioSchemes.add(new Storage("Tube", "Keio Copy Tube", StorageType.TUBE, "", null));

        String systemEmail = AccountManager.getSystemAccount().getEmail();
        Storage keioScheme = new Storage("Keio Working Copy", "Keio Collection",
                StorageType.SCHEME, systemEmail, strainSchemeRoot);
        keioScheme.setSchemes(keioSchemes);
        keioScheme = StorageManager.save(keioScheme);

        return keioScheme;
    }

    public static Storage createKeioBackupCopyScheme() throws UtilityException, ManagerException {

        Storage strainSchemeRoot = getStrainRoot();

        ArrayList<Storage> keioSchemes = new ArrayList<Storage>();
        keioSchemes.add(new Storage("Plate", "Keio Copy ", StorageType.PLATE96, "", null));
        keioSchemes.add(new Storage("Well", "Keio Copy ", StorageType.WELL, "", null));

        String systemEmail = AccountManager.getSystemAccount().getEmail();
        Storage keioSchemeBackup = new Storage("Keio Copy", "Keio Copy", StorageType.SCHEME,
                systemEmail, strainSchemeRoot);
        keioSchemeBackup.setSchemes(keioSchemes);
        keioSchemeBackup = StorageManager.save(keioSchemeBackup);

        return keioSchemeBackup;
    }

    /**
     * parses csv file and extracts values
     * 
     * @param file
     *            csv file to parse
     * @return List of mapping of field->value parsed
     * @throws UtilityException
     */
    public static List<StrainRow> parseStrainFile(File file) throws UtilityException {
        BufferedReader bufferedReader = null;
        CSVReader csvReader = null;
        List<String[]> parsedCsvContent = null;
        ArrayList<StrainRow> result = new ArrayList<StrainRow>();

        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            csvReader = new CSVReader(bufferedReader, '\t', '\"');
            parsedCsvContent = csvReader.readAll();
        } catch (FileNotFoundException e) {
            throw new UtilityException(e);
        } catch (IOException e) {
            throw new UtilityException(e);
        }

        if (parsedCsvContent != null) {
            StrainRow row = null;
            for (int i = 1; i < parsedCsvContent.size(); i++) { //skip first line
                row = new StrainRow(parsedCsvContent.get(i));
                result.add(row);
            }
        }
        return result;
    }

    private static Strain createStrain(EntryController controller, StrainRow row)
            throws UtilityException {

        Strain strain = new Strain();

        // name
        HashSet<Name> strainNames = new HashSet<Name>();
        String name = row.getValueFor(Header.STRAIN_NAME);
        strainNames.add(new Name(name, strain));
        strain.setNames(strainNames);

        // creator
        String creator = row.getValueFor(Header.CREATOR);
        strain.setCreator(creator);
        strain.setOwner(creator);

        // creator email
        String email = row.getValueFor(Header.CREATOR_EMAIL);
        strain.setCreatorEmail(email);
        strain.setOwnerEmail(email);

        //funding source
        EntryFundingSource newStrainFundingSource = new EntryFundingSource();
        newStrainFundingSource.setEntry(strain);
        FundingSource fundingSource = new FundingSource();
        String fundingSourceValue = row.getValueFor(Header.FUNDING_SOURCE);
        fundingSource.setFundingSource(fundingSourceValue);

        // PI
        String PI = row.getValueFor(Header.PRINCIPAL_INVESTIGATOR);
        fundingSource.setPrincipalInvestigator(PI);
        newStrainFundingSource.setFundingSource(fundingSource);
        Set<EntryFundingSource> strainFundingSources = new LinkedHashSet<EntryFundingSource>();
        strainFundingSources.add(newStrainFundingSource);
        strain.setEntryFundingSources(strainFundingSources);

        // Host
        String host = row.getValueFor(Header.STRAIN_PARENT);
        strain.setHost(host);

        // gen/phen
        String genPhen = row.getValueFor(Header.STRAIN_GEN_PHEN);
        strain.setGenotypePhenotype(genPhen);

        // plasmids (TODO: Handle multiple plasmids)
        String plasmids = row.getValueFor(Header.STRAIN_PLASMIDS);
        strain.setPlasmids(plasmids);

        // selection markers
        HashSet<SelectionMarker> selectionMarkers = new HashSet<SelectionMarker>();
        String selectionMarkersValue = row.getValueFor(Header.STRAIN_SELECTION_MARKERS);
        if (selectionMarkersValue != null) {
            SelectionMarker marker = new SelectionMarker(selectionMarkersValue, strain);
            selectionMarkers.add(marker);
        }
        strain.setSelectionMarkers(selectionMarkers);

        // try to see if the plasmid exists, and smart link that instead
        Plasmid plasmid;
        try {
            String strainPlasmids = row.getValueFor(Header.STRAIN_PLASMIDS);
            plasmid = (Plasmid) controller.getByPartNumber(strainPlasmids);
        } catch (ControllerException e1) {
            throw new UtilityException(e1);
        } catch (PermissionException e1) {
            throw new UtilityException(e1);
        }
        if (plasmid != null) {
            strain.setPlasmids("[[jbei:" + plasmid.getOnePartNumber().getPartNumber() + "|"
                    + plasmid.getOneName().getName() + "]]");
        }

        // alias
        String alias = row.getValueFor(Header.STRAIN_ALIAS);
        strain.setAlias(alias);

        // status
        String status = row.getValueFor(Header.STRAIN_STATUS).toLowerCase();
        strain.setStatus(status);

        // keywords
        String keywords = row.getValueFor(Header.STRAIN_KEYWORDS);
        strain.setKeywords(keywords);

        // short description
        String summary = row.getValueFor(Header.STRAIN_SUMMARY);
        strain.setShortDescription(summary);

        // strain references
        String refs = row.getValueFor(Header.STRAIN_REFERENCES);
        strain.setReferences(refs);

        // bio safety level
        strain.setBioSafetyLevel(1);

        // patent info
        String patentInfo = row.getValueFor(Header.INTELLECTUAL_PROPERTY_INFO);
        strain.setIntellectualProperty(patentInfo);

        // notes
        String notes = row.getValueFor(Header.STRAIN_NOTES);
        strain.setLongDescription(notes);
        strain.setLongDescriptionType(Entry.MarkupType.text.name());

        return strain;
    }

    public static void createNewStrains(List<StrainRow> parsedContent) throws UtilityException {

        int index = 0;
        Storage workingCopyScheme = null;
        Storage backupScheme = null;

        try {
            workingCopyScheme = createKeioWorkingCopyScheme();
            backupScheme = createKeioBackupCopyScheme();
        } catch (ManagerException m) {
            throw new UtilityException(m);
        }

        long time = System.currentTimeMillis();

        for (StrainRow row : parsedContent) {
            Account account = null;
            String rack = row.getValueFor(Header.RACK);
            String barcode = row.getValueFor(Header.BARCODE);
            String rackCopy1 = row.getValueFor(Header.RACK_BACKUP1);
            String rackCopy2 = row.getValueFor(Header.RACK_BACKUP2);

            if (index == 96) {
                index = 0;
                long completed = (System.currentTimeMillis() - time) / 1000;
                System.out.println("Completed 3 racks in " + completed + "s");
                time = System.currentTimeMillis();
            }

            try {
                String email = row.getValueFor(Header.CREATOR_EMAIL);
                account = AccountManager.getByEmail(email);
            } catch (ManagerException e2) {
                throw new UtilityException(e2);
            }

            EntryController entryController = new EntryController(account);
            SampleController sampleController = new SampleController(account);

            Strain strain = createStrain(entryController, row);
            Strain newStrain = null;
            try {
                newStrain = (Strain) entryController.createEntry(strain, false, true);
            } catch (ControllerException e1) {
                throw new UtilityException(e1);
            }

            // main sample
            String location = StorageUtils.indexToWell(index, StorageType.PLATE96);
            if (location == null || location.length() != 3)
                throw new UtilityException("Could not map index to location");

            try {

                Storage original = StorageManager.getLocation(workingCopyScheme, new String[] {
                        rack, location, barcode });
                Storage b1 = StorageManager.getLocation(backupScheme, new String[] { rackCopy1,
                        location });

                Storage b2 = StorageManager.getLocation(backupScheme, new String[] { rackCopy2,
                        location });

                // actual sample
                Sample sample = sampleController.createSample("Keio Working Copy",
                    account.getEmail(), "");
                sample.setEntry(newStrain);
                sample.setStorage(original);

                // backup samples
                Sample backup1 = sampleController.createSample("Keio Backup 1", account.getEmail(),
                    "");
                backup1.setEntry(newStrain);
                backup1.setStorage(b1);

                Sample backup2 = sampleController.createSample("Keio Backup 2", account.getEmail(),
                    "");
                backup2.setEntry(newStrain);
                backup2.setStorage(b2);

                try {
                    sampleController.saveSample(backup2, false);
                    sampleController.saveSample(backup1, false);
                    sampleController.saveSample(sample, false);

                } catch (PermissionException pe) {
                    throw new UtilityException(pe);
                }
            } catch (ControllerException ce) {
                throw new UtilityException(ce);
            } catch (ManagerException me) {
                throw new UtilityException(me);
            }

            index += 1;
        }
    }

    public static void main(String[] args) {
        String fileName = "/home/hector/Downloads/Keio_Collection_fake.csv";
        File csvFile = new File(fileName);

        if (csvFile.canRead()) {

            List<StrainRow> parsedContent;
            try {
                parsedContent = parseStrainFile(csvFile);
                createNewStrains(parsedContent);
                System.out.println("Parsed: " + parsedContent.size());
            } catch (UtilityException e) {
                e.printStackTrace();
            }
        } else
            System.out.println("Could not read file " + csvFile.getAbsolutePath());
    }
}
