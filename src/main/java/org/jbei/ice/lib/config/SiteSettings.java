package org.jbei.ice.lib.config;

import org.jbei.ice.storage.IDataTransferModel;

/**
 * Settings for the site
 *
 * @author Hector Plahar
 */
public class SiteSettings implements IDataTransferModel {

    private String version = "5.2.1";
    private String assetName;
    private boolean hasLogo;
    private boolean hasLoginMessage;
    private boolean hasFooter;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isHasLogo() {
        return hasLogo;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public void setHasLogo(boolean hasLogo) {
        this.hasLogo = hasLogo;
    }

    public boolean isHasLoginMessage() {
        return hasLoginMessage;
    }

    public void setHasLoginMessage(boolean hasLoginMessage) {
        this.hasLoginMessage = hasLoginMessage;
    }

    public boolean isHasFooter() {
        return hasFooter;
    }

    public void setHasFooter(boolean hasFooter) {
        this.hasFooter = hasFooter;
    }
}
