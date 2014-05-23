package org.jbei.ice.lib.account.authentication;

/**
 * Backend for authentication using the database. This is the default backend.
 *
 * @author Hector Plahar
 */
public class LocalAuthentication implements IAuthentication {

    public LocalAuthentication() {
    }

    @Override
    public boolean authenticates(String userId, String password) throws AuthenticationException {
        return false;
//        AccountRetriever retriever = new AccountRetriever();
//        boolean accountExists = retriever.accountExists(userId);
//
//        if (!accountExists)
//            throw new AuthenticationException("Cannot authenticate against a non-existent account");
//
//        Authenticator authenticator = new Authenticator();
//        return authenticator.isValidPassword(userId, password);
    }
}
