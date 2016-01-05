

package org.jbei.ice.lib.entry.sample;

import org.jbei.ice.lib.access.Authorization;
import org.jbei.ice.lib.entry.EntryAuthorization;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.model.Sample;

/**
 * @author Hector Plahar
 */
public class SampleAuthorization extends Authorization<Sample> {

    private final EntryAuthorization entryAuthorization;

    public SampleAuthorization() {
        super(DAOFactory.getSampleDAO());
        entryAuthorization = new EntryAuthorization();
    }

    @Override
    protected String getOwner(Sample sample) {
        return sample.getDepositor();
    }

    /**
     * Determines if the specified user has write privileges.
     * This is based on ownership of the sample itself (creator) or
     * the entry associated with the sample
     *
     * @param userId unique user identifier
     * @param sample sample being checked
     * @return true is user has write privileges on the sample, false otherwise
     */
    @Override
    public boolean canWrite(String userId, Sample sample) {
        if (isAdmin(userId))
            return true;

        String owner = getOwner(sample);
        if (userId.equalsIgnoreCase(owner))
            return true;

        // check if there is an entry
        return sample.getEntry() != null && entryAuthorization.canWriteThoroughCheck(userId, sample.getEntry());
    }
}
