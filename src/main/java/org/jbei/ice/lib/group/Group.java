package org.jbei.ice.lib.group;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import org.jbei.ice.lib.dao.IModel;

/**
 * Aggregate users into groups.
 * <p/>
 * Each group has a generated UUIDv4 identifier, except the "everyone" group, which is hard coded
 * and shared by all gd-ice instances. Groups can have parent groups.
 *
 * @author Timothy Ham, Ziovii Dmytriv, Hector Plahar
 */
@Entity
@Table(name = "groups")
@SequenceGenerator(name = "sequence", sequenceName = "groups_id_seq", allocationSize = 1)
public class Group implements IModel {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence")
    protected long id;

    @Column(name = "uuid", length = 36, nullable = false)
    protected String uuid;

    @Column(name = "label", length = 127, nullable = false)
    protected String label;

    @Column(name = "description", length = 255, nullable = false)
    protected String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent")
    protected Group parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Set<Group> children = new HashSet<Group>();

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Group getParent() {
        return parent;
    }

    public void setParent(Group parent) {
        this.parent = parent;
    }

    public Set<Group> getChildren() {
        return children;
    }
}
