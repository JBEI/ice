package org.jbei.ice.lib.account.authentication.ldap;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.authentication.AuthenticationException;
import org.jbei.ice.lib.account.authentication.IAuthentication;
import org.jbei.ice.lib.account.authentication.LocalAuthentication;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.model.Account;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Authentication for LBL's LDAP system. Uses local authentication as a fallback
 * if the account does not exist with LBL LDAP directory
 *
 * @author Hector Plahar
 */
public class LdapAuthentication implements IAuthentication {

    private DirContext dirContext;
    private LdapProperties properties;

    public LdapAuthentication() throws IOException {
        properties = new LdapProperties();
        properties.load();
    }

    @Override
    public AccountTransfer authenticates(String loginId, String password, String ip) throws AuthenticationException {
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
                AccountTransfer accountTransfer = authenticateWithLDAP(loginId, password);
                AccountDAO dao = DAOFactory.getAccountDAO();

                // check if there is an account available to the existing user
                Account account = dao.getByEmail(accountTransfer.getEmail());
                if (account == null) {
                    account = createNewAccountObject(accountTransfer);
                    account.setIp(ip);
                    account = dao.create(account);
                }

                return account.toDataTransferObject();
            } catch (AuthenticationException ae) {
                return null;
            }
        } else {
            // backup local authentication
            LocalAuthentication localBackend = new LocalAuthentication();
            return localBackend.authenticates(loginId, password, ip);
        }
    }

    private Account createNewAccountObject(AccountTransfer accountTransfer) {
        Account account = new Account();
        Date currentTime = Calendar.getInstance().getTime();
        account.setCreationTime(currentTime);
        account.setEmail(accountTransfer.getEmail().toLowerCase());
        account.setFirstName(accountTransfer.getFirstName());
        account.setLastName(accountTransfer.getLastName());
        account.setDescription(accountTransfer.getDescription());
        account.setInstitution(accountTransfer.getInstitution());
        account.setModificationTime(currentTime);
        return account;
    }

    /**
     * Authenticate user to the ldap server.
     *
     * @param userName
     * @param passWord
     * @return valid email if successfully authenticated, null otherwise
     */
    private AccountTransfer authenticateWithLDAP(String userName, String passWord) throws AuthenticationException {
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

            AccountTransfer accountTransfer = new AccountTransfer();

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

            accountTransfer.setInstitution(properties.getOrganization());

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
        ArrayList<String> whitelistGroups = new ArrayList<>(Arrays.asList(whiteListArray));

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
     * @return {@link javax.naming.directory.DirContext} object.
     * @throws javax.naming.NamingException on exception initializing directory context
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
     * @return {@link javax.naming.directory.DirContext} object.
     * @throws javax.naming.NamingException
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
