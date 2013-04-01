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


    public static void main(String args[]) {

//        InputStream stream = RegistryAPIServiceClient.class.getClassLoader().getResourceAsStream
// ("generated/wsdl/RegistryAPI.wsdl");
//        System.out.println(stream != null);
//
//        if( true)
//            return;

//        URL url = new URL("https://registry.jbei.org/api/RegistryAPI?wsdl");
//        Service service = Service.create(url, SERVICE_NAME);
//        long start = System.currentTimeMillis();
//        Service service = Service.create(SERVICE_NAME);
//        String endpointAddress = "https://registry.jbei.org/api/RegistryAPI";
//
//        service.addPort(PORT_NAME_2, SOAPBinding.SOAP11HTTP_BINDING,
//                        "https://public-registry.jbei.org/api/RegistryAPI");
//        service.addPort(PORT_NAME_1, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
//        System.out.println(System.currentTimeMillis() - start + "ms for adding two ports");
//
//        // using endpoint Address
//        // String endpointAddress = "https://registry.jbei.org/api/RegistryAPI";
//        // Add a port to the Service
//        // service.addPort(PORT_NAME, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
//
//        Iterator<QName> ports = service.getPorts();
//        while (ports.hasNext()) {
//            try {
//                QName name = ports.next();
//                start = System.currentTimeMillis();
//                IRegistryAPI hw = service.getPort(name, IRegistryAPI.class);
//                System.out.println(System.currentTimeMillis() - start + "ms for getting a port");
//                String sessionId = hw.login("scanner", "SuperCaliFragilistic");
//
//                start = System.currentTimeMillis();
//                ArrayList<BlastResult> result
//                        = hw.blastn(sessionId, "CTGGTCTTAGAAATTCAACAAATGATCAAATTGAAATTGAAGGTATTGAAATGGCAAAAG");
//                System.out.println(System.currentTimeMillis() - start + "ms for blast call");
//                System.out.println(result.size());
//            } catch (Throwable ye) {
//                System.err.println(ye.getMessage());
//            }
//        }
//
    }
}
