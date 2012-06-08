package org.jbei.ice.lib.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.dao.DAOException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.managers.ConfigurationManager;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.GroupManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.StorageManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.AccountFundingSource;
import org.jbei.ice.lib.models.AnnotationLocation;
import org.jbei.ice.lib.models.Configuration;
import org.jbei.ice.lib.models.Configuration.ConfigurationKey;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.EntryFundingSource;
import org.jbei.ice.lib.models.FundingSource;
import org.jbei.ice.lib.models.Group;
import org.jbei.ice.lib.models.SequenceFeature;
import org.jbei.ice.lib.models.SequenceFeatureAttribute;
import org.jbei.ice.lib.models.Storage;
import org.jbei.ice.lib.models.Storage.StorageType;
import org.jbei.ice.lib.permissions.PermissionManager;

/**
 * Populate an empty database with necessary objects and values.
 * 
 * @author Timothy Ham, Zinovii Dmytriv
 * 
 */
public class PopulateInitialDatabase {
    public static final String DEFAULT_PLASMID_STORAGE_SCHEME_NAME = "Plasmid Storage (Default)";
    public static final String DEFAULT_STRAIN_STORAGE_SCHEME_NAME = "Strain Storage (Default)";
    public static final String DEFAULT_PART_STORAGE_SCHEME_NAME = "Part Storage (Default)";
    public static final String DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME = "Arabidopsis Storage (Default)";

    // Database schema version.
    // If you are extending the existing schema to suit your needs, we suggest using the
    // naming scheme "custom-[your institute]-[your version]", as
    // the system will try to upgrade schemas of known older versions.
    // Setting the correct parent schema version may help you in the future.
    public static final String DATABASE_SCHEMA_VERSION = "0.9.0";
    public static final String PARENT_DATABASE_SCHEMA_VERSION = "0.8.1";

    // This is a global "everyone" uuid
    public static String everyoneGroup = "8746a64b-abd5-4838-a332-02c356bbeac0";

    // This is the system account: "system" as the email, and "System" as last name
    public static String systemAccountEmail = "system";
    public static String adminAccountEmail = "Administrator";
    public static String adminAccountDefaultPassword = "Administrator";

    /**
     * Populate an empty database with necessary objects and values.
     * <p>
     * <ul>
     * <li>Create the everyone group.</li>
     * <li>Create the System account.</li>
     * <li>Create the Admin account.</li>
     * <li>Create default storage schemes.</li>
     * <li>Update the database schema, if necessary.</li>
     * </ul>
     * 
     * @throws UtilityException
     */
    public static void initializeDatabase() throws UtilityException {
        Group group1 = null;
        try {
            group1 = GroupManager.get(everyoneGroup);
        } catch (ManagerException e) {
            throw new UtilityException(e);
        }

        if (group1 == null) {
            // Since everyone group doesn't exist, assume database is new
            // Put all other db initialization below.
            createFirstGroup();
        }

        createSystemAccount();
        createAdminAccount();

        populateDefaultStorageLocationsAndSchemes();
        updateDatabaseSchema();
    }

