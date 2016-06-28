package org.jbei.ice.lib.email;

/**
 * Parent class for custom Email types used to send emails
 *
 * @author Hector Plahar
 */
public abstract class Email {

    public abstract boolean send(String email, String subject, String body);

    public abstract void sendError(String subject, String body);
}
