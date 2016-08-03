package org.jbei.ice.storage.model;

import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.RemotePartnerStatus;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Represents a remote entity that this ice instance knows about
 * and could potentially communicate with or allow communications from
 * <br>Field Descriptions:
 * <ul>
 * <li><code>NAME</code>: Name of the remote instance for display purposes.</li>
 * <li><code>URL</code>: Unique resource locator for the partner. This is used together with the api key to verify
 * access</li>
 * <li><code>STATUS</code>: {@link RemotePartnerStatus} that is used to indicated whether a partner is blocked
 * or approved. All partners are initially approved unless explicitly blocked by the administrator. Partners
 * that are blocked and attempt to request a search will have that request fail silently. Outgoing requests
 * to blocked partners are also not permitted</li>
 * <li><code>API_KEY</code>: This is a globally unique identifier received from a partner and used
 * for all communications with that partner.</li>
 * <li><code>AUTHENTICATION_TOKEN</code>: This is an encrypted <code>API_KEY</code> generated on this system
 * and sent to other partner. The token that the partner receives is the unencrypted <code>API_KEY</code> which the
 * partner then includes for all requests to this server.</li>
 * <li><code>ADD_TIME</code>: Time partner was added</li>
 * <li><code>LAST_CONTACT_TIME</code>: Last time contact was made with partner</li>
 * <li><code>FETCHED</code>: Number of parts that this system has fetched from partner</li>
 * <li><code>SENT</code>: Number of parts that this system has sent to partner</li>
 * </ul>
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "REMOTE_PARTNER")
@SequenceGenerator(name = "sequence", sequenceName = "remote_partner_id_seq", allocationSize = 1)
public class RemotePartner implements DataModel {

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

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "salt")
    private String salt;

    @Column(name = "authentication_token")
    private String authenticationToken;

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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public RegistryPartner toDataTransferObject() {
        RegistryPartner registryPartner = new RegistryPartner();
        registryPartner.setId(this.id);
        registryPartner.setName(this.name);
        if(this.added != null)
            registryPartner.setAddTime(this.added.getTime());
        if (this.lastContact != null)
            registryPartner.setLastContactTime(this.lastContact.getTime());
        registryPartner.setUrl(this.url);
        registryPartner.setStatus(this.partnerStatus);
        registryPartner.setSent(getSent());
        registryPartner.setFetched(fetched);
//        registryPartner.setApiKey(apiKey);
        return registryPartner;
    }
}
