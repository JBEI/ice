package org.jbei.ice.lib.shared.dto.web;

import java.util.ArrayList;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Wrapper around web of registries settings and partners
 *
 * @author Hector Plahar
 */
public class WebOfRegistries implements IDTOModel {

    private boolean webEnabled;
    private ArrayList<RegistryPartner> partners;

    public WebOfRegistries() {
        partners = new ArrayList<RegistryPartner>();
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
}
