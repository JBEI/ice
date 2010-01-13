package org.jbei.ice.lib.models;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jbei.ice.lib.permissions.Group;

@Entity
@Table(name = "accounts")
@SequenceGenerator(name = "sequence", sequenceName = "accounts_id_seq", allocationSize = 1)
public class Account implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
	private int id;

	@Column(name = "firstname", length = 50, nullable = false)
	private String firstName;

	@Column(name = "lastname", length = 50, nullable = false)
	private String lastName;

	@Column(name = "initials", length = 10)
	private String initials;

	@Column(name = "email", length = 100, nullable = false)
	private String email;

	@Column(name = "institution", length = 255)
	private String institution;

	@Column(name = "password", length = 32)
	private String password;

	@Column(name = "description")
	private String description;

	@Column(name = "is_subscribed", nullable = false)
	private int isSubscribed;

	@Column(name = "ip", length = 20)
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

	@ManyToMany
	@JoinTable(name = "account_group", joinColumns = @JoinColumn(name = "account_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
	private Set<Group> groups;

	public Account() {
		super();
	}

	public Account(String firstName, String lastName, String initials,
			String email, String password, String institution,
			int isSubscribed, String description, String ip, Date creationTime,
			Date modificationTime, Date lastLoginTime) {
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

	public Account(String firstName, String lastName, String initials,
			String email, String password, String institution,
			String description) {
		super();

		this.firstName = firstName;
		this.lastName = lastName;
		this.initials = initials;
		this.email = email;
		this.password = password;
		this.institution = institution;
		this.description = description;
		this.creationTime = new Date();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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
