package org.jbei.ice.services.webservices;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;

/**
 * @author Hector Plahar
 */
@WebServiceClient(name = "RegistryAPIServiceClient", targetNamespace = "https://api.registry.jbei.org/")
public class RegistryAPIServiceClient {

    private static final QName SERVICE_NAME = new QName("https://api.registry.jbei.org/", "RegistryAPIService");
    private static final QName PORT_NAME = new QName("https://api.registry.jbei.org/", "RegistryAPIPort");
//    public final static URL WSDL_LOCATION;

    private RegistryAPIServiceClient() {
    }

    /*
    static {
        URL url = RegistryAPIService.class.getResource
        ("/home/hplahar/Dev/Projects/RegistryDesktop/src/org/jbei/ice/service/RegistryAPI1.wsdl");
        if (url == null) {
            java.util.logging.Logger.getLogger(RegistryAPIService.class.getName())
                .log(java.util.logging.Level.INFO,
                     "Can not initialize the default wsdl from {0}",
                     "/home/hplahar/Dev/Projects/RegistryDesktop/src/org/jbei/ice/service/RegistryAPI1.wsdl");
        }
        WSDL_LOCATION = url;
    }
     */

    public static void main(String args[]) throws Exception {

        URL url = new URL("https://registry.jbei.org/api/RegistryAPI?wsdl");
        Service service = Service.create(url, SERVICE_NAME);

        // using endpoint Address
        // String endpointAddress = "https://registry.jbei.org/api/RegistryAPI";
        // Add a port to the Service
        // service.addPort(PORT_NAME, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);

        IRegistryAPI hw = service.getPort(PORT_NAME, IRegistryAPI.class);
        String sessionId = hw.login("scanner", "SuperCaliFragilistic");
        System.out.println(sessionId);
    }
}
