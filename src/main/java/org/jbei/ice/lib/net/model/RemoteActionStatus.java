package org.jbei.ice.lib.net.model;

/**
 * Action status for requests to partner with other sites in web of registries
 * configuration
 *
 * @author Hector Plahar
 */
public enum RemoteActionStatus {

    // request to partner has been sent
    SUBMITTED,

    // submitted request to partner was denied by the "other side"
    SUBMIT_REQUEST_DENIED,

    // request to partner has been received and pending action on this side
    PENDING,

    // received request to partner was denied
    DENIED,

    // received request to partner was approved
    APPROVED,

    // submitted request to partner was approved on "the other side"
    SUBMIT_REQUEST_APPROVED;
}
