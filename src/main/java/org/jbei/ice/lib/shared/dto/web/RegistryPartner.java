package org.jbei.ice.lib.shared.dto.web;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Remote Web of Registries partner
 *
 * @author Hector Plahar
 */
public class RegistryPartner implements IDTOModel {

    private long id;
    private RemoteActionStatus status;
    private String name;
    private String url;

    public RegistryPartner() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public RemoteActionStatus getStatus() {
        return status;
    }

    public void setStatus(RemoteActionStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
