package org.jbei.ice.storage;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.dto.entry.CustomField;
import org.jbei.ice.dto.entry.EntryType;
import org.jbei.ice.dto.entry.PartData;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.ParameterModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Factory object for converting data transfer objects to model objects
 *
 * @author Hector Plahar
 */
public class InfoToModelFactory {

    public static Entry infoToEntry(PartData info) {
        EntryType type = info.getType();
        Entry entry = new Entry();

        // common fields
        if (StringUtils.isEmpty(info.getRecordId()))
            entry.setRecordId(UUID.randomUUID().toString());
        else
            entry.setRecordId(info.getRecordId());

        entry.setVersionId(entry.getRecordId());
        if (info.getCreationTime() == 0)
            entry.setCreationTime(new Date());
        else
            entry.setCreationTime(new Date(info.getCreationTime()));

        entry.setModificationTime(entry.getCreationTime());

        if (info.getVisibility() != null)
            entry.setVisibility(info.getVisibility().getValue());
        return entry;
    }

    private static List<ParameterModel> getParameters(ArrayList<CustomField> infos, Entry entry) {
        List<ParameterModel> parameters = new ArrayList<>();

        if (infos == null)
            return parameters;

        for (CustomField info : infos) {
            ParameterModel param = new ParameterModel();
            param.setEntry(entry);
            param.setKey(info.getName());
            param.setValue(info.getValue());
            parameters.add(param);
        }
        return parameters;
    }
}
