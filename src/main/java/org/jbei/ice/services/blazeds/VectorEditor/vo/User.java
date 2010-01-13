package org.jbei.ice.services.blazeds.VectorEditor.vo;

public class User {
	private String firstname;
	private String lastname;
	private String authToken;
	
	// Constructors
	public User() {
	}
	
	public User(String firstname, String lastname, String authToken) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.authToken= authToken;
	}
	
	// Properties
	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getAuthToken() {
		return authToken;
	}
}