    /**
     * Create default root node Storage for each part types
     */
    private static void populateDefaultStorageLocationsAndSchemes() throws UtilityException {
        Configuration strainRootConfig = null;
        Configuration plasmidRootConfig = null;
        Configuration partRootConfig = null;
        Configuration arabidopsisRootConfig = null;
        Storage strainRoot = null;
        Storage plasmidRoot = null;
        Storage partRoot = null;
        Storage arabidopsisSeedRoot = null;

        try {
            // read configuration
            strainRootConfig = ConfigurationManager.get(ConfigurationKey.STRAIN_STORAGE_ROOT);
            plasmidRootConfig = ConfigurationManager.get(ConfigurationKey.PLASMID_STORAGE_ROOT);
            partRootConfig = ConfigurationManager.get(ConfigurationKey.PART_STORAGE_ROOT);
            arabidopsisRootConfig = ConfigurationManager
                    .get(ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT);
        } catch (ManagerException e1) {
            throw new UtilityException(e1);
        }

        // if null, create root storage and config for entry types
        try {
            if (strainRootConfig == null) {
                strainRoot = new Storage("Strain Storage Root", "Default Strain Storage Root",
                        StorageType.GENERIC, systemAccountEmail, null);
                strainRoot = StorageManager.save(strainRoot);
                strainRootConfig = new Configuration(ConfigurationKey.STRAIN_STORAGE_ROOT,
                        strainRoot.getUuid());
                ConfigurationManager.save(strainRootConfig);

                Storage defaultStrain = new Storage(DEFAULT_STRAIN_STORAGE_SCHEME_NAME,
                        DEFAULT_STRAIN_STORAGE_SCHEME_NAME, StorageType.SCHEME, systemAccountEmail,
                        strainRoot);
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
                schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
                schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
                defaultStrain.setSchemes(schemes);
                defaultStrain = StorageManager.save(defaultStrain);
                ConfigurationManager.save(new Configuration(
                        ConfigurationKey.STRAIN_STORAGE_DEFAULT, defaultStrain.getUuid()));

            }
            if (plasmidRootConfig == null) {
                plasmidRoot = new Storage("Plasmid Storage Root", "Default Plasmid Storage Root",
                        StorageType.GENERIC, systemAccountEmail, null);
                plasmidRoot = StorageManager.save(plasmidRoot);
                plasmidRootConfig = new Configuration(ConfigurationKey.PLASMID_STORAGE_ROOT,
                        plasmidRoot.getUuid());
                ConfigurationManager.save(plasmidRootConfig);

                Storage defaultPlasmid = new Storage(DEFAULT_PLASMID_STORAGE_SCHEME_NAME,
                        DEFAULT_PLASMID_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                        systemAccountEmail, plasmidRoot);
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
                schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
                schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
                defaultPlasmid.setSchemes(schemes);
                defaultPlasmid = StorageManager.save(defaultPlasmid);
                ConfigurationManager.save(new Configuration(
                        ConfigurationKey.PLASMID_STORAGE_DEFAULT, defaultPlasmid.getUuid()));
            }
            if (partRootConfig == null) {
                partRoot = new Storage("Part Storage Root", "Default Part Storage Root",
                        StorageType.GENERIC, systemAccountEmail, null);
                partRoot = StorageManager.save(partRoot);
                partRoot = StorageManager.save(partRoot);
                partRootConfig = new Configuration(ConfigurationKey.PART_STORAGE_ROOT,
                        partRoot.getUuid());
                ConfigurationManager.save(partRootConfig);

                Storage defaultPart = new Storage(DEFAULT_PART_STORAGE_SCHEME_NAME,
                        DEFAULT_PART_STORAGE_SCHEME_NAME, StorageType.SCHEME, systemAccountEmail,
                        partRoot);
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
                schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
                schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
                defaultPart.setSchemes(schemes);
                defaultPart = StorageManager.save(defaultPart);
                ConfigurationManager.save(new Configuration(ConfigurationKey.PART_STORAGE_DEFAULT,
                        defaultPart.getUuid()));
            }
            if (arabidopsisRootConfig == null) {
                arabidopsisSeedRoot = new Storage("Arabidopsis Storage Root",
                        "Default Arabidopsis Seed Storage Root", StorageType.GENERIC,
                        systemAccountEmail, null);
                arabidopsisSeedRoot = StorageManager.save(arabidopsisSeedRoot);
                arabidopsisRootConfig = new Configuration(
                        ConfigurationKey.ARABIDOPSIS_STORAGE_ROOT, arabidopsisSeedRoot.getUuid());
                ConfigurationManager.save(arabidopsisRootConfig);

                Storage defaultArabidopsis = new Storage(DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME,
                        DEFAULT_ARABIDOPSIS_STORAGE_SCHEME_NAME, StorageType.SCHEME,
                        systemAccountEmail, arabidopsisSeedRoot);
                ArrayList<Storage> schemes = new ArrayList<Storage>();
                schemes.add(new Storage("Shelf", "", StorageType.SHELF, "", null));
                schemes.add(new Storage("Box", "", StorageType.BOX_UNINDEXED, "", null));
                schemes.add(new Storage("Tube", "", StorageType.TUBE, "", null));
                defaultArabidopsis.setSchemes(schemes);
                defaultArabidopsis = StorageManager.save(defaultArabidopsis);
                ConfigurationManager
                        .save(new Configuration(ConfigurationKey.ARABIDOPSIS_STORAGE_DEFAULT,
                                defaultArabidopsis.getUuid()));
            }
        } catch (ManagerException e) {
            throw new UtilityException(e);
        }

    }

