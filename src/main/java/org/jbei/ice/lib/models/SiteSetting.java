package org.jbei.ice.lib.models;

import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;

public class SiteSetting implements IDataTransferModel {
//    public String logo = "rest/asset/logo.png";
//    public String loginMessage = "rest/asset/institution.html";
//    public String footer = "rest/asset/footer.html";
    public String logo;
    public String loginMessage;
    public String footer;
    public String version = "4.5.2-beta";

    public SiteSetting(){
        logo = Utils.getConfigValue(ConfigurationKey.LOGO);
        loginMessage = Utils.getConfigValue(ConfigurationKey.LOGIN_MESSAGE);
        footer = Utils.getConfigValue(ConfigurationKey.FOOTER);
    }
}
