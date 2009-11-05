package org.jbei.ice.lib.models;

public class EntryAssemblyRelationship {
	private int id;
	private Entry subject;
	private Entry object;
	private AssemblyRelationship relationship;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Entry getSubject() {
		return subject;
	}
	public void setSubject(Entry subject) {
		this.subject = subject;
	}
	public Entry getObject() {
		return object;
	}
	public void setObject(Entry object) {
		this.object = object;
	}
	public AssemblyRelationship getRelationship() {
		return relationship;
	}
	public void setRelationship(AssemblyRelationship relationship) {
		this.relationship = relationship;
	}
	
}
