package org.jbei.ice.services.rest.multipart;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.jbei.ice.services.rest.AuthenticationInterceptor;
import org.jbei.ice.services.rest.FileResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Hector Plahar
 */
public class IceApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        // register resources and features
        classes.add(MultiPartFeature.class);
        classes.add(FileResource.class);
        classes.add(AuthenticationInterceptor.class);
        return classes;
    }
}
