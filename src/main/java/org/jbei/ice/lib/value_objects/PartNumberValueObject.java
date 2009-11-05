package org.jbei.ice.lib.value_objects;

import org.jbei.ice.lib.models.Entry;

public interface PartNumberValueObject {

	public abstract int getId();

	public abstract void setId(int id);

	public abstract String getPartNumber();

	public abstract void setPartNumber(String partNumber);

	public abstract Entry getEntry();

	public abstract void setEntry(Entry entry);

}