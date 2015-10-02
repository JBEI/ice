package org.jbei.ice.lib.dto.sample;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Request status for user sample request
 *
 * @author Hector Plahar
 */
public enum SampleRequestStatus implements IDataTransferModel {

    IN_CART,    // request has been added to cart but not submitted. This is the initial default state

    PENDING,    // request has been submitted and is pending action from archivist

    FULFILLED,   // archivist has made sample available to user

    REJECTED    // rejected by the archivist. Sample will not be delivered
}
