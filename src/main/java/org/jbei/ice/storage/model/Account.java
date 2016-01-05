package org.jbei.ice.storage.model;

import org.hibernate.annotations.Type;
import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.storage.DataModel;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Store the account information for a single user.
 * <p>
 *
 * @author Timothy Ham, Zinovii Dmytriv, Hector Plahar
 */
@Entity
@Table(name = "accounts")
@SequenceGenerator(name = "sequence", sequenceName = "accounts_id_seq", allocationSize = 1)
public class Account implements DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    private long id;

    @Column(name = "firstname", length = 50, nullable = false)
    private String firstName;

    @Column(name = "lastname", length = 50, nullable = false)
    private String lastName;

    @Column(name = "initials", length = 10, nullable = false)
    private String initials;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "institution", length = 255, nullable = false)
    private String institution;

    @Column(name = "password", length = 40, nullable = false)
    private String password;

    @Column(name = "description", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Column(name = "ip", length = 20, nullable = false)
    private String ip;

    @Column(name = "creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Column(name = "is_subscribed")
    private int isSubscribed;

    @Column(name = "modification_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationTime;

    @Column(name = "lastlogin_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginTime;

    @Enumerated(EnumType.STRING)
    private AccountType type = AccountType.NORMAL;

    @Column(name = "salt")
    private String salt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "account_group", joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> groups = new LinkedHashSet<>();

    /**
     * Constructor.
     */
    public Account() {
        super();
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

    public Set<Group> getGroups() {
        return groups;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != Account.class)
            return false;

        Account account = (Account) obj;
        return account.getId() == this.getId() && account.getEmail().equals(this.getEmail());
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.id).intValue();
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        if (type == null)
            this.type = AccountType.NORMAL;
        else
            this.type = type;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public AccountTransfer toDataTransferObject() {
        AccountTransfer info = new AccountTransfer();
        info.setEmail(email);
        info.setFirstName(firstName);
        info.setLastName(lastName);
        info.setInstitution(institution);
        info.setDescription(description);
        if (lastLoginTime != null)
            info.setLastLogin(lastLoginTime.getTime());
        if (this.type != null)
            info.setAccountType(this.type);
        else
            info.setAccountType(AccountType.NORMAL);
        if (this.creationTime != null)
            info.setRegisterDate(this.creationTime.getTime());
        info.setId(id);
        return info;
    }
}
