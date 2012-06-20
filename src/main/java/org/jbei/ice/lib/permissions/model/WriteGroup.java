package org.jbei.ice.lib.permissions.model;

import org.jbei.ice.lib.dao.IModel;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.group.Group;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Give a {@link Group} write permission to an {@link Entry}.
 * <p/>
 * If WriteGroup object exists for a given Entry:Group pair, then the Group has write permission to
 * the Entry.
 *
 * @author Timothy Ham, Zinovii Dmytriv
 */
@Entity
@Table(name = "permission_write_groups")
@SequenceGenerator(name = "sequence", sequenceName = "permission_write_groups_id_seq", allocationSize = 1)
public class WriteGroup implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    private int id;

    @ManyToOne
    @JoinColumn(name = "entry_id")
    private Entry entry;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    public WriteGroup() {

    }

    public WriteGroup(Entry entry, Group group) {
        setEntry(entry);
        setGroup(group);
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

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

}
