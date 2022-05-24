package org.jbei.ice.lib.account.authentication.ldap;import org.jbei.ice.lib.common.logging.Logger;import org.jbei.ice.lib.config.ConfigurationSettings;import org.jbei.ice.lib.dto.ConfigurationKey;import java.io.FileInputStream;import java.io.IOException;import java.nio.file.Path;import java.nio.file.Paths;import java.util.Properties;public class LdapProperties {    private static LdapProperties INSTANCE;    private final Properties properties;    private LdapProperties() throws IOException {        properties = new Properties();        load();    }    public static LdapProperties getInstance() throws IOException {        if (INSTANCE == null)            INSTANCE = new LdapProperties();        return INSTANCE;    }    private void load() throws IOException {        String dataDirectory = new ConfigurationSettings().getPropertyValue(ConfigurationKey.DATA_DIRECTORY);        Path path = Paths.get(dataDirectory, "config", "ldap-config.properties");        Logger.info("Loading LDAP config properties from '" + path.toString() + "'");        FileInputStream in = new FileInputStream(path.toFile());        properties.load(in);        in.close();    }    String getWhiteListString() {        return properties.getProperty("LDAP_WHITELIST_GROUPS");    }    String getGroupBaseDN() {        return properties.getProperty("LDAP_GROUP_BASE_DN");    }    String getGroupQuery() {        return properties.getProperty("LDAP_GROUP_QUERY");    }    String getUserBaseDN() {        return properties.getProperty("LDAP_USER_BASE_DN");    }    String getUserQuery() {        return properties.getProperty("LDAP_USER_QUERY");    }    String getLDAPQuery() {        return properties.getProperty("LDAP_QUERY");    }    String getOrganization() {        return properties.getProperty("LDAP_ORGANIZATION");    }    String getAuthenticationUrl() {        return properties.getProperty("LDAP_AUTHENTICATION_URL");    }    String getSearchUrl() {        return properties.getProperty("LDAP_SEARCH_URL");    }    String getLdapContextFactory() {        return "com.sun.jndi.ldap.LdapCtxFactory";    }    String getEmployeeIdentifier() {        return properties.getProperty("LDAP_EMPLOYEE_IDENTIFIER_NAME");    }    String getOrganizationEmailDomain() {        return properties.getProperty("LDAP_ORGANIZATION_EMAIL_DOMAIN");    }    String getProperty(String name, String defaultValue) {        return properties.getProperty(name, defaultValue);    }    // write a template of the expected file    // this is expected to be used in conjunction with init//    public void writeTemplate() throws IOException {//        String dataDirectory = new Settings().getValue(ConfigurationValue.DATA_DIR);//        try (FileOutputStream out = new FileOutputStream(Paths.get(dataDirectory, "config", "ldap-config.properties.dist").toFile())) {//            properties.store(out, "Writing default");//        }//    }}