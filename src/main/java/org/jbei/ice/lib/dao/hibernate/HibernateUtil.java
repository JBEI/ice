package org.jbei.ice.lib.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.models.ShotgunSequence;

/**
 * Helper class to Initialize Hibernate, and obtain new sessions.
 *
 * @author Zinovii Dmytriv, Timothy Ham, Hector Plahar
 */
public class HibernateUtil {

    // thread safe global object that is instantiated once
    private static SessionFactory sessionFactory;

    // singleton
    private HibernateUtil() {
    }

    private static String BASE_FILE = "hibernate.cfg.xml";
    private static String MOCK_FILE = "mock.cfg.xml";
    private static String AWS_FILE = "aws.cfg.xml";
    private static String DEFAULT_FILE = "default.cfg.xml";

    /**
     * Open a new {@link Session} from the sessionFactory.
     * This needs to be closed when done with
     *
     * @return New Hibernate {@link Session}.
     */
    public static Session newSession() {
        return getSessionFactory().openSession();
    }

    /**
     * @return session bound to context.
     */
    protected static Session currentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public static void beginTransaction() {
        getSessionFactory().getCurrentSession().beginTransaction();
    }

    public static void commitTransaction() {
        getSessionFactory().getCurrentSession().getTransaction().commit();
    }

    public static void rollbackTransaction() {
        getSessionFactory().getCurrentSession().getTransaction().rollback();
    }

    /**
     * Initialize a in-memory mock database for testing.
     */
    public static void initializeMock() {
        initialize(Type.MOCK);
    }

    private static synchronized void initialize(Type type) {
        if (sessionFactory == null) {
            Logger.info("Initializing session factory for type " + type.name());
            Configuration configuration = new Configuration().configure(BASE_FILE);
            if (type == Type.MOCK) {
                configuration.configure(MOCK_FILE);
            } else {
                String environment = System.getProperty("environment");

                if (environment != null && environment.equals("aws")) {
                    configuration.configure(AWS_FILE);
                } else {
                    configuration.configure(DEFAULT_FILE);
                }
            }

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                    configuration.getProperties()).build();

            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.model.Entry.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.model.Plasmid.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.model.Strain.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.model.Part.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.model.ArabidopsisSeed.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.model.Link.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.SelectionMarker.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.Sequence.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.Feature.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.SequenceFeature.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.SequenceFeatureAttribute.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.Comment.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.account.model.Account.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.attachment.Attachment.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.sample.model.Sample.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.account.model.AccountPreferences.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.group.Group.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.TraceSequence.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.TraceSequenceAlignment.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.Configuration.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.Storage.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.folder.Folder.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.model.Parameter.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.AnnotationLocation.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.bulkupload.BulkUpload.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.access.Permission.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.message.Message.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.account.model.Preference.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.net.RemotePartner.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.entry.sample.model.Request.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.models.Audit.class);
            configuration.addAnnotatedClass(org.jbei.ice.lib.experiment.Experiment.class);
            configuration.addAnnotatedClass(ShotgunSequence.class);

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        }
    }

    /**
     * Retrieve the {@link SessionFactory}.
     *
     * @return Hibernate sessionFactory
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            initialize(Type.NORMAL);
        }
        return sessionFactory;
    }

    public static void close() {
        currentSession().disconnect();
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            Logger.info("Closing session factory");
            sessionFactory.close();
        }
    }

    /**
     * initialization types
     */
    private enum Type {
        NORMAL, MOCK;
    }
}
