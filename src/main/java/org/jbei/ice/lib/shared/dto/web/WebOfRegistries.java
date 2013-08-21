package org.jbei.ice.lib.shared.dto.web;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Wrapper around web of registries settings and partners
 *
 * @author Hector Plahar
 */
public class WebOfRegistries implements IDTOModel {

    private static final long serialVersionUID = 1l;

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
            partners = new ArrayList<RegistryPartner>();

        this.partners.clear();
        this.partners.addAll(list);
    }
}
