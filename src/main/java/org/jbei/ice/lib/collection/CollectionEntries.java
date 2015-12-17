package org.jbei.ice.lib.collection;

import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.OwnerEntries;
import org.jbei.ice.lib.entry.VisibleEntries;
import org.jbei.ice.lib.shared.ColumnField;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.ModelToInfoFactory;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Entry;

import java.util.List;

/**
 * Represents entries contained within a specified collection filtered by
 * the permissions available to the person retrieving the entries
 *
 * @author Hector Plahar
 */
public class CollectionEntries {

    private final CollectionType type;
    private final String userId;
    private final EntryDAO entryDAO;

    public CollectionEntries(String userId, CollectionType type) {
        this.type = type;
        this.userId = userId;
        entryDAO = DAOFactory.getEntryDAO();
    }

    /**
     * Retrieves list of entries without filter text {@see getEntries} which this delegates to with
     * null filter value
     *
     * @param field  sort field
     * @param asc    sort order
     * @param offset paging start
     * @param limit  maximum number of entries to retrieve
     * @return wrapper around list of parts that conform to the parameters and the maximum number
     * of such entries that are available
     */
    public Results<PartData> getEntries(ColumnField field, boolean asc, int offset, int limit) {
        return this.getEntries(field, asc, offset, limit, null);
    }

    /**
     * Retrieves parts (paged and sorted by the specified parameter values) based on the type of collection
     *
     * @param field  sort field
     * @param asc    sort order
     * @param offset paging start
     * @param limit  maximum number of entries to retrieve
     * @param filter optional text
     * @return wrapper around list of parts that conform to the parameters and the maximum number
     * of such entries that are available
     */
    public Results<PartData> getEntries(ColumnField field, boolean asc, int offset, int limit, String filter) {
        switch (this.type) {
            case PERSONAL:
            default:
                return this.getPersonalEntries(field, asc, offset, limit, filter);

            case AVAILABLE:
                return this.getAvailableEntries(field, asc, offset, limit, filter);

            case DELETED:
                return this.getEntriesByVisibility(Visibility.DELETED, field, asc, offset, limit, filter);

            case DRAFTS:
                return this.getEntriesByVisibility(Visibility.DRAFT, field, asc, offset, limit, filter);

            case PENDING:
                return this.getEntriesByVisibility(Visibility.PENDING, field, asc, offset, limit, filter);

            case TRANSFERRED:
                return this.getEntriesByVisibility(Visibility.TRANSFERRED, field, asc, offset, limit, filter);
        }
    }

    /**
     * Retrieves entries owned by user
     *
     * @param field  sort field
     * @param asc    sort order
     * @param offset paging start
     * @param limit  maximum number of entries to retrieve
     * @param filter optional text to filter entries by
     * @return wrapper around list of parts that conform to the parameters and the maximum number
     * of such entries that are available
     */
    protected Results<PartData> getPersonalEntries(ColumnField field, boolean asc, int offset, int limit,
                                                   String filter) {
        OwnerEntries ownerEntries = new OwnerEntries(userId, userId);
        final List<PartData> entries = ownerEntries.retrieveOwnerEntries(field, asc, offset, limit, filter);
        final long count = ownerEntries.getNumberOfOwnerEntries();
        Results<PartData> results = new Results<>();
        results.setResultCount(count);
        results.setData(entries);
        return results;
    }

    /**
     * Retrieves entries available to user. "Availability" is determined by any permissions set on the entries
     *
     * @param field  sort field
     * @param asc    sort order
     * @param offset paging start
     * @param limit  maximum number of entries to retrieve
     * @param filter optional text to filter entries by
     * @return wrapper around list of parts that conform to the parameters and the maximum number
     * of such entries that are available
     */
    protected Results<PartData> getAvailableEntries(ColumnField field, boolean asc, int offset, int limit,
                                                    String filter) {
        VisibleEntries visibleEntries = new VisibleEntries(userId);
        List<PartData> entries = visibleEntries.getEntries(field, asc, offset, limit, filter);
        long count = visibleEntries.getEntryCount(filter);
        Results<PartData> results = new Results<>();
        results.setResultCount(count);
        results.setData(entries);
        return results;
    }

    /**
     * Retrieves entries by the specified visibility. {@link Visibility} is used to create
     * collections of entries
     *
     * @param visibility visibility to retrieve entries by
     * @param field      sort field
     * @param asc        sort order
     * @param offset     paging start
     * @param limit      maximum number of entries to retrieve
     * @param filter     optional text to filter entries by
     * @return wrapper around list of parts that conform to the parameters and the maximum number
     * of such entries that are available
     */
    protected Results<PartData> getEntriesByVisibility(Visibility visibility, ColumnField field, boolean asc,
                                                       int offset, int limit, String filter) {
        List<Entry> entries = entryDAO.getByVisibility(userId, visibility, field, asc, offset, limit, filter);
        Results<PartData> results = new Results<>();
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false);
            results.getData().add(info);
        }
        results.setResultCount(entryDAO.getByVisibilityCount(userId, visibility, filter));
        return results;
    }
}
