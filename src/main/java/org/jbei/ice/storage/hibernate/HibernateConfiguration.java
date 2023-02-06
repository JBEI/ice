package org.jbei.ice.storage.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.jbei.ice.dto.DataStorage;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.model.*;

import java.nio.file.Path;

/**
 * Helper class to Initialize Hibernate, and obtain new sessions.
 *
 * @author Hector Plahar
 */
public class HibernateConfiguration {

    // thread safe global object that is instantiated once
    private static SessionFactory sessionFactory;

    // singleton
    private HibernateConfiguration() {
    }

    public static void beginTransaction() {
        if (sessionFactory == null)
            return;

        if (!sessionFactory.getCurrentSession().getTransaction().isActive())
            sessionFactory.getCurrentSession().beginTransaction();
    }

    public static void commitTransaction() {
        if (sessionFactory == null)
            return;

        if (sessionFactory.getCurrentSession().getTransaction().isActive())
            sessionFactory.getCurrentSession().getTransaction().commit();
    }

    public static void rollbackTransaction() {
        if (sessionFactory == null)
            return;

        if (sessionFactory.getCurrentSession().getTransaction().isActive())
            sessionFactory.getCurrentSession().getTransaction().rollback();
    }

    /**
     * Open a new {@link Session} from the sessionFactory.
     * This needs to be closed when done with
     *
     * @return New Hibernate {@link Session}.
     */
    public static Session newSession() {
        return sessionFactory.openSession();
    }

