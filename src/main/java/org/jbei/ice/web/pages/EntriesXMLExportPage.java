package org.jbei.ice.web.pages;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
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
            //return IceXmlSerializer.serializeToJbeiXml(entries).asXML();
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter xmlWriter;
            StringWriter stringWriter = new StringWriter();

            xmlWriter = new XMLWriter(stringWriter, format);
            xmlWriter.write(IceXmlSerializer.serializeToJbeiXml(entries));

            return stringWriter.toString();

        } catch (UtilityException e) {
            throw new ViewException(e);
        } catch (IOException e) {
            throw new ViewException(e);
        }
    }
}
