package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Sample;

public class UserSamplesDataProvider extends SortableDataProvider<Sample> {
    private static final long serialVersionUID = 1L;

    private Account account;
    private ArrayList<Sample> samples = new ArrayList<Sample>();

    public UserSamplesDataProvider(Account account) {
        super();

        this.account = account;
    }

    public Iterator<Sample> iterator(int first, int count) {
        samples.clear();

        try {
            LinkedHashSet<Sample> results = SampleManager.getByAccount(account, first, count);

            for (Sample sample : results) {
                samples.add(sample);
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return samples.iterator();
    }

    public IModel<Sample> model(Sample object) {
        return new Model<Sample>(object);
    }

    public int size() {
        try {
            return SampleManager.getByAccountCount(account);
        } catch (ManagerException e) {
            return 0;
        }
    }

    public ArrayList<Sample> getSamples() {
        return samples;
    }
}
