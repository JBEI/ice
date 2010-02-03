package org.jbei.ice.web.pages;

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.protocol.http.WebResponse;

public abstract class ExportPage extends Page {
    protected void onRender(MarkupStream markupStream) {
        doExport();
    }

    public abstract String getMimeType();

    public abstract String getContent();

    public String getFileName() {
        return null;
    }

    protected int doExport() {
        String mimeType = getMimeType();
        String content = getContent();
        String filename = getFileName();

        return writeExport(mimeType, content, filename);
    }

    private int writeExport(String mimeType, String content, String filename) {
        WebResponse response = (WebResponse) getResponse();
        HttpServletResponse servletResponse = response.getHttpServletResponse();

        // response can't be already committed at this time
        if (servletResponse.isCommitted()) {
            throw new WicketRuntimeException(
                    "HTTP response already committed. Can not change that any more");
        }

        // if cache is disabled using http header, export will not work.
        // Try to remove bad headers overwriting them, since there is no way to
        // remove a single header and reset()
        // could remove other "useful" headers like content encoding
        if (servletResponse.containsHeader("Cache-Control")) {
            servletResponse.setHeader("Cache-Control", "public");
        }
        if (servletResponse.containsHeader("Expires")) {
            servletResponse.setHeader("Expires", "Thu, 01 Dec 2069 16:00:00 GMT");
        }
        if (servletResponse.containsHeader("Pragma")) {
            // Pragma: no-cache
            // http 1.0 equivalent of Cache-Control: no-cache
            // there is no "Cache-Control: public" equivalent, so just try to
            // set it to an empty String (note
            // this is NOT a valid header)
            servletResponse.setHeader("Pragma", "");
        }

        try {
            servletResponse.resetBuffer();
        } catch (Exception e) {
            throw new WicketRuntimeException("Unable to reset HTTP response", e);
        }

        response.setContentType(mimeType);

        if ((filename != null) && (filename.trim().length() > 0)) {
            servletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + filename
                    + "\"");
        }

        response.write(content);
        return 0;
    }
}
