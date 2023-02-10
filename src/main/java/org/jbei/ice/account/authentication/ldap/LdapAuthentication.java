package org.jbei.ice.account.authentication.ldap;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.account.Account;
import org.jbei.ice.account.authentication.AuthenticationException;
import org.jbei.ice.account.authentication.IAuthentication;
import org.jbei.ice.account.authentication.LocalAuthentication;
import org.jbei.ice.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.AccountModel;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authentication using LDAP.
 * <b>Note</b>
 * This expects a property file named <code>ldap-config.properties</code>
 * located in <code>{data-dir}/config</code> directory
 *
 * @author Hector Plahar
 */
public class LdapAuthentication implements IAuthentication {

    private DirContext dirContext;
    private final LdapProperties properties;

    public LdapAuthentication() throws IOException {
        properties = LdapProperties.getInstance();
    }

    @Override
    public String authenticates(String loginId, String password) throws AuthenticationException {
        if (StringUtils.isEmpty(loginId) || StringUtils.isEmpty(password)) {
            throw new AuthenticationException("Username and Password are mandatory!");
        }

        loginId = loginId.toLowerCase().trim();
        String emailSuffix = properties.getOrganizationEmailDomain();
        if (!StringUtils.isEmpty(emailSuffix)) {
            int idx = loginId.indexOf("@" + emailSuffix);
            if (idx > 0)
                loginId = loginId.substring(0, idx);
        }

        if (isLDAPUser(loginId)) {
            try {
                Account user = authenticateWithLDAP(loginId, password);
                checkCreateAccount(user);
                return user.getEmail();
            } catch (AuthenticationException ae) {
                Logger.warn("LDAP Authentication failed for user " + loginId);
                return null;
            }
        } else {
            // backup local authentication
            LocalAuthentication localBackend = new LocalAuthentication();
            return localBackend.authenticates(loginId, password);
        }
    }

    /**
     * Intended to be called when the credentials successfully authenticate with ldap.
     * Ensures an account exists with the login specified in the parameter which also belongs to the
     * LBL/JBEI group.
     * <p/>
     * Since LBL's LDAP mechanism handles authentication, no password information is
     * managed
     */
    private void checkCreateAccount(Account user) throws AuthenticationException {
        String loginId = user.getEmail();
        AccountModel account = DAOFactory.getAccountDAO().getByEmail(loginId);

        if (account == null) {
            account = new AccountModel();
            Date currentTime = Calendar.getInstance().getTime();
            account.setCreationTime(currentTime);
            account.setEmail(loginId.toLowerCase());
            account.setFirstName(user.getFirstName());
            account.setLastName(user.getLastName());
            account.setDescription(user.getDescription());
            account.setPassword("");
            account.setInitials("");
            account.setIp("");
            account.setInstitution("");
            account.setModificationTime(currentTime);
            DAOFactory.getAccountDAO().create(account);
        }
    }

    /**
     * Authenticate user to the ldap server.
     *
     * @param userName
     * @param passWord
     * @return valid email if successfully authenticated, null otherwise
     */
    private Account authenticateWithLDAP(String userName, String passWord) throws AuthenticationException {
        DirContext authContext = null;

        try {
            String filter = "(uid=" + userName + ")";
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchControls.setCountLimit(0);

            if (dirContext == null) {
                dirContext = getContext();
            }
            SearchResult searchResult = dirContext.search(properties.getLDAPQuery(), filter, searchControls).nextElement();
            Attributes attributes = searchResult.getAttributes();

            Account accountTransfer = new Account();

            if (attributes.get("givenName") != null) {
                String firstName = (String) attributes.get("givenName").get();
                accountTransfer.setFirstName(firstName);
            }

            if (attributes.get("sn") != null) {
                String lastName = (String) attributes.get("sn").get();
                accountTransfer.setLastName(lastName);
            }

            if (attributes.get("mail") != null) {
                String email = (String) attributes.get("mail").get();
                accountTransfer.setEmail(email.toLowerCase());
            }

            if (attributes.get("description") != null) {
                String description = (String) attributes.get("description").get();
                accountTransfer.setDescription(description);
            }

            authContext = getAuthenticatedContext(attributes, passWord);
            authContext.close();
            dirContext.close();
            return accountTransfer;
        } catch (NamingException e) {
            throw new AuthenticationException("Got LDAP NamingException", e);
        } finally {
            if (authContext != null) {
                try {
                    authContext.close();
                } catch (NamingException e) {
                    Logger.error(e);
                }
            }

            if (dirContext != null) {
                try {
                    dirContext.close();
                } catch (NamingException e) {
                    Logger.error(e);
                }
            }
        }
    }

