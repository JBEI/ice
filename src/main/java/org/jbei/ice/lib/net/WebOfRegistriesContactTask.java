package org.jbei.ice.lib.net;

import java.util.ArrayList;

import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.executor.Task;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;
import org.jbei.ice.services.webservices.IRegistryAPI;
import org.jbei.ice.services.webservices.RegistryAPIServiceClient;
import org.jbei.ice.services.webservices.ServiceException;

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

        // it is expected that all partners in this task should already be stored
        WoRController controller = ControllerFactory.getWebController();
        for (RegistryPartner partner : partners) {
            String url = partner.getUrl();
            String name = partner.getName();
            IRegistryAPI api = RegistryAPIServiceClient.getInstance().getAPIPortForURL(url);
            if (api == null)
                continue;

            try {
                String token = controller.getAuthenticationKey(url);
                if (token == null) {
                    Logger.error("Registry partner " + url + " not recognized. Skipping");
                    continue;
                }

                // request api key
                String apiKey = null;
                try {
                    apiKey = api.requestAPIKey(url, name, token);
                } catch (ServiceException e) {
                    Logger.error(e);
                }

                if (apiKey == null) {
                    Logger.error("Registry partner " + url + " responded with null api key");
                    continue;
                }

                // save the api key
                controller.setApiKeyForPartner(url, apiKey);
            } catch (ControllerException ce) {
                Logger.error(ce);
            }
        }
    }
}
