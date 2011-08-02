package org.jbei.ice.web.panels.adminpage;

import java.util.ArrayList;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.jbei.ice.lib.managers.EntryManager;
import org.jbei.ice.lib.managers.ManagerException;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.utils.IceXmlSerializer;
import org.jbei.ice.lib.utils.UtilityException;
import org.jbei.ice.web.common.ViewException;
import org.jbei.ice.web.pages.DownloadPage;

import edu.emory.mathcs.backport.java.util.Arrays;

public class AdminImportExportPanel extends Panel {

    private static final long serialVersionUID = 1L;

    ImportExportForm importExportForm = null;

    class ImportExportForm extends Form<Object> {
        private static final long serialVersionUID = 1L;

        private String exportList = null;
        private String importXml = null;
        private FileUpload importXmlFile = null;

        public ImportExportForm(String id) {
            super(id);
            setModel(new CompoundPropertyModel<Object>(this));
            setMultiPart(true);

            add(new FeedbackPanel("feedback"));

            // export fields
            add(new TextArea<String>("exportList"));
            add(new AjaxButton("submitExportListButton") {

                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    ImportExportForm parentForm = (ImportExportForm) form;
                    target.addComponent(parentForm);
                    Page page = ((ImportExportForm) getParent()).processExportList();
                    if (page != null) {
                        setResponsePage(page);
                    }

                }

            });

            // import fields
            add(new TextArea<String>("importXml"));
            add(new FileUploadField("importXmlFile"));
            add(new AjaxButton("submitImportXmlButton") {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                }

            });

        }

        @Override
        protected void onSubmit() {

        }

        public String getExportList() {
            return exportList;
        }

        public String getImportXml() {
            return importXml;
        }

        public FileUpload getImportXmlFile() {
            return importXmlFile;
        }

        @SuppressWarnings("unchecked")
        public Page processExportList() {
            String exportList = getExportList();
            ArrayList<String> startingSet = new ArrayList<String>();
            String[] commaSepList = exportList.split(",");
            for (String item : commaSepList) {
                String[] whitespaceSepList = item.trim().split("\\s+");
                startingSet.addAll(Arrays.asList(whitespaceSepList));
            }

            ArrayList<String> entryRecordIds = new ArrayList<String>();
            ArrayList<String> failedIds = new ArrayList<String>();
            for (String identifier : startingSet) {
                System.out.println(identifier);
                Entry item = null;

                try {
                    item = EntryManager.getByRecordId(identifier);
                    if (item == null) {
                        item = EntryManager.getByPartNumber(identifier);
                    }
                    if (item == null) {
                        try {
                            item = EntryManager.get(Long.parseLong(identifier));
                        } catch (NumberFormatException e) {
                            // pass
                        }
                    }
                    if (item == null) { // failed ones go here.
                        failedIds.add(identifier);
                    }
                    if (item != null) { //success go here.
                        entryRecordIds.add(item.getRecordId());
                        item = null;
                    }

                } catch (ManagerException e) {
                    failedIds.add(identifier);
                }

            }
            System.out.println(failedIds.size() + " unknown items");
            System.out.println(entryRecordIds.size() + " known items");

            if (failedIds.size() != 0) {
                importExportForm.error("Following identifiers were not recognized: " + failedIds);
                return null;
            } else {
                ArrayList<Entry> entries = new ArrayList<Entry>();

                for (String recordId : entryRecordIds) {
                    try {
                        entries.add(EntryManager.getByRecordId(recordId));

                    } catch (ManagerException e) {
                        error("Exception retrieving " + recordId);
                    }
                }
                String xmlString;
                try {
                    xmlString = IceXmlSerializer.serializeToJbeiXml(entries);
                } catch (UtilityException e) {
                    throw new ViewException(e);
                }
                return new DownloadPage("ice.xml", "text/xml", xmlString);
            }

        }

        public Page processImportXml() {

            System.out.println("submit export clicked: " + getImportXml());
            return null;
        }
    }

    public AdminImportExportPanel(String id) {
        super(id);
        importExportForm = new ImportExportForm("importExportForm");
        add(importExportForm);

    }

}