    /**
     * Update the database schema.
     * 
     * @throws UtilityException
     */
    private static void updateDatabaseSchema() throws UtilityException {
        Configuration databaseSchema = null;

        try {
            databaseSchema = ConfigurationManager.get(ConfigurationKey.DATABASE_SCHEMA_VERSION);
            if (databaseSchema == null) {
                databaseSchema = new Configuration(ConfigurationKey.DATABASE_SCHEMA_VERSION,
                        DATABASE_SCHEMA_VERSION);
                ConfigurationManager.save(databaseSchema);
            }

            if (databaseSchema.getValue().equals(PARENT_DATABASE_SCHEMA_VERSION)) {
                // do schema upgrade
                boolean error = migrateFrom081To090();
                if (!error) {
                    databaseSchema.setValue(DATABASE_SCHEMA_VERSION);
                    ConfigurationManager.save(databaseSchema);
                }
            }

        } catch (ManagerException e) {
            throw new UtilityException(e);
        }
    }

    /**
     * Check for, and create first admin account
     * 
     * @throws UtilityException
     */
    private static void createAdminAccount() throws UtilityException {
        try {
            AccountController controller = new AccountController();
            Account adminAccount = controller.createAdminAccount(adminAccountEmail,
                adminAccountDefaultPassword);
            if (adminAccount == null)
                throw new UtilityException("Could not create admin account");
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
    }

    /**
     * Check for and create the System account.
     * 
     * @throws UtilityException
     */
    private static void createSystemAccount() throws UtilityException {
        // Check for, and create system account
        Account systemAccount = null;
        try {
            systemAccount = AccountController.getByEmail(systemAccountEmail);
        } catch (ControllerException e) {
            throw new UtilityException(e);
        }
        if (systemAccount == null) {
            // since system account doesn't exist, initialize a new system account
            systemAccount = new Account();
            systemAccount.setEmail(systemAccountEmail);
            systemAccount.setLastName("");
            systemAccount.setFirstName("");
            systemAccount.setInitials("");
            systemAccount.setInstitution("");
            systemAccount.setPassword("");
            systemAccount.setDescription("System Account");
            systemAccount.setIsSubscribed(0);
            systemAccount.setIp("");
            Date currentTime = Calendar.getInstance().getTime();
            systemAccount.setCreationTime(currentTime);
            systemAccount.setModificationTime(currentTime);
            systemAccount.setLastLoginTime(currentTime);

            try {
                AccountController controller = new AccountController();
                controller.save(systemAccount);
            } catch (ControllerException e) {
                String msg = "Could not create system account: " + e.toString();
                Logger.error(msg, e);
            }
        }
    }

    /**
     * Check for and create the everyone group.
     * 
     * @return Everyone group.
     */
    public static Group createFirstGroup() {
        Group group1 = null;
        try {
            group1 = GroupManager.get(everyoneGroup);

        } catch (ManagerException e) {
            String msg = "Could not get everyone group " + e.toString();
            Logger.info(msg);
        }

        if (group1 == null) {
            Group group = new Group();
            group.setLabel("Everyone");
            group.setDescription("Everyone");
            group.setParent(null);

            group.setUuid(everyoneGroup);
            try {
                GroupManager.save(group);
                Logger.info("Creating everyone group");
                group1 = group;
            } catch (ManagerException e) {
                String msg = "Could not save everyone group: " + e.toString();
                Logger.error(msg, e);
            }
        }

        return group1;
    }

    /**
     * Populate the permission read group. For schema upgrade only.
     */
    public static void populatePermissionReadGroup() {
        Group group1 = null;
        try {
            group1 = GroupManager.get(everyoneGroup);
        } catch (ManagerException e) {
            // nothing happens
            Logger.debug(e.toString());
        }
        if (group1 != null) {
            ArrayList<Entry> allEntries = null;
            try {
                allEntries = EntryManager.getAllEntries();
            } catch (ManagerException e1) {
                e1.printStackTrace();
            }
            for (Entry entry : allEntries) {
                try {
                    Set<Group> groups = PermissionManager.getReadGroup(entry);
                    int originalSize = groups.size();
                    groups.add(group1);
                    PermissionManager.setReadGroup(entry, groups);

                    String msg = "updated id:" + entry.getId() + " from " + originalSize + " to "
                            + groups.size() + ".";
                    Logger.info(msg);
                } catch (ManagerException e) {
                    // skip
                    Logger.debug(e.toString());
                }

            }
        }
    }

    /**
     * Process funding sources. For schema update.
     * 
     * @param dupeFundingSource
     * @throws DAOException
     */
    @SuppressWarnings("unchecked")
    public static void normalizeFundingSources(FundingSource dupeFundingSource) throws DAOException {

        String queryString = "from " + FundingSource.class.getName()
                + " where fundingSource=:fundingSource AND"
                + " principalInvestigator=:principalInvestigator";
        Session session = DAO.newSession();
        Query query = session.createQuery(queryString);
        query.setParameter("fundingSource", dupeFundingSource.getFundingSource());
        query.setParameter("principalInvestigator", dupeFundingSource.getPrincipalInvestigator());
        ArrayList<FundingSource> dupeFundingSources = new ArrayList<FundingSource>();
        try {
            dupeFundingSources = new ArrayList<FundingSource>(query.list());
        } catch (HibernateException e) {
            Logger.error("Could not get funding sources " + e.toString(), e);
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        FundingSource keepFundingSource = dupeFundingSources.get(0);
        for (int i = 1; i < dupeFundingSources.size(); i++) {
            FundingSource deleteFundingSource = dupeFundingSources.get(i);
            // normalize EntryFundingSources
            queryString = "from " + EntryFundingSource.class.getName()
                    + " where fundingSource=:fundingSource";
            session = DAO.newSession();
            query = session.createQuery(queryString);
            query.setParameter("fundingSource", deleteFundingSource);
            List<EntryFundingSource> entryFundingSources = null;
            try {
                entryFundingSources = (query).list();
            } catch (HibernateException e) {
                Logger.error("Could not get funding sources " + e.toString(), e);
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }

            for (EntryFundingSource entryFundingSource : entryFundingSources) {
                try {
                    entryFundingSource.setFundingSource(keepFundingSource);
                    DAO.save(entryFundingSource);
                } catch (DAOException e) {
                    throw e;
                }
            }

            // normalize AccountFundingSources
            queryString = "from " + AccountFundingSource.class.getName()
                    + " where fundingSource=:fundingSource";
            session = DAO.newSession();
            query = session.createQuery(queryString);
            query.setParameter("fundingSource", deleteFundingSource);
            List<AccountFundingSource> accountFundingSources = null;
            try {
                accountFundingSources = query.list();
            } catch (HibernateException e) {
                Logger.error("Could not get funding sources " + e.toString(), e);
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }

            for (AccountFundingSource accountFundingSource : accountFundingSources) {
                accountFundingSource.setFundingSource(keepFundingSource);
                try {
                    DAO.save(accountFundingSource);
                } catch (DAOException e) {
                    String msg = "Could set normalized entry funding source: " + e.toString();
                    Logger.error(msg, e);
                }
            }
            try {
                String temp = deleteFundingSource.getPrincipalInvestigator() + ":"
                        + deleteFundingSource.getFundingSource();
                DAO.delete(deleteFundingSource);
                Logger.info("Normalized funding source: " + temp);
            } catch (DAOException e) {
                String msg = "Could not delete funding source during normalization: "
                        + e.toString();
                Logger.error(msg, e);
            }
        }
    }

    /**
     * parse SequenceFeature.description and populate SequenceFeatureAttribute.
     * 
     */
    @SuppressWarnings("deprecation")
    public static boolean migrateFrom080To081() {
        Logger.warn("Updating database schema from 0.8.0 to 0.8.1. Please wait...");
        Logger.info("reading database");
        boolean error = false;
        // get all sequence features
        Session session = DAO.newSession();
        String queryString = "from " + SequenceFeature.class.getName();
        Query query = session.createQuery(queryString);

        @SuppressWarnings("rawtypes")
        List queryResults = query.list();
        session.close();

        Logger.info("parsing fields");
        ArrayList<SequenceFeature> sequenceFeatures = new ArrayList<SequenceFeature>();
        for (Object item : queryResults) {
            sequenceFeatures.add((SequenceFeature) item);
        }

        // parse description as attributes
        for (SequenceFeature sequenceFeature : sequenceFeatures) {
            List<SequenceFeatureAttribute> parsedAttributes = parseDescription(sequenceFeature
                    .getDescription());

            for (SequenceFeatureAttribute attribute : parsedAttributes) {
                attribute.setSequenceFeature(sequenceFeature);
                try {
                    // save
                    DAO.save(attribute);
                } catch (DAOException e) {
                    Logger.error("Error saving parsed SequenceFeatureAttribute", e);
                    error = true;
                }
            }
            // delete description and save
            if (!error) {
                sequenceFeature.setDescription("");
                try {
                    DAO.save(sequenceFeature);
                } catch (DAOException e) {
                    Logger.error("Error saving cleaned SequenceFeature", e);
                    error = true;
                }
            }

        }
        if (error) {
            Logger.error("Error converting database schema from 0.8.0 to 0.8.1. Restore from backup!");
        }

        return error;
    }

    /**
     * Convert SequenceFeature.start and ends to locations.
     * 
     * @return True if error exists..
     */
    @SuppressWarnings({ "unchecked", "deprecation" })
    public static boolean migrateFrom081To090() {
        Logger.warn("Updating database schema from 0.8.1 to 0.9.0. Please wait...");
        Logger.info("reading database");
        boolean error = false;

        /* Changes: Creation of new AnnotationLocation table. Move all 
        SequenceFeature genbankStart/end to AnnotationLocations */
        String queryString = "from " + SequenceFeature.class.getName();
        Session session = DAO.newSession();
        Query query = session.createQuery(queryString);
        LinkedList<SequenceFeature> sequenceFeatures = null;
        sequenceFeatures = new LinkedList<SequenceFeature>(query.list());
        session.close();

        Logger.info("parsing fields");
        for (SequenceFeature sequenceFeature : sequenceFeatures) {
            AnnotationLocation location = new AnnotationLocation(sequenceFeature.getGenbankStart(),
                    sequenceFeature.getEnd(), sequenceFeature);
            sequenceFeature.setGenbankStart(1);
            sequenceFeature.setEnd(1);
            try {
                DAO.save(location);
                DAO.save(sequenceFeature);
            } catch (DAOException e) {
                Logger.error("Error saving new locations", e);
                error = true;
            }
        }

        if (error) {
            Logger.error("Error converting database schema from 0.8.1 to 0.9.0. Restore from backup!");
        }

        return error;
    }

    /**
     * Clean up and parse the description field into SequenceFeature attributes.
     * 
     * @param row
     * @return
     */
    private static List<SequenceFeatureAttribute> parseDescription(String row) {
        Pattern uuidPattern = Pattern.compile("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}");
        Pattern keyValuePattern = Pattern.compile("\"*(\\w+)=\"*\\s{0,1}([^\"]+)\\s{0,1}\"*");

        ArrayList<SequenceFeatureAttribute> result = new ArrayList<SequenceFeatureAttribute>();

        for (String rowItem : row.split("\n")) {
            if ("\"\"".equals(rowItem) || "note=\"\"\"\"".equals(rowItem)
                    || "note=\"\"\"".equals(rowItem) || "description=".equals(rowItem)
                    || "description=\"".equals(rowItem)) {
                return result;
            }

            Matcher uuidMatcher = uuidPattern.matcher(rowItem);
            Matcher keyValueMatcher = keyValuePattern.matcher(rowItem);
            SequenceFeatureAttribute sfa = null;

            if (uuidMatcher.find()) {
                sfa = new SequenceFeatureAttribute();
                sfa.setKey("record_id");
                sfa.setValue(uuidMatcher.group());
                sfa.setQuoted(false);
                result.add(sfa);
            }
            try {
                while (keyValueMatcher.find()) {
                    sfa = new SequenceFeatureAttribute();

                    sfa.setKey(keyValueMatcher.group(1).trim());
                    sfa.setValue(keyValueMatcher.group(2).trim());
                    sfa.setQuoted(false);
                    result.add(sfa);
                }
            } catch (IllegalStateException e) {
                Logger.warn("Could not parse " + rowItem);
                continue;
            }
        }
        return result;
    }
}
