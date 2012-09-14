package org.jbei.ice.lib.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.jbei.ice.lib.entry.model.Entry;

/**
 * Hold search results.
 * <p/>
 * Holds the score and the {@link Entry}.
 *
 * @author Zinovii Dmytriv
 */
public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Entry entry;
    private float score;

    public SearchResult() {
        entry = null;
        score = 0;
    }

    public SearchResult(Entry entry, float score) {
        setEntry(entry);
        setScore(score);
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getScore() {
        return score;
    }

    protected static ArrayList<SearchResult> sort(ArrayList<SearchResult> incoming) {
        class SearchResultComparator implements Comparator<SearchResult> {
            @Override
            public int compare(SearchResult arg0, SearchResult arg1) {
                float temp = arg1.getScore() - arg0.getScore();
                int result = 0;
                if (temp == 0.0) {
                    result = 0;
                } else if (temp < 0) {
                    result = -1;
                } else if (temp > 0) {
                    result = 1;
                }
                return result;
            }
        }

        SearchResultComparator comparator = new SearchResultComparator();
        Collections.sort(incoming, comparator);

        return incoming;
    }
}
