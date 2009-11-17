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
 * l.initialize(String URL);
 * if (l.isWikiUser(String Username)) {
 * 		try {
 * 		l.authenticate(Username, password);
 * 		} except {
 * 		//auth failed
 * 		}		
 * }
 * 
 * @author tham
 *
 */
public class LblLdapAuth {
	//protected DirContext dirContext = null;
	protected String ldapURL =null; 
	protected boolean initialized = false;

	public boolean authenticated = false;
	public String givenName = null;
	public String sirName = null;
	public String eMail = null;
	public String org = null;
	public String description = null;
	
	public synchronized boolean authenticate(String userName, String passWord) throws NamingException, AuthenticationException {
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
		
		DirContext context = getContext();
		SearchResult searchResult = context.search(query, filter, cons).nextElement();
		context.close();
		Attributes attributes = searchResult.getAttributes();
		employeeNumber = (String) attributes.get("lblempnum").get();
		
		this.givenName = (String) attributes.get("givenName").get();
		this.sirName = (String) attributes.get("sn").get();
		this.eMail = (String) attributes.get("mail").get();
		this.eMail = this.eMail.toLowerCase();
		this.org = (String) attributes.get("o").get();
		this.description = (String) attributes.get("description").get();
		
		DirContext authContext = getAuthenticatedContext(employeeNumber, passWord);

		authContext.close();
		
		authenticated = true;
		return authenticated;
	}
	
	public void initialize(String serviceURL) throws NamingException {
		if (this.initialized == false) {
			this.ldapURL = serviceURL;
			this.initialized = true;
		} 	
	}	
	
	public synchronized boolean isWikiUser(String loginName) {
		boolean result = false;
		
		//Use "JBEI Employees" and "Keasling Lab" as whitelist
		String query = "cn=JBEI Employees,ou=JBEI-Groups,ou=Groups,o=Lawrence Berkeley Laboratory,c=US";
		String query2 = "cn=Keasling Lab,ou=JBEI-Groups,ou=Groups,o=Lawrence Berkeley Laboratory,c=US";
		String filter = "(objectClass=*)";
		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
		cons.setCountLimit(0);
		
		try {
			DirContext context = getContext();
			SearchResult searchResult = context.search(query, filter, cons).nextElement();
			NamingEnumeration<?> uniqueMembers = searchResult.getAttributes().get("uniquemember").getAll();
			
			SearchResult searchResult2 = context.search(query2, filter, cons).nextElement();
			NamingEnumeration<?> uniqueMembers2 = searchResult2.getAttributes().get("uniquemember").getAll();
			
			context.close();
			
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
			Logger.error("ldap auth failure: " + e.toString());
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
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put("com.sun.jndi.ldap.connect.pool", "false"); 
		env.put("com.sun.jndi.ldap.read.timeout", "2000");
		env.put("com.sun.jndi.ldap.connect.timeout", "2000");
		
		env.put(Context.PROVIDER_URL, this.ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, "none");
		return new InitialDirContext(env);
	}
	
	protected DirContext getAuthenticatedContext(String lblEmployeeNumber, String passWord) throws NamingException {
		
		String baseDN = "o=Lawrence Berkeley Laboratory,c=US";
				
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put("com.sun.jndi.ldap.connect.pool", "false"); 
		env.put("com.sun.jndi.ldap.read.timeout", "2000");
		env.put("com.sun.jndi.ldap.connect.timeout", "2000");
		
		env.put(Context.PROVIDER_URL, this.ldapURL);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "lblempnum=" + lblEmployeeNumber
				+ ",ou=People," + baseDN);
		env.put(Context.SECURITY_CREDENTIALS, passWord);
		return new InitialDirContext(env);
	}
	
	public static void main(String[] args) {
		
		LblLdapAuth l = new LblLdapAuth();
		try {
			l.initialize("ldaps://ldapauth.lbl.gov");
		} catch (NamingException e) {
			e.printStackTrace();
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
