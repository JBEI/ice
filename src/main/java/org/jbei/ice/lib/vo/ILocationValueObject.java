package org.jbei.ice.lib.vo;

import java.util.Date;

import org.jbei.ice.lib.models.Sample;

public interface ILocationValueObject {
    int getId();

    void setId(int id);

    Sample getSample();

    void setSample(Sample sample);

    String getLocation();

    void setLocation(String location);

    String getBarcode();

    void setBarcode(String barcode);

    String getNotes();

    void setNotes(String notes);

    String getWells();

    void setWells(String wells);

    int getnColumns();

    void setnColumns(int nColumns);

    int getnRows();

    void setnRows(int nRows);

    Date getCreationTime();

    void setCreationTime(Date creationTime);

    Date getModificationTime();

    void setModificationTime(Date modificationTime);
}