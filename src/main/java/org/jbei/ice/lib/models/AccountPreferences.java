package org.jbei.ice.lib.models;

public class AccountPreferences {
	private int id;
	private String preferences;
	private String restrictionEnzymes;
	private Account account;

	public AccountPreferences() {
		super();
	}

	public AccountPreferences(Account account, String preferences,
			String restrictionEnzymes) {
		super();
		this.preferences = preferences;
		this.restrictionEnzymes = restrictionEnzymes;
		this.account = account;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPreferences() {
		return preferences;
	}

	public void setPreferences(String preferences) {
		this.preferences = preferences;
	}

	public String getRestrictionEnzymes() {
		return restrictionEnzymes;
	}

	public void setRestrictionEnzymes(String restrictionEnzymes) {
		this.restrictionEnzymes = restrictionEnzymes;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}