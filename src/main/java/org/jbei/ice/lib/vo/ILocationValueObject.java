package org.jbei.ice.lib.vo;

import java.util.Date;

import org.jbei.ice.lib.models.Sample;

public interface ILocationValueObject {

    public abstract int getId();

    public abstract void setId(int id);

    public abstract Sample getSample();

    public abstract void setSample(Sample sample);

    public abstract String getLocation();

    public abstract void setLocation(String location);

    public abstract String getBarcode();

    public abstract void setBarcode(String barcode);

    public abstract String getNotes();

    public abstract void setNotes(String notes);

    public abstract String getWells();

    public abstract void setWells(String wells);

    public abstract int getnColumns();

    public abstract void setnColumns(int nColumns);

    public abstract int getnRows();

    public abstract void setnRows(int nRows);

    public abstract Date getCreationTime();

    public abstract void setCreationTime(Date creationTime);

    public abstract Date getModificationTime();

    public abstract void setModificationTime(Date modificationTime);

}