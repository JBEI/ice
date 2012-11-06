package org.jbei.ice.client.bulkupload;

import org.jbei.ice.client.collection.view.OptionSelect;

public class BulkUploadMenuItem extends OptionSelect {

    private long count;
    private String dateTime;
    private String type;
    private String email;

    public BulkUploadMenuItem(long id, String name, long count, String datetime, String type,
            String email) {
        super(id, name);
        this.setCount(count);
        this.setDateTime(datetime);
        this.setType(type);
        this.setEmail(email);
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
