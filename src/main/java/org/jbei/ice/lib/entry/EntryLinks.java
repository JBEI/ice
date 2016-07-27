package org.jbei.ice.lib.entry;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.EntryType;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.hibernate.dao.SequenceDAO;
import org.jbei.ice.storage.model.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a main part and the hierarchical links that it is involved in.
 * The specified user must have read privileges on the main part.
 * Some of the actions/methods in this class require write privileges
 *
 * @author Hector Plahar
 */
public class EntryLinks {

    private final EntryDAO entryDAO;
    private final SequenceDAO sequenceDAO;
    private final Entry entry;
    private final EntryAuthorization entryAuthorization;
    private final String userId;

    public EntryLinks(String userId, long partId) {
        this.entryDAO = DAOFactory.getEntryDAO();
        this.entry = this.entryDAO.get(partId);
        if (this.entry == null)
            throw new IllegalArgumentException("Could not retrieve part with id " + partId);
        this.userId = userId;
        this.sequenceDAO = DAOFactory.getSequenceDAO();
        this.entryAuthorization = new EntryAuthorization();
        this.entryAuthorization.expectRead(userId, this.entry);
    }

    /**
     * Adds a link for the specified entry (or creates a new entry) of the type specified
     *
     * @param partData data for entry to be linked. If the id does not point to an existing entry, a new one will be
     *                 created and linked to this entry
     * @param type     type of link to create. If <code>CHILD</code> (default) is specified, then the entry in the parameter
     *                 is added as a child in the hierarchy; if <code>PARENT</code> is specified, then the entry is
     *                 add as a parent in the hierarchy.
     * @return true, if the two entries are successfully linked. false, if the specified hierarchy is not compatible
     * with the types of the link specified e.g. an plasmid cannot be added as a <code>PARENT</code> of a strain
     * @throws org.jbei.ice.lib.access.PermissionException if the user does not have write permissions
     *                                                     on at least one of the entries being linked
     */
    public boolean addLink(PartData partData, LinkType type) {
        long linkId = partData.getId();
        Entry linkedEntry = this.entryDAO.get(linkId);
        if (linkedEntry == null) {
            // todo : create a new entry
            Logger.error("Could not retrieve entry for linking: " + linkId);
            return false;
        }

        if (linkedEntry.getId() == this.entry.getId())
            throw new IllegalArgumentException("Cannot link and entry to itself");

        // should have write permissions on the main entry but only read on the entry being linked
        entryAuthorization.expectRead(userId, linkedEntry);
        entryAuthorization.expectWrite(userId, entry);

        // add as parent
        switch (type) {
            case PARENT:
                return addParentEntry(linkedEntry);

            // add as child. default behavior
            case CHILD:
            default:
                return addChildEntry(linkedEntry);
        }
    }

    /**
     * Removes link specified entry id based on the specified type
     *
     * @param partToRemove unique identifier for part to remove
     * @param linkType     type of relationship that exists
     * @return true, if entry is removed successfully.
     */
    public boolean removeLink(long partToRemove, LinkType linkType) {
        entryAuthorization.expectWrite(userId, entry);
        Entry linkedEntry = entryDAO.get(partToRemove);

        switch (linkType) {
            case PARENT:
                return linkedEntry.getLinkedEntries().remove(entry) && entryDAO.update(linkedEntry) != null;

            case CHILD:
            default:
                return entry.getLinkedEntries().remove(linkedEntry) && entryDAO.update(entry) != null;
        }
    }

    /**
     * Adds specified entry as a child of the entry associated with <code>this</code>
     *
     * @param child entry to be added as the child entry
     * @return true if added successfully, false otherwise
     */
    protected boolean addChildEntry(Entry child) {
        if (!isCompatible(
                EntryType.nameToType(this.entry.getRecordType()),
                EntryType.nameToType(child.getRecordType()))) {
            return false;
        }
        this.entry.getLinkedEntries().add(child);
        return this.entryDAO.update(this.entry) != null;
    }

    protected boolean addParentEntry(Entry parent) {
        if (!isCompatible(
                EntryType.nameToType(parent.getRecordType()),
                EntryType.nameToType(this.entry.getRecordType()))) {
            return false;
        }
        parent.getLinkedEntries().add(this.entry);
        return this.entryDAO.update(parent) != null;
    }

    private boolean isCompatible(EntryType parentType, EntryType childType) {
        switch (parentType) {
            case ARABIDOPSIS:
                return childType == parentType || childType == EntryType.PART;

            case STRAIN:
                return childType == parentType || childType == EntryType.PLASMID || childType == EntryType.PART;

            case PART:
            default:
                return childType == parentType;

            case PLASMID:
                return childType == parentType || childType == EntryType.PART;
        }
    }

    /**
     * Retrieves the list of children this entry, that the user has read permissions
     *
     * @return list of retrieved entries
     */
    public List<PartData> getChildren() {
        List<PartData> children = new ArrayList<>(this.entry.getLinkedEntries().size());
        for (Entry childEntry : this.entry.getLinkedEntries()) {
            if (!entryAuthorization.canRead(this.userId, childEntry))
                continue;
            children.add(childEntry.toDataTransferObject());
        }
        return children;
    }

    /**
     * Retrieves list of this entry's parents that user has read access to
     *
     * @return list of parents of entry
     */
    public List<PartData> getParents() {
        List<Entry> parents = this.entryDAO.getParents(this.entry.getId());
        List<PartData> parentData = new ArrayList<>(parents.size());
        for (Entry parent : parents) {
            if (!entryAuthorization.canRead(this.userId, parent))
                continue;
            parentData.add(parent.toDataTransferObject());
        }
        return parentData;
    }

    /**
     * Retrieves entry links that are parents or children depending on specified type
     *
     * @param type specified type of links to return
     * @return list of links that match specified type. These are filtered based on user permissions
     */
    public List<PartData> get(LinkType type) {
        List<Entry> entries;
        if (type == LinkType.CHILD) {
            entries = new ArrayList<>(this.entry.getLinkedEntries());
        } else {
            entries = this.entryDAO.getParents(this.entry.getId());
        }

        // get sequence and other summary information
        List<PartData> results = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            if (!this.entryAuthorization.canRead(this.userId, entry))
                continue;

            PartData partData = new PartData(EntryType.nameToType(entry.getRecordType()));
            partData.setId(entry.getId());
            partData.setName(entry.getName());
            partData.setPartId(entry.getPartNumber());
            partData.setShortDescription(entry.getShortDescription());

            boolean hasSequence = sequenceDAO.hasSequence(entry.getId());
            partData.setHasSequence(hasSequence);
            boolean hasOriginalSequence = sequenceDAO.hasOriginalSequence(entry.getId());
            partData.setHasOriginalSequence(hasOriginalSequence);
            String sequenceString = sequenceDAO.getSequenceString(entry);
            if (StringUtils.isEmpty(sequenceString))
                partData.setBasePairCount(0);
            else
                partData.setBasePairCount(sequenceString.trim().length());

            if (!StringUtils.isEmpty(sequenceString)) {
                partData.setFeatureCount(DAOFactory.getSequenceFeatureDAO().getFeatureCount(entry));
            }

            results.add(partData);
        }

        return results;
    }
}
