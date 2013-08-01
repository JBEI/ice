package org.jbei.ice.lib.vo;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB bean to be used for MTOM optimization
 *
 * @author Hector Plahar
 */
@XmlType
public class PartAttachment {

    private String name;
    private String description;

    @XmlMimeType("application/octet-stream")
    private DataHandler data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataHandler getAttachmentData() {
        return data;
    }

    public void setAttachmentData(DataHandler data) {
        this.data = data;
    }
}
