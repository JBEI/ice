package org.jbei.ice.lib.message;

/**
 * Used to set the status of a message. Current supported status are
 * <code>INBOX</code> implies regular message
 * <code>TRASHED</code> implies message has been moved to trash. Messages deleted from the trash bin are permanently
 * deleted
 * <code>ARCHIVED</code> implies message has been archived.
 *
 * @author Hector Plahar
 */
public enum MessageStatus {

    INBOX,
    TRASHED,
    ARCHIVED
}
