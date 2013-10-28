package org.jbei.ice.lib.entry.filter;

import org.apache.lucene.search.FieldCacheTermsFilter;
import org.apache.lucene.search.Filter;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

/**
 * @author Hector Plahar
 */
public class EntryHasFilterFactory {

    private String field;

    // injected
    public void setField(String field) {
        this.field = field;
    }

    @Key
    public FilterKey getKey() {
        StandardFilterKey filterKey = new StandardFilterKey();
        filterKey.addParameter(field);
        return filterKey;
    }

    @Factory
    public Filter getFilter() {
        return new FieldCacheTermsFilter(this.field, "true");
    }
}
