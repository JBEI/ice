package org.jbei.ice.lib.net;

import java.util.ArrayList;

import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.utils.Utils;

/**
 * Task to contact other registry instances that are in the web of registries
 * config to request authentication keys and to send the same to them
 *
 * @author Hector Plahar
 */
public class WebOfRegistriesContactTask extends Task {

    private final ArrayList<RegistryPartner> partners;

    public WebOfRegistriesContactTask(ArrayList<RegistryPartner> partners) {
        this.partners = partners;
    }

    @Override
    public void execute() {
        if (partners == null || partners.isEmpty())
            return;

        String myUrl = Utils.getConfigValue(ConfigurationKey.URI_PREFIX);

        // it is expected that all partners in this task should already be stored but missing an api key
        WoRController controller = new WoRController();
        // TODO
//        for (RegistryPartner partner : partners) {
//            IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(partner.getUrl());
//            if (api == null)
//                continue;
//
//            try {
//                String token = controller.getAuthenticationKey(partner.getUrl());
//                if (token == null) {
//                    Logger.error("Registry partner " + partner.getUrl() + " not recognized. Skipping");
//                    continue;
//                }
//
//                // request api key
//                String apiKey;
//                try {
//                    apiKey = api.requestAPIKey(myUrl, partner.getName(), token);
//                    if (apiKey == null) {
//                        Logger.error("Registry partner " + partner.getUrl() + " responded with null api key");
//                        continue;
//                    }
//                } catch (Throwable e) {
//                    Logger.warn("Could not obtain API KEY for server " + partner.getUrl() + ": " + e.getMessage());
//                    continue;
//                }
//
//                // save the api key
//                controller.setApiKeyForPartner(partner.getUrl(), apiKey);
//            } catch (ControllerException ce) {
//                Logger.error(ce);
//            }
//        }
    }
}
