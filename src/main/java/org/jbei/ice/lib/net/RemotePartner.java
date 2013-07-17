package org.jbei.ice.lib.net;

import java.util.Date;
import javax.persistence.*;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.shared.dto.web.RegistryPartner;
import org.jbei.ice.lib.shared.dto.web.RemotePartnerStatus;

/**
 * Stores information about partners that this registry is involved with in web of registries
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REMOTE_PARTNER")
@SequenceGenerator(name = "sequence", sequenceName = "remote_partner_id_seq", allocationSize = 1)
public class RemotePartner implements IModel {

    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "name", length = 127)
    private String name;

    @Column(name = "url", length = 127, unique = true, nullable = false)
    private String url;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    private RemotePartnerStatus partnerStatus;

    @Column(name = "add_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date added;

    @Column(name = "last_contact_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastContact;

    @Column(name = "fetched")
    private long fetched;

    @Column(name = "sent")
    private long sent;

    public long getId() {
        return id;
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

    public RemotePartnerStatus getPartnerStatus() {
        return partnerStatus;
    }

    public void setPartnerStatus(RemotePartnerStatus approved) {
        this.partnerStatus = approved;
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded(Date added) {
        this.added = added;
    }

    public Date getLastContact() {
        return lastContact;
    }

    public void setLastContact(Date lastContact) {
        this.lastContact = lastContact;
    }

    public long getFetched() {
        return fetched;
    }

    public void setFetched(long fetched) {
        this.fetched = fetched;
    }

    public long getSent() {
        return sent;
    }

    public void setSent(long sent) {
        this.sent = sent;
    }

    public static RegistryPartner toDTO(RemotePartner partner) {
        RegistryPartner registryPartner = new RegistryPartner();
        registryPartner.setId(partner.getId());
        registryPartner.setName(partner.getName());
        registryPartner.setUrl(partner.getUrl());
        registryPartner.setStatus(partner.getPartnerStatus());
        registryPartner.setSent(partner.getSent());
        registryPartner.setFetched(partner.getFetched());
        return registryPartner;
    }

}
