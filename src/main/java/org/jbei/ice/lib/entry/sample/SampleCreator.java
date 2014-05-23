package org.jbei.ice.lib.entry.sample;

import java.util.Calendar;
import java.util.Date;

import org.jbei.ice.lib.entry.sample.model.Sample;
import org.jbei.ice.lib.utils.Utils;

/**
 * Handles creation of sample and associated actions
 *
 * @author Hector Plahar
 */
public class SampleCreator {

    /**
     * Create a {@link org.jbei.ice.lib.entry.sample.model.Sample} object.
     * <p/>
     * Generates the UUID and the time stamps.
     *
     * @param label     display label for sample
     * @param depositor name of the depositor
     * @param notes     associated notes
     * @return {@link org.jbei.ice.lib.entry.sample.model.Sample}
     */
    public static Sample createSampleObject(String label, String depositor, String notes) {
        String uuid = Utils.generateUUID();
        Date creationTime = Calendar.getInstance().getTime();

        Sample sample = new Sample();
        sample.setLabel(label);
        sample.setDepositor(depositor);
        sample.setNotes(notes);
        sample.setUuid(uuid);
        sample.setCreationTime(creationTime);
        sample.setModificationTime(null);
        return sample;
    }
}
