import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.predicate.PredicatesHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.builder.PredicatedHandlersParser;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jbei.ice.servlet.IceServletContextListener;

import java.io.File;

/**
 * Embedded (Undertow) server for development. Uses the settings from <code>web.xml</code>
 * todo : parse web.xml file
 *
 * @author Hector Plahar
 */
public class DevelopmentServer {

    public static void main(String[] args) throws Exception {
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(ClassLoader.getSystemClassLoader())
                .addListener(Servlets.listener(IceServletContextListener.class))
                .setContextPath("/")
                .setDeploymentName("Inventory for Composable Elements")
                .setResourceManager(new FileResourceManager(new File("src/main/webapp"), 10)).addWelcomePage("index.htm")
                .addServlets(
                        Servlets.servlet("Jersey REST Servlet", ServletContainer.class)
                                .addInitParam("jersey.config.server.provider.packages", "org.jbei.ice.services.rest")
                                .addInitParam("jersey.config.server.provider.scanning.recursive", "false")
                                .addInitParam("javax.ws.rs.Application", "org.jbei.ice.services.rest.multipart.IceApplication")
                                .setAsyncSupported(true)
                                .setEnabled(true)
                                .addMapping("/rest/*")
                );

        // deploy servlet
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        HttpHandler servletHandler = manager.start();

        PredicatesHandler handler = Handlers.predicates(PredicatedHandlersParser.parse(
                "path-prefix('folders') or path-prefix('entry') or path-prefix('admin') and regex('/.+') -> rewrite('/')",
                ClassLoader.getSystemClassLoader()), servletHandler);

        PathHandler path = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", handler);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(path)
                .build();
        server.start();
    }
}