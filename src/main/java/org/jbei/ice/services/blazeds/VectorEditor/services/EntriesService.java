package org.jbei.ice.services.blazeds.VectorEditor.services;

import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;

public class EntriesService {
	public Entry getEntry(String entryId) {
		Entry entry = null;
		try {
			entry = EntryManager.getByRecordId(entryId);
		} catch (ManagerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entry;
	}
}
