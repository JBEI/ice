package org.jbei.ice.server.servlet;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.jbei.ice.controllers.AccountController;
import org.jbei.ice.controllers.EntryController;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.models.Account;
import org.jbei.ice.lib.models.Entry;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.IceXmlSerializer;
import org.jbei.ice.lib.utils.JbeirSettings;
import org.jbei.ice.lib.utils.UtilityException;

public class EntryExportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String XML_EXPORT = "xml";
    private static final String EXCEL_EXPORT = "excel";
    private static final int BYTES_DOWNLOAD = 1024;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Logger.info(EntryExportServlet.class.getSimpleName() + ": attempt to download file");
        Account account = null;

        try {
            account = isLoggedIn(request.getCookies());
        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            response.sendRedirect(url);
            Logger.info(EntryExportServlet.class.getSimpleName()
                    + ": authenication failed. Redirecting user to " + url);
            return;
        }

        if (account == null)
            return;

        String type = request.getParameter("type");
        String commaSeparated = request.getParameter("entries");
        Logger.info(EntryExportServlet.class.getSimpleName() + ": user = " + account.getEmail()
                + ", type = " + type + ", entries = " + commaSeparated);

        EntryController controller = new EntryController(account);
        ArrayList<Entry> entries = retrieveEntries(commaSeparated, controller);
        if (entries == null || entries.isEmpty())
            return;

        if (XML_EXPORT.equalsIgnoreCase(type)) {
            exportXML(entries, response);
        } else if (EXCEL_EXPORT.equalsIgnoreCase(type)) {

        }
    }

    private ArrayList<Entry> retrieveEntries(String commaSeparated, EntryController controller) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        String[] idStrs = commaSeparated.split(",");

        for (String idStr : idStrs) {
            Entry entry = null;
            try {
                long id = Long.decode(idStr);
                entry = controller.get(id);
            } catch (NumberFormatException nfe) {
                Logger.error("Could not convert string id to long : " + idStr);
                continue;
            } catch (ControllerException e) {
                Logger.error(e);
                continue;
            } catch (PermissionException e) {
                Logger.error(e);
                continue;
            }

            if (entry != null)
                entries.add(entry);
        }
        return entries;
    }

    private void exportXML(ArrayList<Entry> entries, HttpServletResponse response) {
        try {
            String xmlDocument = IceXmlSerializer.serializeToJbeiXml(entries);

            // write to file
            String tmpDir = JbeirSettings.getSetting("TEMPORARY_DIRECTORY");
            String saveName = UUID.randomUUID().toString() + ".xml";
            File file = new File(tmpDir + File.separator + saveName);
            FileUtils.writeStringToFile(file, xmlDocument);
            Logger.info("Wrote contents to file " + file.getAbsolutePath());

            response.setContentType("application/octet-stream");
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment;filename=" + saveName);

            OutputStream os = response.getOutputStream();
            DataInputStream is = new DataInputStream(new FileInputStream(file));

            int read = 0;
            byte[] bytes = new byte[BYTES_DOWNLOAD];

            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
            os.close();

        } catch (UtilityException e) {
            Logger.error(e);
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    private Account isLoggedIn(Cookie[] cookies) throws ControllerException {
        for (Cookie cookie : cookies) {
            if ("gd-ice".equals(cookie.getName())) {
                String sid = cookie.getValue();
                if (sid == null || sid.isEmpty())
                    return null;

                if (!AccountController.isAuthenticated(sid))
                    return null;
                return AccountController.getAccountBySessionKey(sid);
            }
        }
        return null;
    }
}
