package org.jbei.ice.lib.dto.web;

import org.jbei.ice.storage.IDataTransferModel;

import java.util.ArrayList;

/**
 * Wrapper around web of registries settings and partners
 *
 * @author Hector Plahar
 */
public class WebOfRegistries implements IDataTransferModel {

    private boolean webEnabled;
    private ArrayList<RegistryPartner> partners;

    public WebOfRegistries() {
    }

    public boolean isWebEnabled() {
        return webEnabled;
    }

    public void setWebEnabled(boolean webEnabled) {
        this.webEnabled = webEnabled;
    }

    public ArrayList<RegistryPartner> getPartners() {
        return partners;
    }

    public void setPartners(ArrayList<RegistryPartner> list) {
        if (this.partners == null)
            partners = new ArrayList<>();

        this.partners.clear();
        this.partners.addAll(list);
    }
}
