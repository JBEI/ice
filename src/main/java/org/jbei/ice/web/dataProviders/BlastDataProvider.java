package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.search.BlastResult;

public class BlastDataProvider implements IDataProvider<BlastResult> {
    private static final long serialVersionUID = 1L;

    private ArrayList<BlastResult> blastResults;
    private ArrayList<Entry> entries;

    public BlastDataProvider(ArrayList<BlastResult> blastResults) {
        super();

        this.blastResults = blastResults;
    }

    @Override
    public Iterator<BlastResult> iterator(int first, int count) {
        int numBlastResults = blastResults.size();

        if (first > numBlastResults - 1) {
            first = numBlastResults - 1;
        }

        if (first + count > numBlastResults) {
            count = numBlastResults - 1 - first;
        }

        entries = new ArrayList<Entry>();

        for (int i = first; i < first + count; i++) {
            entries.add(blastResults.get(i).getEntry());
        }

        return (Iterator<BlastResult>) blastResults.subList(first, first + count).iterator();
    }

    @Override
    public int size() {
        return blastResults.size();
    }

    @Override
    public IModel<BlastResult> model(BlastResult blastResult) {
        return new Model<BlastResult>(blastResult);
    }

    @Override
    public void detach() {
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }
}
