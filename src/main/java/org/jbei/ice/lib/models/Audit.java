package org.jbei.ice.lib.models;

import java.util.Date;
import javax.persistence.*;

import org.jbei.ice.lib.dao.IDataModel;
import org.jbei.ice.lib.dto.History;
import org.jbei.ice.lib.entry.model.Entry;

/**
 * Audit object for keep track of actions affecting entries.
 * Actions include : edit, view
 *
 * @author Hector Plahar
 */
@Entity
@Table(name = "audit")
@SequenceGenerator(name = "sequence", sequenceName = "audit_id_seq", allocationSize = 1)
public class Audit implements IDataModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    // what
    @Column(name = "action", length = 12, nullable = false)
    private String action;

    // who (using userId since it could come from somewhere else)
    @Column(name = "userId", length = 127, nullable = false)
    private String userId;

    // whether the audit event is from a local user; note that
    // that the user id could match a local one but the user could be remote
    @Column(name = "localUser")
    private boolean localUser;

    @Column(name = "institution", length = 255)
    private String institution;

    @Column(name = "firstname", length = 50)
    private String firstName;

    @Column(name = "lastName", length = 50)
    private String lastName;

    // when
    @Column(name = "time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date time;

    // entity (one entry can have many audits, one audit cannot have many entries)
    @ManyToOne
    @JoinColumn(name = "entry_id")
    private Entry entry;

    public long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public boolean isLocalUser() {
        return localUser;
    }

    public void setLocalUser(boolean localUser) {
        this.localUser = localUser;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public History toDataTransferObject() {
        return null;
    }
}
