package org.jbei.ice.lib.account.model;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.group.Group;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Store the account information for a single user.
 * <p/>
 * Because gd-ice is able to import an {@link org.jbei.ice.lib.entry.model.Entry} object from another gd-ice
 * instance, but the
 * Account associated with the entry is not imported, Entries may point to an Account that may not
 * exist. To work around this possibility, Entries are associated with an Account object via the
 * email field (Entry.ownerEmail to Account.email). This of course means that as far as the system
 * is concerned, email is the identifying value of a user, not the Account's id.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "accounts")
@SequenceGenerator(name = "sequence", sequenceName = "accounts_id_seq", allocationSize = 1)
public class Account implements IModel {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private long id;

    @Column(name = "firstname", length = 50, nullable = false)
    private String firstName;

    @Column(name = "lastname", length = 50, nullable = false)
    private String lastName;

    @Column(name = "initials", length = 10, nullable = false)
    private String initials;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "institution", length = 255, nullable = false)
    private String institution;

    @Column(name = "password", length = 40, nullable = false)
    private String password;

    @Column(name = "description", nullable = false)
    @Lob
    private String description;

    @Column(name = "is_subscribed", nullable = false)
    private int isSubscribed;

    @Column(name = "ip", length = 20, nullable = false)
    private String ip;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @Column(name = "lastlogin_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginTime;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "account_group", joinColumns = @JoinColumn(name = "account_id"),
               inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> groups = new LinkedHashSet<Group>();

    /**
     * Constructor.
     */
    public Account() {
        super();
    }

    /**
     * Constructor with parameters.
     *
     * @param firstName
     * @param lastName
     * @param initials
     * @param email
     * @param password
     * @param institution
     * @param isSubscribed
     * @param description
     * @param ip
     * @param creationTime
     * @param modificationTime
     * @param lastLoginTime
     */
    public Account(String firstName, String lastName, String initials, String email,
            String password, String institution, int isSubscribed, String description, String ip,
            Date creationTime, Date modificationTime, Date lastLoginTime) {
        super();

        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.isSubscribed = isSubscribed;
        this.description = description;
        this.ip = ip;
        this.creationTime = creationTime;
        this.modificationTime = modificationTime;
        this.lastLoginTime = lastLoginTime;
    }

    /**
     * Constructor with simplified parameters.
     *
     * @param firstName
     * @param lastName
     * @param initials
     * @param email
     * @param password
     * @param institution
     * @param description
     */
    public Account(String firstName, String lastName, String initials, String email,
            String password, String institution, String description) {
        super();

        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
        this.email = email;
        this.password = password;
        this.institution = institution;
        this.description = description;
        creationTime = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public int getIsSubscribed() {
        return isSubscribed;
    }

    public void setIsSubscribed(int isSubscribed) {
        this.isSubscribed = isSubscribed;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
