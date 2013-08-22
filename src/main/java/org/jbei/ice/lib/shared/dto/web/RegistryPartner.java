package org.jbei.ice.lib.shared.dto.web;

import org.jbei.ice.lib.shared.dto.IDTOModel;

/**
 * Remote Web of Registries partner
 *
 * @author Hector Plahar
 */
public class RegistryPartner implements IDTOModel {

    private static final long serialVersionUID = 1l;

    private long id;
    private String status;
    private String name;
    private String url;
    private String apiKey;
    private long sent;
    private long fetched;

    public RegistryPartner() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public long getFetched() {
        return fetched;
    }

    public void setFetched(long fetched) {
        this.fetched = fetched;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String toString() {
        return name + "(" + url + "); status = " + status + "; apiKey = " + apiKey;
    }
}
