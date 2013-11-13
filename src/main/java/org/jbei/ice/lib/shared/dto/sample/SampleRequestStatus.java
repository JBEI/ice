package org.jbei.ice.lib.shared.dto.sample;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Request status for user sample request
 *
 * @author Hector Plahar
 */
public enum SampleRequestStatus implements IDTOModel {

    IN_CART,    // request has been added to cart but not submitted. This is the initial default state

    PENDING,    // request has been submitted and is pending action from archivist

    FULFILLED   // archivist has made sample available to user
}
