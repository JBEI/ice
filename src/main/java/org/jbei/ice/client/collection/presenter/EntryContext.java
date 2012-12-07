package org.jbei.ice.client.collection.presenter;

import org.jbei.ice.client.common.IHasNavigableData;

public class EntryContext {

    private long id;
    private String recordId;
    private Type type;
    private IHasNavigableData nav;
    private String partnerUrl;

    public EntryContext(Type type) {
        this.setType(type);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public IHasNavigableData getNav() {
        return nav;
    }

    public void setNav(IHasNavigableData nav) {
        this.nav = nav;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPartnerUrl() {
        return partnerUrl;
    }

    public void setPartnerUrl(String partnerUrl) {
        this.partnerUrl = partnerUrl;
    }

    public enum Type {
        SEARCH, COLLECTION, SAMPLES;
    }
}
