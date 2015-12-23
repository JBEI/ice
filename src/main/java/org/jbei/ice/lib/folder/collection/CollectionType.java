package org.jbei.ice.lib.folder.collection;

/**
 * Types of collections that are supported in ICE.
 * The entries contained in them are based on the status of the entry of the permissions
 * that the specified user has
 *
 * @author Hector Plahar
 */
public enum CollectionType {
    FEATURED,
    AVAILABLE,
    PERSONAL,
    SHARED,
    DRAFTS,
    PENDING,
    DELETED,
    TRANSFERRED
}
