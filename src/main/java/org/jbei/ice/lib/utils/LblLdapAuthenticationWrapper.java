package org.jbei.ice.lib.utils;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Little wrapper for lbl ldap.
 * 
 * Simple usage:
 * LdapAuthl = new LdapAuth();
 * if (l.isWikiUser(String Username)) {
 * try {
 * l.authenticate(Username, password);
 * } except {
 * //auth failed
 * }
 * }
 * 
 * @author tham
 * 
 */
public class LblLdapAuthenticationWrapper {
    private final String LDAP_SEARCH_URL = "ldap://ldap.lbl.gov";
    private final String LDAP_AUTHENTICATION_URL = "ldaps://ldapauth.lbl.gov:636";
    private final String LBL_QUERY = "o=Lawrence Berkeley Laboratory,c=US";

    protected DirContext dirContext = null;
    protected String searchURL = null;
    protected String authenticationURL = null;
    protected boolean initialized = false;

    public boolean authenticated = false;
    public String givenName = null;
    public String sirName = null;
    public String eMail = null;
    public String org = null;
    public String description = null;

    public LblLdapAuthenticationWrapper() {
        initialize();
    }

    public boolean authenticate(String userName, String passWord)
            throws LblLdapAuthenticationWrapperException {
        DirContext authContext = null;

        try {
            authenticated = false;

            String employeeNumber = "";

            //has to look up employee number for binding
            String query = LBL_QUERY;
            String filter = "(uid=" + userName + ")";
            SearchControls cons = new SearchControls();
            cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            cons.setCountLimit(0);

            if (dirContext == null) {
                dirContext = getContext();
            }
            SearchResult searchResult = dirContext.search(query, filter, cons).nextElement();

            Attributes attributes = searchResult.getAttributes();
            employeeNumber = (String) attributes.get("lblempnum").get();

            this.givenName = (String) attributes.get("givenName").get();
            this.sirName = (String) attributes.get("sn").get();
            this.eMail = (String) attributes.get("mail").get();
            this.eMail = this.eMail.toLowerCase();
            this.org = "Lawrence Berkeley Laboratory";
            this.description = (String) attributes.get("description").get();

            authContext = getAuthenticatedContext(employeeNumber, passWord);

            authContext.close();
            dirContext.close(); //because authentication should be the last step
            authenticated = true;
        } catch (NamingException e) {
            throw new LblLdapAuthenticationWrapperException("Got LDAP NamingException", e);
        } finally {
            if (authContext != null) {
                try {
                    authContext.close();
                } catch (NamingException e) {
                    throw new LblLdapAuthenticationWrapperException("Got LDAP NamingException", e);
                }
            }
            try {
                dirContext.close();
            } catch (NamingException e) {
                throw new LblLdapAuthenticationWrapperException("Got LDAP NamingException", e);
            }
        }

        return authenticated;
    }

    public boolean isWikiUser(String loginName) throws LblLdapAuthenticationWrapperException {
        boolean result = false;

        //Use "JBEI Employees" and "Keasling Lab" as whitelist
        String query = "cn=JBEI Employees,ou=JBEI-Groups,ou=Groups,o=Lawrence Berkeley Laboratory,c=US";
        String query2 = "cn=Keasling Lab,ou=JBEI-Groups,ou=Groups,o=Lawrence Berkeley Laboratory,c=US";
        String filter = "(objectClass=*)";
        SearchControls cons = new SearchControls();
        cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
        cons.setCountLimit(0);

        try {
            if (dirContext == null) {
                dirContext = getContext();
            }

            SearchResult searchResult = dirContext.search(query, filter, cons).nextElement();
            NamingEnumeration<?> uniqueMembers = searchResult.getAttributes().get("uniquemember")
                    .getAll();

            SearchResult searchResult2 = dirContext.search(query2, filter, cons).nextElement();
            NamingEnumeration<?> uniqueMembers2 = searchResult2.getAttributes().get("uniquemember")
                    .getAll();

            ArrayList<String> whiteList = new ArrayList<String>();
            while (uniqueMembers.hasMore()) {
                String temp = (String) uniqueMembers.next();

                whiteList.add(temp.toLowerCase());
            }

            while (uniqueMembers2.hasMore()) {
                String temp = (String) uniqueMembers2.next();

                whiteList.add(temp.toLowerCase());
            }

            if (whiteList.contains(loginName.toLowerCase())) {
                result = true;
            }

            uniqueMembers.close();
            uniqueMembers2.close();

        } catch (NamingException e) {
            throw new LblLdapAuthenticationWrapperException(
                    "Failed to fetch wiki ldap users whitelist!", e);
        }

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

    public String geteMail() {
        return eMail;
    }

    public String getOrg() {
        return org;
    }

    public String getDescription() {
        return description;
    }

    protected DirContext getContext() throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "10000");

        env.put("com.sun.jndi.ldap.read.timeout", "500");
        env.put("com.sun.jndi.ldap.connect.timeout", "10000");

        env.put(Context.PROVIDER_URL, this.searchURL);
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

    protected DirContext getAuthenticatedContext(String lblEmployeeNumber, String passWord)
            throws NamingException {

        String baseDN = "o=Lawrence Berkeley Laboratory,c=US";

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put("com.sun.jndi.ldap.connect.pool", "true");
        env.put("com.sun.jndi.ldap.connect.pool.timeout", "1000");

        env.put("com.sun.jndi.ldap.read.timeout", "3000");
        env.put("com.sun.jndi.ldap.connect.timeout", "1000");

        env.put(Context.PROVIDER_URL, this.authenticationURL);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "lblempnum=" + lblEmployeeNumber + ",ou=People,"
                + baseDN);
        env.put(Context.SECURITY_CREDENTIALS, passWord);

        InitialDirContext result = null;
        try {
            result = new InitialDirContext(env);
        } catch (NamingException e) {
            e.printStackTrace();
            throw e;
        }
        return result;
    }

    private void initialize() {
        if (this.initialized == false) {
            this.searchURL = LDAP_SEARCH_URL;
            this.authenticationURL = LDAP_AUTHENTICATION_URL;
            this.initialized = true;
        }
    }

    public class LblLdapAuthenticationWrapperException extends Exception {
        private static final long serialVersionUID = 1L;

        public LblLdapAuthenticationWrapperException() {
            super();
        }

        public LblLdapAuthenticationWrapperException(String message, Throwable cause) {
            super(message, cause);
        }

        public LblLdapAuthenticationWrapperException(String message) {
            super(message);
        }

        public LblLdapAuthenticationWrapperException(Throwable cause) {
            super(cause);
        }
    }
}
