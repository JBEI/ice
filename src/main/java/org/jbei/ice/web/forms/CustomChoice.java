package org.jbei.ice.web.forms;

import java.io.Serializable;

public class CustomChoice implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String value;

	public CustomChoice(String displayName, String value) {
		this.name = displayName;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
