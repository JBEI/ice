package org.jbei.ice.services.webservices;

import java.util.HashSet;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.soap.SOAPBinding;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.config.ConfigurationController;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.shared.dto.ConfigurationKey;

/**
 * @author Hector Plahar
 */
@WebServiceClient(name = "RegistryAPIServiceClient", targetNamespace = "https://api.registry.jbei.org/")
public class RegistryAPIServiceClient {

    private static final QName SERVICE_NAME = new QName("https://api.registry.jbei.org/", "RegistryAPIService");
    private static final String QNAME_LOCAL_PART = "RegistryAPIPort";
    private final Service service;
    private static final RegistryAPIServiceClient INSTANCE = new RegistryAPIServiceClient();
    private static final HashSet<String> namespaceURIs = new HashSet<>();

    public static RegistryAPIServiceClient getInstance() {
        return INSTANCE;
    }

    private RegistryAPIServiceClient() {
        Logger.info("Creating service client for " + SERVICE_NAME);
        service = Service.create(SERVICE_NAME);
    }

    public void addPortName(String namespaceURI) {
        if (namespaceURIs.contains(namespaceURI))
            return;

        namespaceURIs.add(namespaceURI);
        QName name = new QName(namespaceURI, QNAME_LOCAL_PART);
        service.addPort(name, SOAPBinding.SOAP11HTTP_BINDING, "https://" + namespaceURI + "/api/RegistryAPI");
    }

    public IRegistryAPI getAPIPortForURL(String namespaceURI) {
        addPortName(namespaceURI);
        Iterator<QName> iter = service.getPorts();
        while (iter.hasNext()) {
            QName name = iter.next();
            if (name.getNamespaceURI().equals(namespaceURI)) {
                return service.getPort(name, IRegistryAPI.class);
            }
        }
        return null;
    }

    public void removePortName(String namespaceURI) {
        Iterator<QName> iter = service.getPorts();
        while (iter.hasNext()) {
            QName name = iter.next();
            if (name.getNamespaceURI().equals(namespaceURI)) {
                iter.remove();
                namespaceURIs.remove(namespaceURI);
                return;
            }
        }
    }

    // TODO : this needs to be handled better
    public Service getService() {
        String value = null;
        try {
            value = new ConfigurationController().getPropertyValue(ConfigurationKey.WEB_PARTNERS);
        } catch (ControllerException e) {
            Logger.warn(e.getMessage());
        }

        if (value != null) {
            for (String split : value.split(";"))
                addPortName(split);
        }

        return this.service;
    }
}
