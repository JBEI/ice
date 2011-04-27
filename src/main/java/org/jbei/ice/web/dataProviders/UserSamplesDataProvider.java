package org.jbei.ice.web.dataProviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jbei.ice.controllers.SampleController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Sample;
import org.jbei.ice.web.IceSession;
import org.jbei.ice.web.common.ViewException;

public class UserSamplesDataProvider extends SortableDataProvider<Sample> {
    private static final long serialVersionUID = 1L;

    private final Account account;
    private final ArrayList<Sample> samples = new ArrayList<Sample>();

    public UserSamplesDataProvider(Account account) {
        super();

        this.account = account;
        setSort("creationTime", false);
    }

    @Override
    public Iterator<Sample> iterator(int first, int count) {
        samples.clear();

        SampleController sampleController = new SampleController(IceSession.get().getAccount());

        try {
            ArrayList<Sample> results = sampleController.getSamplesByDepositor(account.getEmail(),
                first, count);

            samples.addAll(results);

            if (getSort() != null)
                sort();
        } catch (ControllerException e) {
            throw new ViewException(e);
        }

        return samples.iterator();
    }

    protected void sort() {
        Collections.sort(samples, new Comparator<Sample>() {

            @Override
            public int compare(Sample o1, Sample o2) {
                int result = 0;
                if (getSort() == null)
                    return result;

                String property = getSort().getProperty();

                PropertyModel<Comparable<Object>> model1 = new PropertyModel<Comparable<Object>>(
                        o1, property);
                PropertyModel<Comparable<Object>> model2 = new PropertyModel<Comparable<Object>>(
                        o2, property);

                if (model1.getObject() == null || model2.getObject() == null) {
                    return 0;
                }
                result = model1.getObject().compareTo(model2.getObject());

                if (!getSort().isAscending())
                    result *= -1;

                return result;
            }
        });
    }

    @Override
    public IModel<Sample> model(Sample object) {
        return new Model<Sample>(object);
    }

    @Override
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
