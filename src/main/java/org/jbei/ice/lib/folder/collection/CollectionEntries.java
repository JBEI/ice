package org.jbei.ice.lib.folder.collection;

import org.jbei.ice.lib.access.PermissionException;
import org.jbei.ice.lib.dto.common.Results;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.Visibility;
import org.jbei.ice.lib.entry.OwnerEntries;
import org.jbei.ice.lib.entry.SampleEntries;
import org.jbei.ice.lib.entry.SharedEntries;
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
        return this.getEntries(field, asc, offset, limit, null, null);
    }

    /**
     * Retrieves parts (paged and sorted by the specified parameter values) based on the type of collection.
     *
     * @param sortField sort field
     * @param asc       sort order
     * @param offset    paging start
     * @param limit     maximum number of entries to retrieve
     * @param filter    optional text
     * @param fields    fields to include
     * @return wrapper around list of parts that conform to the parameters and the maximum number
     * of such entries that are available
     */
    public Results<PartData> getEntries(ColumnField sortField, boolean asc, int offset, int limit, String filter, List<String> fields) {
        switch (this.type) {
            case PERSONAL:
            default:
                return this.getPersonalEntries(sortField, asc, offset, limit, filter, fields);

            case FEATURED:
            case AVAILABLE:
                return this.getAvailableEntries(sortField, asc, offset, limit, filter, fields);

            case SHARED:
                return this.getSharedEntries(sortField, asc, offset, limit, filter, fields);

            case DELETED:
                return this.getEntriesByVisibility(Visibility.DELETED, sortField, asc, offset, limit, this.userId, filter, fields);

            case DRAFTS:
                return this.getEntriesByVisibility(Visibility.DRAFT, sortField, asc, offset, limit, this.userId, filter, fields);

            case PENDING:
                return this.getEntriesByVisibility(Visibility.PENDING, sortField, asc, offset, limit, null, filter, fields);

            case TRANSFERRED:
                return this.getEntriesByVisibility(Visibility.TRANSFERRED, sortField, asc, offset, limit, null, filter, fields);

            case SAMPLES:
                return this.getSampleEntries(sortField, asc, offset, limit, filter, fields);
        }
    }

    private Results<PartData> getSampleEntries(ColumnField field, boolean asc, int offset, int limit, String filter, List<String> fields) {
        SampleEntries entries = new SampleEntries(this.userId);
        final List<PartData> list = entries.get(field, asc, offset, limit, filter, fields);
        final long count = entries.getCount(filter);
        Results<PartData> results = new Results<>();
        results.setResultCount(count);
        results.setData(list);
        return results;
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
     * @throws PermissionException on null user id which is required for owner entries
     */
    private Results<PartData> getPersonalEntries(ColumnField field, boolean asc, int offset, int limit,
                                                 String filter, List<String> fields) {
        if (userId == null || userId.isEmpty())
            throw new PermissionException("User id is required to retrieve owner entries");
        OwnerEntries ownerEntries = new OwnerEntries(userId, userId);
        final List<PartData> entries = ownerEntries.retrieveOwnerEntries(field, asc, offset, limit, filter, fields);
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
    private Results<PartData> getAvailableEntries(ColumnField field, boolean asc, int offset, int limit,
                                                  String filter, List<String> fields) {
        VisibleEntries visibleEntries = new VisibleEntries(userId);
        List<PartData> entries = visibleEntries.getEntries(field, asc, offset, limit, filter, fields);
        long count = visibleEntries.getEntryCount(filter);
        Results<PartData> results = new Results<>();
        results.setResultCount(count);
        results.setData(entries);
        return results;
    }

    /**
     * Retrieves entries shared with user.
     *
     * @param field  sort field
     * @param asc    sort order ascending if true, descending if false
     * @param offset paging parameter start
     * @param limit  maximum number of entries to retrieve
     * @param filter optional text to filter entries by
     * @return wrapper around list of parts matching the parameters along with the maximum number of entries
     * available
     */
    private Results<PartData> getSharedEntries(ColumnField field, boolean asc, int offset, int limit, String filter, List<String> fields) {
        SharedEntries sharedEntries = new SharedEntries(this.userId);
        List<PartData> entries = sharedEntries.getEntries(field, asc, offset, limit, filter, fields);
        final long count = sharedEntries.getNumberOfEntries(filter);
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
     * @param user       additional user filter
     * @param filter     optional text to filter entries by
     * @return wrapper around list of parts that conform to the parameters and the maximum number
     * of such entries that are available
     */
    private Results<PartData> getEntriesByVisibility(Visibility visibility, ColumnField field, boolean asc,
                                                     int offset, int limit, String user, String filter, List<String> fields) {
        List<Entry> entries = entryDAO.getByVisibility(user, visibility, field, asc, offset, limit, filter);
        Results<PartData> results = new Results<>();
        for (Entry entry : entries) {
            PartData info = ModelToInfoFactory.createTableViewData(userId, entry, false, fields);
            results.getData().add(info);
        }
        results.setResultCount(entryDAO.getByVisibilityCount(user, visibility, filter));
        return results;
    }
}
