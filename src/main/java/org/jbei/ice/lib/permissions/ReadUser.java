package org.jbei.ice.lib.permissions;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;

@Entity
@Table(name="permission_read_users")
@SequenceGenerator(name = "sequence", sequenceName = "permission_read_users_id_seq",
		allocationSize = 1)
public class ReadUser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
	private int id;
	
	@ManyToOne
	@JoinColumn(name="entry_id")
	private Entry entry;
	
	@ManyToOne
	@JoinColumn(name = "account_id")
	private Account readUser;
	
	public ReadUser(Entry entry, Account account) {
		setEntry(entry);
		setReadUser(account);
	}
	
	//getters and setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Entry getEntry() {
		return entry;
	}
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	public Account getReadUser() {
		return readUser;
	}
	public void setReadUser(Account readUser) {
		this.readUser = readUser;
	}

	
}
