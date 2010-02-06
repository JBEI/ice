package org.jbei.ice.lib.permissions;

import java.util.LinkedHashSet;

import org.jbei.ice.lib.managers.AccountManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.managers.SampleManager;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.models.Sample;

/**
 * All samples are visible to everyone, and anyone can create samples. Only edit and delete require
 * permission.
 * 
 * Other methods are provided here for completeness
 * 
 * @author tham
 * 
 */
public class AuthenticatedSampleManager {
    public static Sample get(int id) throws ManagerException {
        return SampleManager.get(id);
    }

    public static LinkedHashSet<Sample> get(Entry entry) throws ManagerException {
        return SampleManager.get(entry);
    }

    public static LinkedHashSet<Sample> getByAccount(Account account, int offset, int limit)
            throws ManagerException {
        return SampleManager.getByAccount(account, offset, limit);
    }

    public static int getByAccountCount(Account account) throws ManagerException {
        return SampleManager.getByAccountCount(account);
    }

    public static boolean hasSample(Entry entry) {
        return SampleManager.hasSample(entry);
    }

    public static int getNumberOfSamples(Entry entry) {
        return SampleManager.getNumberOfSamples(entry);
    }

    public static Sample create(Sample sample) throws ManagerException {
        return SampleManager.create(sample);
    }

    public static Sample save(Sample sample, String sessionKey) throws ManagerException {
        if (sample.getId() == 0) {
            // This is a new sample, which means anyone is allowed to create.
            return SampleManager.save(sample);
        } else {
            try {
                Sample oldSample = SampleManager.get(sample.getId());
                Account user = AccountManager.getAccountByAuthToken(sessionKey);
                if (oldSample.getDepositor().equals(user.getEmail())) {
                    return SampleManager.save(sample);
                } else {
                    throw new PermissionException("save not permitted");
                }
            } catch (ManagerException e) {
                throw new PermissionException("save failed");
            }
        }
    }

    public static void delete(Sample sample, String sessionKey) throws ManagerException {
        Sample oldSample = SampleManager.get(sample.getId());
        Account user = AccountManager.getAccountByAuthToken(sessionKey);
        if (oldSample.getDepositor().equals(user.getEmail())) {
            SampleManager.delete(sample);
        } else {
            throw new PermissionException("save not permitted");
        }
    }
}