    /**
     * Check if the user is in the settings file specified ldap group before authenticating.
     *
     * @param loginName
     * @return True if user is in the specified ldap group.
     */
    private boolean isLDAPUser(String loginName) throws AuthenticationException {
        boolean result = false;
        String whiteListString = properties.getWhiteListString();
        String[] whiteListArray;
        if (!StringUtils.isEmpty(whiteListString))
            whiteListArray = whiteListString.split(",");
        else
            whiteListArray = new String[]{};
        ArrayList<String> whitelistGroups = Arrays.stream(whiteListArray).distinct().map(String::trim)
                .collect(Collectors.toCollection(ArrayList::new));

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setCountLimit(0);
        try {
            if (dirContext == null) {
                dirContext = getContext();
            }

            // find user
            NamingEnumeration<SearchResult> userResults;
            String userQuery = MessageFormat.format(properties.getUserQuery(), loginName);
            userResults = dirContext.search(properties.getUserBaseDN(), userQuery, searchControls);
            if (userResults.hasMore()) {
                // find user groups
                NamingEnumeration<SearchResult> groupResults;
                SearchResult user = userResults.next();
                String query = MessageFormat.format(properties.getGroupQuery(), user.getNameInNamespace());
                groupResults = dirContext.search(properties.getGroupBaseDN(), query, searchControls);
                while (groupResults.hasMore()) {
                    String name = groupResults.next().getAttributes().get("cn").get().toString();
                    if (whitelistGroups.isEmpty() || whitelistGroups.contains(name)) {
                        result = true;
                    }
                }
                groupResults.close();
            }
            userResults.close();
        } catch (NamingException e) {
            throw new AuthenticationException("Error authenticating with LDAP", e);
        }

        String msg;
        if (result) {
            msg = loginName.toLowerCase() + " found in ldap directory.";
        } else {
            msg = loginName.toLowerCase() + " not found in ldap directory.";
        }

        Logger.info(msg);
        return result;
    }

    /**
     * Get unauthenticated ldap context.
     *
     * @return {@link DirContext} object.
     * @throws NamingException on exception initializing directory context
     */
    protected DirContext getContext() throws NamingException {
        Hashtable<String, String> env = getBaseContext();
        env.put(Context.PROVIDER_URL, properties.getSearchUrl());
        env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty(Context.SECURITY_AUTHENTICATION, "none"));
        return new InitialDirContext(env);
    }

    /**
     * Get authenticated context from the ldap server. Failure means bad user or password.
     *
     * @param password
     * @return {@link DirContext} object.
     * @throws NamingException
     */
    private DirContext getAuthenticatedContext(Attributes attributes, String password) throws NamingException {
        Hashtable<String, String> env = getBaseContext();
        env.put(Context.PROVIDER_URL, properties.getAuthenticationUrl());
        env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty(Context.SECURITY_AUTHENTICATION, "simple"));

        String securityPrincipal = properties.getUserBaseDN();

        String identifier = properties.getEmployeeIdentifier();
        if (!StringUtils.isEmpty(identifier)) {
            String employeeIdentifier = (String) attributes.get(identifier).get();
            securityPrincipal = (identifier + "=" + employeeIdentifier + "," + securityPrincipal);
        }

        env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
        env.put(Context.SECURITY_CREDENTIALS, password);

        return new InitialDirContext(env);
    }

    private Hashtable<String, String> getBaseContext() {
        Hashtable<String, String> environment = new Hashtable<>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, properties.getLdapContextFactory());
        environment.put("com.sun.jndi.ldap.connect.pool", properties.getProperty("com.sun.jndi.ldap.connect.pool", "true"));
        environment.put("com.sun.jndi.ldap.connect.pool.timeout", properties.getProperty("com.sun.jndi.ldap.connect.pool.timeout", "10000"));
        environment.put("com.sun.jndi.ldap.read.timeout", properties.getProperty("com.sun.jndi.ldap.read.timeout", "5000"));
        environment.put("com.sun.jndi.ldap.connect.timeout", properties.getProperty("com.sun.jndi.ldap.connect.timeout", "10000"));
        return environment;
    }
}
