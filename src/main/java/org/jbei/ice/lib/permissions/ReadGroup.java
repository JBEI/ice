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
@Table(name="permission_read_groups")
@SequenceGenerator(name = "permission_read_groups_sequence", sequenceName = "permission_read_groups_id_seq")
public class ReadGroup implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permission_read_groups_sequence")
	private int id;
	
	@ManyToOne
	@JoinColumn(name = "entry_id")
	private Entry entry;
	
	@ManyToOne
	@JoinColumn(name = "group_id")
	private Group readGroup;

	public ReadGroup(Entry entry, Group group) {
		setEntry(entry);
		setReadGroup(group);
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

	public Group getReadGroup() {
		return readGroup;
	}

	public void setReadGroup(Group readGroup) {
		this.readGroup = readGroup;
	}


}
