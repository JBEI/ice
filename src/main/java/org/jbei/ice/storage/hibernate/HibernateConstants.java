package org.jbei.ice.storage.hibernate;

/**
 * Hibernate mapping keys for database configuration and other parameters
 *
 * @author Hector Plahars
 */
public final class HibernateConstants {
    // JDBC URL to database
    public static final String CONNECTION_URL = "hibernate.connection.url";

    // JDBC driver class (value depends on driver e.g. Oracle or Postgresql)
    public static final String CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";

    // Username for the database
    public static final String CONNECTION_USERNAME = "hibernate.connection.username";

    // password for user access to the database
    public static final String CONNECTION_PASSWORD = "hibernate.connection.password";

    // appropriate SQL dialect for specific database
    public static final String DIALECT = "hibernate.dialect";

    // hibernate search storage directory
    public static final String SEARCH_BACKEND_DIRECTORY = "hibernate.search.backend.directory.root";
}
