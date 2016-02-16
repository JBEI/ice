package org.jbei.ice.lib.dto.web;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Status for remote partners
 *
 * @author Hector Plahar
 */
public enum RemotePartnerStatus implements IDataTransferModel {

    // information about the remote instance has been saved but contact did not succeed
    NOT_CONTACTED,

    // remote partner has been blocked from sending and receiving results from this registry
    BLOCKED,

    // partner approved to send and receive results from this registry
    APPROVED,

    // request to partner has been sent and awaiting response
    PENDING,

    // request received; pending admin approval
    PENDING_APPROVAL,

    // attempted contact failed
    CONTACT_FAILED
}
