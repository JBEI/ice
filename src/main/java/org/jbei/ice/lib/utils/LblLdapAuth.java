package org.jbei.ice.lib.utils;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.jbei.ice.lib.logging.Logger;

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
public class LblLdapAuth {
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

    public LblLdapAuth() throws NamingException {
        super();
        /*
         * this is a workaround for some strange bug where repeated searches to
         * ldapauth.lbl.gov results in readReply timing out. Doing it this way doesn't 
         * seem to trigger it.
         */

        this.initialize("ldap://ldap.lbl.gov", "ldaps://ldapauth.lbl.gov:636");
    }

    public boolean authenticate(String userName, String passWord) throws NamingException,
            AuthenticationException {
        DirContext authContext = null;
        try {

            authenticated = false;
            String employeeNumber = "";

            if (passWord.length() == 0) {
                throw new AuthenticationException("No password received.");
            }

            //has to look up employee number for binding
            String query = "o=Lawrence Berkeley Laboratory,c=US";
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
            this.org = (String) attributes.get("o").get();
            this.description = (String) attributes.get("description").get();

            authContext = getAuthenticatedContext(employeeNumber, passWord);

            authContext.close();
            dirContext.close(); //because authentication should be the last step
            authenticated = true;

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (authContext != null) {
                    authContext.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                dirContext.close(); //because authentication should be the last step
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return authenticated;
    }

    public void initialize(String searchURL, String authenticationURL) throws NamingException {
        if (this.initialized == false) {
            this.searchURL = searchURL;
            this.authenticationURL = authenticationURL;
            this.initialized = true;
        }
    }

    public boolean isWikiUser(String loginName) {
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

            e.printStackTrace();
            Logger.error("ldap whitelist retrieval failure: " + e.toString());
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

    public static void main(String[] args) {

        LblLdapAuth l = null;
        try {
            l = new LblLdapAuth();
        } catch (NamingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        boolean result = l.isWikiUser("tsham");
        System.out.println(result);

        try {
            l.authenticate("tsham", "");
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

}
