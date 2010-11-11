package org.jbei.ice.web.panels;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.lang.Bytes;
import org.jbei.ice.web.common.ViewException;

public abstract class FileUploadFormProcessingPanel extends Panel {

    private static final long serialVersionUID = 1L;

    public FileUploadFormProcessingPanel(String id) {

        super(id);
        FileUploadForm form = new FileUploadForm("file_upload", true);
        add(form);
    }

    protected abstract void processFile(File file) throws IOException;

    private class FileUploadForm extends Form<Void> {
        private static final long serialVersionUID = 1L;
        private FileUploadField field;

        public FileUploadForm(String id, boolean isMultiPart) {
            super(id);
            setMultiPart(true);
            field = new FileUploadField("file_input");
            add(field);
            setMaxSize(Bytes.kilobytes(10000));
        }

        @Override
        protected void onSubmit() {
            final FileUpload upload = field.getFileUpload();
            if (upload != null) {
                File file;
                try {
                    file = upload.writeToTempFile();
                    processFile(file);
                    upload.delete(); // delete temp file
                } catch (IOException e) {
                    throw new ViewException(e);
                }
            }
        }
    }

}
