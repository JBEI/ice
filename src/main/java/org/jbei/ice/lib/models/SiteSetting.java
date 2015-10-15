package org.jbei.ice.lib.models;

import org.jbei.ice.lib.dao.IDataTransferModel;

public class SiteSetting implements IDataTransferModel {
    public String logo = "rest/asset/logo.png";
    public String loginMessage = "rest/asset/institution.html";
    public String footer = "rest/asset/footer.html";
    public String version = "4.5.2-beta";
}
