package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

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

        SampleController sampleController = new SampleController(IceSession.get().getAccount());

        try {
            ArrayList<Sample> results = sampleController.getSamplesByDepositor(account.getEmail(),
                    first, count);

            for (Sample sample : results) {
                samples.add(sample);
            }
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return samples.iterator();
    }

    public IModel<Sample> model(Sample object) {
        return new Model<Sample>(object);
    }

    public int size() {
        int numberOfSamples = 0;

        SampleController sampleController = new SampleController(IceSession.get().getAccount());

        try {
            numberOfSamples = sampleController.getNumberOfSamplesByDepositor(account.getEmail());
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return numberOfSamples;
    }

    public ArrayList<Sample> getSamples() {
        return samples;
    }
}
