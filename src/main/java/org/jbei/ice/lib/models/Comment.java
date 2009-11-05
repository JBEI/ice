package org.jbei.ice.lib.models;

import java.util.Calendar;
import java.util.Date;

public class Comment {
	private int id;
	private Account account;
	private Entry entry;
	private String body;
	private Date creationTime;
	
	public Comment () {
		
	}
	
	public Comment(Entry entry, Account account, String body) {
		this.setEntry(entry);
		this.setAccount(account);
		this.setBody(body);
		this.setCreationTime(Calendar.getInstance().getTime());
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public Entry getEntry() {
		return entry;
	}
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Date getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	

}
