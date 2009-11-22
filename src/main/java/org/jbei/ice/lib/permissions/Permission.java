package org.jbei.ice.lib.permissions;

import java.util.Set;

import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;

public class Permission {

	private int id;
	private Entry entry;
	private Account account;
	private Set<Group> writeGroups;
	private Set<Group> readGroups;	
	private Set<Account> readUsers;
	private Set<Account> writeUsers;
	
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
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public Set<Group> getWriteGroup() {
		return writeGroups;
	}
	public void setWriteGroup(Set<Group> writeGroup) {
		this.writeGroups = writeGroup;
	}
	public Set<Group> getReadGroup() {
		return readGroups;
	}
	public void setReadGroup(Set<Group> readGroup) {
		this.readGroups = readGroup;
	}
	public Set<Account> getReadUsers() {
		return readUsers;
	}
	public void setReadUsers(Set<Account> readUsers) {
		this.readUsers = readUsers;
	}
	public Set<Account> getWriteUsers() {
		return writeUsers;
	}
	public void setWriteUsers(Set<Account> writeUsers) {
		this.writeUsers = writeUsers;
	}


		
}
