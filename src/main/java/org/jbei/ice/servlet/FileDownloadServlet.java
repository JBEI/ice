package org.jbei.ice.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.ConfigurationKey;
import org.jbei.ice.lib.utils.Utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Servlet for serving the different kinds of files
 * available on gd-ice. Requires a valid session id as
 * a parameter // TODO : currently only being used for sbol visual download
 *
 * @author Hector Plahar
 */
public class FileDownloadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String SBOL_VISUAL_TYPE = "sbol_visual";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileId = request.getParameter("id");
        String type = request.getParameter("type");
        String sid = request.getParameter("sid");
        File file = null;

        if (SBOL_VISUAL_TYPE.equalsIgnoreCase(type)) {
            getSBOLVisualType(fileId, response);
            return;
        }

        // check for null file
        if (file == null) {
            Logger.info(FileDownloadServlet.class.getSimpleName() + ": attempt failed");
            return;
        }

        response.setContentType("application/octet-stream");
        response.setContentLength((int) file.length());
        IOUtils.copy(new FileInputStream(file), response.getOutputStream());
    }

    private void getSBOLVisualType(String fileId, HttpServletResponse response) {
        if (StringUtils.isEmpty(fileId))
            return;

        response.setContentType("image/png");
        String tmpDir = Utils.getConfigValue(ConfigurationKey.TEMPORARY_DIRECTORY);
        File file = Paths.get(tmpDir, fileId).toFile();
        if (file.exists() && file.canRead()) {
            response.setContentLength((int) file.length());
            try {
                IOUtils.copy(new FileInputStream(file), response.getOutputStream());
            } catch (IOException ioe) {
                Logger.error(ioe);
            }
        }
    }
}
