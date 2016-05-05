package org.jbei.ice.lib.account.authentication;

import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Account;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;


/**
 * Authentication for LBL's LDAP system. Uses local authentication as a fallback
 * if the account does not exist with LBL LDAP directory
 *
 * @author Hector Plahar
 */
public class LblLdapAuthentication implements IAuthentication {

    protected DirContext dirContext;
    protected String searchURL;
    protected String authenticationURL;
    protected boolean initialized;

    public boolean authenticated;
    public String givenName;
    public String sirName;
    public String email;
    public String organization;
    public String description;

    public LblLdapAuthentication() {
        initialize();
        givenName = "";
        sirName = "";
        email = "";
        organization = "";
        description = "";
    }

    @Override
    public String authenticates(String loginId, String password) throws AuthenticationException {
        if (loginId == null || password == null || loginId.isEmpty() || password.isEmpty()) {
            throw new AuthenticationException("Username and Password are mandatory!");
        }

        loginId = loginId.toLowerCase().trim();
        String authenticatedEmail;

        if (isWikiUser(loginId)) {
            try {
                authenticatedEmail = authenticateWithLDAP(loginId, password);
                if (authenticatedEmail == null) {
                    return null;
                }
            } catch (AuthenticationException ae) {
                return null;
            }

            Account account = checkCreateAccount(authenticatedEmail);
            if (account == null)
                return null;
            return account.getEmail();
        } else {
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
     *
     * @param loginId unique login identifier
     */
    private Account checkCreateAccount(String loginId) throws AuthenticationException {
        AccountController retriever = new AccountController();
        Account account = retriever.getByEmail(loginId);

        if (account == null) {
            account = new Account();
            Date currentTime = Calendar.getInstance().getTime();
            account.setCreationTime(currentTime);
            account.setEmail(getEmail().toLowerCase());
            account.setFirstName(getGivenName());
            account.setLastName(getSirName());
            account.setDescription(getDescription());
            account.setPassword("");
            account.setInitials("");
            account.setIp("");
            account.setInstitution("Lawrence Berkeley Laboratory");
            account.setModificationTime(currentTime);
            account = DAOFactory.getAccountDAO().create(account);
        }

        return account;
    }

    /**
     * Authenticate user to the ldap server.
     *
     * @param userName
     * @param passWord
     * @return valid email if successfully authenticated, null otherwise
     */
    public String authenticateWithLDAP(String userName, String passWord) throws AuthenticationException {
        DirContext authContext = null;

        try {
            authenticated = false;
            String employeeNumber = "";

            //has to look up employee number for binding
            int idx = userName.indexOf("@lbl.gov");
            if (idx > 0)
                userName = userName.substring(0, idx);
            String filter = "(uid=" + userName + ")";
            SearchControls cons = new SearchControls();
            cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            cons.setCountLimit(0);

            if (dirContext == null) {
                dirContext = getContext();
            }
            String LDAP_QUERY = "dc=lbl,dc=gov";
            SearchResult searchResult = dirContext.search(LDAP_QUERY, filter, cons).nextElement();

            Attributes attributes = searchResult.getAttributes();
            employeeNumber = (String) attributes.get("lblempnum").get();

            if (attributes.get("givenName") != null) {
                givenName = (String) attributes.get("givenName").get();
            }
            if (attributes.get("sn") != null) {
                sirName = (String) attributes.get("sn").get();
            }
            if (attributes.get("mail") != null) {
                email = (String) attributes.get("mail").get();
            }
            email = email.toLowerCase();
            organization = "Lawrence Berkeley Laboratory";
            if (attributes.get("description") != null) {
                description = (String) attributes.get("description").get();
            }
            authContext = getAuthenticatedContext(employeeNumber, passWord);

            authContext.close();
            dirContext.close(); //because authentication should be the last step
        } catch (NamingException e) {
            throw new AuthenticationException("Got LDAP NamingException", e);
        } finally {
            if (authContext != null) {
                try {
                    authContext.close();
                } catch (NamingException e) {
                    throw new AuthenticationException("Got LDAP NamingException", e);
                }
            }
            try {
                dirContext.close();
            } catch (NamingException e) {
                throw new AuthenticationException("Got LDAP NamingException", e);
            }
        }

        return email;
    }

    /**
     * Check if the user is in the settings file specified ldap group before authenticating.
     *
     * @param loginName
     * @return True if user is in the specified ldap group.
     */
    public boolean isWikiUser(String loginName) throws AuthenticationException {
        boolean result = false;
        ArrayList<String> whitelistGroups = new ArrayList<String>();
        String whitelistString = "JBEI,Keasling Lab,DNA DIVA, BIOFAB, Mukhopadhyay GTL, Cell Wall Synthesis";
        String[] whiteListArray = whitelistString.split(",");
        for (String element : whiteListArray) {
            whitelistGroups.add(element);
        }

        String groupDn = "ou=JBEI-Groups,ou=Groups,dc=lbl,dc=gov";
        String groupQueryString = "(&(objectClass=groupOfUniqueNames)(uniqueMember={0}))";
        String userDn = "ou=People,dc=lbl,dc=gov";
        String userQueryString = "(&(objectclass=lblPerson)(uid={0})(lblAccountStatus=Active))";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setCountLimit(0);
        try {
            if (dirContext == null) {
                dirContext = getContext();
            }

            // find user
            NamingEnumeration<SearchResult> userResults;
            int idx = loginName.indexOf("@lbl.gov");
            String queryName;
            if (idx > 0)
                queryName = loginName.substring(0, idx);
            else
                queryName = loginName;

            String userQuery = MessageFormat.format(userQueryString, queryName);
            userResults = dirContext.search(userDn, userQuery, searchControls);
            if (userResults.hasMore()) {
                // find user groups
                NamingEnumeration<SearchResult> groupResults;
                SearchResult user = userResults.next();
                String query = MessageFormat.format(groupQueryString, user.getNameInNamespace());
                groupResults = dirContext.search(groupDn, query, searchControls);
                while (groupResults.hasMore()) {
                    String name = groupResults.next().getAttributes().get("cn").get().toString();
                    if (whitelistGroups.contains(name)) {
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
            msg = loginName.toLowerCase() + " is in wiki.";
        } else {
            msg = loginName.toLowerCase() + " is not in wiki.";
        }

        Logger.info(msg);
        return result;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSirName() {
        return sirName;
    }

    public String getEmail() {
        return email;
    }

    public String getOrganization() {
        return organization;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get unauthenticated ldap context.
     *
     * @return {@link javax.naming.directory.DirContext} object.
     * @throws javax.naming.NamingException
     */
    protected DirContext getContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "10000");

        env.put("com.sun.jndi.ldap.read.timeout", "5000");
        env.put("com.sun.jndi.ldap.connect.timeout", "10000");

        env.put(Context.PROVIDER_URL, searchURL);
        env.put(Context.SECURITY_AUTHENTICATION, "none");

        InitialDirContext result = null;
        try {
            result = new InitialDirContext(env);
        } catch (NamingException e) {
            e.printStackTrace();
            throw e;
        }

        return result;
    }

    /**
     * Get authenticated context from the ldap server. Failure means bad user or password.
     *
     * @param lblEmployeeNumber
     * @param passWord
     * @return {@link javax.naming.directory.DirContext} object.
     * @throws javax.naming.NamingException
     */
    protected DirContext getAuthenticatedContext(String lblEmployeeNumber, String passWord) throws NamingException {
        String baseDN = "dc=lbl,dc=gov";
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "10000");

        env.put("com.sun.jndi.ldap.read.timeout", "5000");
        env.put("com.sun.jndi.ldap.connect.timeout", "10000");

        env.put(Context.PROVIDER_URL, authenticationURL);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "lblempnum=" + lblEmployeeNumber + ",ou=People," + baseDN);
        env.put(Context.SECURITY_CREDENTIALS, passWord);

        InitialDirContext result = new InitialDirContext(env);

        return result;
    }

    private void initialize() {
        if (!initialized) {
            searchURL = "ldap://identity.lbl.gov";
            authenticationURL = "ldaps://identity.lbl.gov:636";
            initialized = true;
        }
    }
}