    public static Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Configure the database using the storage parameters
     *
     * @param storage       wrapper around data type and associated parameters. If null, <code>MEMORY</code> is assumed
     * @param dataDirectory path to the data directory optional for in memory database
     */
    public static synchronized void initialize(DataStorage storage, Path dataDirectory) {
        if (sessionFactory != null) {
            Logger.info("Database already configured. Close/reset to re-configure");
            return;
        }

        if (storage == null)
            throw new IllegalArgumentException("Invalid storage params");

        Logger.info("Initializing session factory for type " + storage.getType().name());
        Configuration configuration = new Configuration();

        switch (storage.getType()) {
            case H2DB -> configureH2Db(configuration, dataDirectory);
            case MEMORY -> configureInMemoryDb(configuration);
            case POSTGRESQL -> configurePostgresDb(configuration, storage, dataDirectory);
            default -> throw new IllegalArgumentException("Invalid/Unhandled storage type: " + storage.getType());
        }

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        configuration.configure();
        addAnnotatedClasses(configuration);
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    /**
     * Configure hibernate to connect to a postgres db.
     * todo : validate
     *
     * @param configuration hibernate configuration API
     * @param storage       wrapper around storage params for connecting to a postgres database
     * @param dbPath        path to the data directory
     */
    private static void configurePostgresDb(Configuration configuration, DataStorage storage, Path dbPath) {
        // load (additional) base configuration
        configuration.configure();

        configuration.setProperty(HibernateConstants.CONNECTION_URL, "jdbc:postgresql://" + storage.getConnectionUrl() + "/" + storage.getDatabaseName());
        configuration.setProperty(HibernateConstants.CONNECTION_DRIVER_CLASS, "org.postgresql.Driver");
        configuration.setProperty(HibernateConstants.CONNECTION_USERNAME, storage.getDatabaseUser());
        configuration.setProperty(HibernateConstants.CONNECTION_PASSWORD, storage.getDatabasePassword());
        configuration.setProperty(HibernateConstants.SEARCH_BACKEND_DIRECTORY, dbPath + "/data/lucene-data");
    }

    private static void configureH2Db(Configuration configuration, Path dbPath) {
        configuration.configure();
        configuration.setProperty(HibernateConstants.CONNECTION_URL, "jdbc:h2:" + dbPath + "/db/ice-h2db");
        configuration.setProperty(HibernateConstants.CONNECTION_DRIVER_CLASS, "org.h2.Driver");
        configuration.setProperty(HibernateConstants.CONNECTION_USERNAME, "sa");
        configuration.setProperty(HibernateConstants.DIALECT, "org.hibernate.dialect.H2Dialect");
        configuration.setProperty(HibernateConstants.SEARCH_BACKEND_DIRECTORY, dbPath + "/data/lucene-data");
    }

    /**
     * Configure hibernate to use an in-memory/transient database. Currently used mostly for running unit tests
     * s     * @param configuration
     */
    private static void configureInMemoryDb(Configuration configuration) {
        configuration.setProperty(HibernateConstants.CONNECTION_URL, "jdbc:h2:mem:test");
        configuration.setProperty(HibernateConstants.CONNECTION_DRIVER_CLASS, "org.h2.Driver");
        configuration.setProperty(HibernateConstants.CONNECTION_USERNAME, "sa");
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.current_session_context_class",
            "org.hibernate.context.internal.ThreadLocalSessionContext");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.search.default.directory_provider",
            "org.hibernate.search.store.impl.RAMDirectoryProvider");
    }

    private static void addAnnotatedClasses(Configuration configuration) {
        configuration.addAnnotatedClass(Entry.class);
        configuration.addAnnotatedClass(Plasmid.class);
        configuration.addAnnotatedClass(Strain.class);
        configuration.addAnnotatedClass(Part.class);
        configuration.addAnnotatedClass(ArabidopsisSeed.class);
        configuration.addAnnotatedClass(Protein.class);
        configuration.addAnnotatedClass(Link.class);
        configuration.addAnnotatedClass(SelectionMarker.class);
        configuration.addAnnotatedClass(Sequence.class);
        configuration.addAnnotatedClass(Feature.class);
        configuration.addAnnotatedClass(SequenceFeature.class);
        configuration.addAnnotatedClass(SequenceFeatureAttribute.class);
        configuration.addAnnotatedClass(Comment.class);
        configuration.addAnnotatedClass(AccountModel.class);
        configuration.addAnnotatedClass(Attachment.class);
        configuration.addAnnotatedClass(Sample.class);
        configuration.addAnnotatedClass(org.jbei.ice.storage.model.AccountPreferences.class);
        configuration.addAnnotatedClass(Group.class);
        configuration.addAnnotatedClass(TraceSequence.class);
        configuration.addAnnotatedClass(TraceSequenceAlignment.class);
        configuration.addAnnotatedClass(ConfigurationModel.class);
        configuration.addAnnotatedClass(Storage.class);
        configuration.addAnnotatedClass(Folder.class);
        configuration.addAnnotatedClass(ParameterModel.class);
        configuration.addAnnotatedClass(AnnotationLocation.class);
        configuration.addAnnotatedClass(BulkUpload.class);
        configuration.addAnnotatedClass(Permission.class);
        configuration.addAnnotatedClass(Message.class);
        configuration.addAnnotatedClass(Preference.class);
        configuration.addAnnotatedClass(RemotePartner.class);
        configuration.addAnnotatedClass(Request.class);
        configuration.addAnnotatedClass(Audit.class);
        configuration.addAnnotatedClass(Experiment.class);
        configuration.addAnnotatedClass(ShotgunSequence.class);
        configuration.addAnnotatedClass(ConfigurationModel.class);
        configuration.addAnnotatedClass(ApiKey.class);
        configuration.addAnnotatedClass(RemoteClientModel.class);
        configuration.addAnnotatedClass(RemoteAccessModel.class);
        configuration.addAnnotatedClass(ManuscriptModel.class);
        configuration.addAnnotatedClass(FeatureCurationModel.class);
        configuration.addAnnotatedClass(CustomEntryFieldModel.class);
        configuration.addAnnotatedClass(CustomEntryFieldOptionModel.class);
        configuration.addAnnotatedClass(CustomEntryFieldValueModel.class);
        configuration.addAnnotatedClass(SequenceHistoryModel.class);
        configuration.addAnnotatedClass(SampleCreateModel.class);
    }

    /**
     * Initialize an in-memory mock database for testing.
     */
    public static void initializeMock() {
        initialize(null, null);
    }

    public static void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            Logger.info("Closing session factory");
            sessionFactory.getCurrentSession().close();
            sessionFactory.close();
        }
    }
}
