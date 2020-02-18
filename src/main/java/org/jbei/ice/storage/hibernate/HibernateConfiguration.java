package org.jbei.ice.storage.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.service.ServiceRegistry;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.model.*;

import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuration for Hibernate database connection and sessions
 *
 * @author Hector Plahar
 */
public class HibernateConfiguration {

    // thread safe global object that is instantiated once
    private static SessionFactory sessionFactory;

    // singleton
    private HibernateConfiguration() {
    }

    /**
     * Configure the database using the (optional) connection properties
     *
     * @param dbType        database type
     * @param properties    optional connection properties. Required only if
     *                      DbType is <code>POSTGRESQL</code>
     * @param dataDirectory path to the data directory optional for in memory database
     */
    public static synchronized void initialize(DbType dbType, Properties properties, Path dataDirectory) {
        if (sessionFactory != null) {
            Logger.info("Database already configured. Close/reset to re-configure");
            return;
        }

        Logger.info("Initializing session factory for type " + dbType.name());
        Configuration configuration = new Configuration();

        switch (dbType) {
            case H2DB:
            default:
                configureH2Db(configuration, dataDirectory.toString());
                break;

            case MEMORY:
                configureInMemoryDb(configuration);
                break;

            case POSTGRESQL:
                configurePostgresDb(configuration, properties, dataDirectory.toString());
                break;
        }

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        addAnnotatedClasses(configuration);
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);
    }

    private static void configurePostgresDb(Configuration configuration, Properties properties, String dbPath) {
        // load (additional) base configuration
        configuration.configure();
        String url = properties.getProperty("connectionUrl");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        configuration.setProperty("hibernate.connection.url", "jdbc:postgresql://" + url + "/" + username);
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.search.default.indexBase", dbPath + "/data/lucene-data");
    }

    private static void configureH2Db(Configuration configuration, String dbPath) {
        configuration.configure();
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:" + dbPath + "/db/ice-h2db");
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.username", "sa");
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.search.default.indexBase", dbPath + "/data/lucene-data");
    }

    private static void configureInMemoryDb(Configuration configuration) {
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:mem:test");
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.username", "sa");
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.current_session_context_class",
                "org.hibernate.context.internal.ThreadLocalSessionContext");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.search.default.directory_provider",
                "org.hibernate.search.store.impl.RAMDirectoryProvider");
    }

    public static FullTextSession getFullTextSession() {
        return Search.getFullTextSession(currentSession());
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

    public static boolean isInitialized() {
        return sessionFactory != null && sessionFactory.isOpen();
    }

    /**
     * @return session bound to context.
     */
    protected static Session currentSession() {
        return sessionFactory.getCurrentSession();
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
     * Initialize a in-memory mock database for testing.
     */
    public static void initializeMock() {
        initialize(DbType.MEMORY, null, null);
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
        configuration.addAnnotatedClass(Account.class);
        configuration.addAnnotatedClass(Attachment.class);
        configuration.addAnnotatedClass(Sample.class);
        configuration.addAnnotatedClass(Group.class);
        configuration.addAnnotatedClass(TraceSequence.class);
        configuration.addAnnotatedClass(TraceSequenceAlignment.class);
        configuration.addAnnotatedClass(Storage.class);
        configuration.addAnnotatedClass(Folder.class);
        configuration.addAnnotatedClass(Parameter.class);
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

    public static void close() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            Logger.info("Closing session factory");
            sessionFactory.getCurrentSession().disconnect();
            sessionFactory.close();
        }
    }
}
