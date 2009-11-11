package org.jbei.ice.lib.models;

import java.io.Serializable;

import org.jbei.ice.lib.value_objects.ILinkValueObject;

public class Link implements ILinkValueObject, Serializable {
	private int id;
	private String link;
	private String url;
	private Entry entry;

	public Link() {
	}

	public Link(int id, String link, String url, Entry entry) {
			this.id = id;
			this.link = link;
			this.url = url;
			this.entry = entry;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}
}
