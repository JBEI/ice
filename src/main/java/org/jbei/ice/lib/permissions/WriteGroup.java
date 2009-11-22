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

import org.jbei.ice.lib.permissions.Group;
import org.jbei.ice.lib.models.Entry;

@Entity
@Table(name="permission_write_groups")
@SequenceGenerator(name = "permission_write_groups_sequence", sequenceName = "permission_write_groups_id_seq")
public class WriteGroup implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permission_write_groups_sequence")
	private int id;
	
	@ManyToOne
	@JoinColumn(name = "entry_id")
	private Entry entry;
	
	@ManyToOne
	@JoinColumn(name = "group_id")
	private Group writeGroup;

	public WriteGroup(Entry entry, Group group) {
		setEntry(entry);
		setWriteGroup(group);
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

	public Group getWriteGroup() {
		return writeGroup;
	}

	public void setWriteGroup(Group writeGroup) {
		this.writeGroup = writeGroup;
	}

}
