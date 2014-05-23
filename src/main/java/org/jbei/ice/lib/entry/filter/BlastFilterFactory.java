package org.jbei.ice.lib.entry.filter;

import java.util.HashSet;

import org.apache.lucene.search.FieldCacheTermsFilter;
import org.apache.lucene.search.Filter;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;

/**
 * @author Hector Plahar
 */
public class BlastFilterFactory {

    private HashSet<String> recordIds;

    // injected
    public void setRecordIds(HashSet<String> ids) {
        this.recordIds = ids;
    }

    @Key
    public FilterKey getKey() {
        return new StandardFilterKey();
    }

    @Factory
    public Filter getFilter() {
        String[] a = new String[]{};
        return new FieldCacheTermsFilter("id", recordIds.toArray(a));
    }
}
