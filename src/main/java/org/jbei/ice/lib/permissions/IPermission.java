package org.jbei.ice.lib.permissions;

import java.util.Set;

import org.jbei.ice.lib.models.Account;

public interface IPermission {

	//getters and setters
	public Set<Group> getWriteGroups(); 
	public void setWriteGroup(Set<Group> writeGroup);
	public Set<Group> getReadGroups(); 
	public void setReadGroup(Set<Group> readGroup);
	public Set<Account> getReadUsers();
	public void setReadUsers(Set<Account> readUsers);
	public Set<Account> getWriteUsers();
	public void setWriteUsers(Set<Account> writeUsers);
			
}
