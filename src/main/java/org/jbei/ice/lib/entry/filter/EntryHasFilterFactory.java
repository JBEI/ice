package org.jbei.ice.lib.entry.filter;

import java.util.ArrayList;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;
import org.hibernate.search.filter.impl.CachingWrapperFilter;

/**
 * @author Hector Plahar
 */
public class EntryHasFilterFactory {

    private String[] field;

    // injected
    public void setField(ArrayList<String> field) {
        this.field = field.toArray(new String[field.size()]);
    }

    @Key
    public FilterKey getKey() {
        StandardFilterKey filterKey = new StandardFilterKey();
        filterKey.addParameter(field);
        return filterKey;
    }

    @Factory
    public Filter getFilter() {
        BooleanQuery query = new BooleanQuery(true);
        for (String f : field) {
            query.add(new TermQuery(new Term(f, "true")), BooleanClause.Occur.MUST);
        }
        return new CachingWrapperFilter(new QueryWrapperFilter(query));
    }
}
