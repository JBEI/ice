package org.jbei.ice.lib.dto.web;

import org.jbei.ice.lib.dao.IDataTransferModel;

/**
 * Status for remote partners
 *
 * @author Hector Plahar
 */
public enum RemotePartnerStatus implements IDataTransferModel {

    // remote partner has been blocked from sending and receiving results from this registry
    BLOCKED("Blocked"),

    // partner approved to send and receive results from this registry
    APPROVED("Approved");

    private String display;

    RemotePartnerStatus() {
    }

    RemotePartnerStatus(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}
