package org.jbei.ice.services.rest.multipart;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

import org.jbei.ice.services.rest.FileResource;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

/**
 * @author Hector Plahar
 */
public class IceApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        // register resources and features
        classes.add(MultiPartFeature.class);
        classes.add(FileResource.class);
        return classes;
    }
}
