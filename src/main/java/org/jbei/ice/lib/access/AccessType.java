package org.jbei.ice.lib.access;

/**
 * Used with the access permission classes to indicate the type of access
 * that is being granted.
 *
 * Options are read, write and both; with write access type also conferring read access.
 * When access type of "BOTH" is conferred, the user is able to remove write access
 * without removing read access.
 *
 * @author Hector Plahar
 */
public enum AccessType {

    READ,
    WRITE,
    BOTH
}
