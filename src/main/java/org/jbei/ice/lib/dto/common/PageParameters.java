package org.jbei.ice.lib.dto.common;

import org.jbei.ice.lib.shared.ColumnField;

/**
 * POJO for specifying parameters for a page of results
 * <p>
 * Field descriptions:
 * <ul>
 * <li><code>sort</code>     sort order for folder content retrieval</li>
 * <li><code>asc</code>      sort order for folder content retrieval; ascending if true</li>
 * <li><code>start</code>    index of first item in retrieval</li>
 * <li><code>limit</code>    upper limit count of items to be retrieval</li>
 * </ul>
 *
 * @author Hector Plahar
 */
public class PageParameters {

    private int offset;
    private int limit;
    private ColumnField sortField;
    private boolean ascending;
    private String filter;

    public PageParameters() {
    }

    public PageParameters(int offset, int limit, ColumnField sortField, boolean ascending, String filter) {
        this.offset = offset;
        this.limit = limit;
        this.sortField = sortField;
        this.ascending = ascending;
        this.filter = filter;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public ColumnField getSortField() {
        return sortField;
    }

    public void setSortField(ColumnField sortField) {
        this.sortField = sortField;
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
