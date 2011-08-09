package org.jbei.ice.web.pages;

import java.util.ArrayList;

import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.IceXmlSerializer;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.web.common.ViewException;

public class EntriesXMLExportPage extends XMLExportPage {
    private ArrayList<Entry> entries;

    public EntriesXMLExportPage(ArrayList<Entry> entries) {
        super();

        this.entries = entries;
    }

    @Override
    public String getContent() {

        try {
            return IceXmlSerializer.serializeToJbeiXml(entries);

        } catch (UtilityException e) {
            throw new ViewException(e);
        }
    }
}
