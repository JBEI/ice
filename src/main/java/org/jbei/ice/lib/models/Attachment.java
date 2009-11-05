package org.jbei.ice.lib.models;

import org.jbei.ice.lib.utils.Base64String;
import org.jbei.ice.lib.value_objects.AttachmentValueObject;

public class Attachment implements AttachmentValueObject {
	private int id;
	private String description;
	private String fileName;
	private String fileId;
	private Entry entry;
	private Base64String data; //data is persisted base64 encoded string as a file on disk, not in db. 
	
	public Attachment() {
	}
	
	/** 
	 * Attachment constructor
	 * @param description description
	 * @param fileName file name
	 * @param entry Entry instance
	 * @param data base64 encoded string 
	 */
	public Attachment (String description, String fileName, Entry entry, Base64String data) {
		this.description = description; 
		this.fileName = fileName;
		this.fileId = fileId;
		this.entry = entry;
		this.data = data;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileId() {
		return fileId;
	}
	public void setFileId(String fileId) {
		this.fileId = fileId;
	}
	public Entry getEntry() {
		return entry;
	}
	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	public void setData(Base64String data) {
		this.data = data;
	}
	public Base64String getData() {
		return data;
	}
	
}
