package org.jbei.ice.server.servlet;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.EntryController;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.permissions.PermissionException;
import org.jbei.ice.lib.utils.IceXlsSerializer;
import org.jbei.ice.lib.utils.IceXmlSerializer;
import org.jbei.ice.lib.utils.UtilityException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class EntryExportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String XML_EXPORT = "xml";
    private static final String EXCEL_EXPORT = "excel";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Logger.info(EntryExportServlet.class.getSimpleName() + ": attempt to download file");
        Account account;

        try {
            account = isLoggedIn(request.getCookies());
        } catch (ControllerException ce) {
            Logger.error(ce);
            String url = request.getRequestURL().toString();
            String path = request.getServletPath();
            url = url.substring(0, url.indexOf(path));
            response.sendRedirect(url);
            Logger.info(EntryExportServlet.class.getSimpleName()
                                + ": authentication failed. Redirecting user to " + url);
            return;
        }

        if (account == null)
            return;

        String type = request.getParameter("type");
        String commaSeparated = request.getParameter("entries");
        Logger.info(EntryExportServlet.class.getSimpleName() + ": user = " + account.getEmail()
                            + ", type = " + type + ", entries = " + commaSeparated);

        EntryController controller = new EntryController();
        ArrayList<Entry> entries = retrieveEntries(account, commaSeparated, controller);
        if (entries == null || entries.isEmpty())
            return;

        if (XML_EXPORT.equalsIgnoreCase(type)) {
            exportXML(account, entries, response);
        } else if (EXCEL_EXPORT.equalsIgnoreCase(type)) {
            exportExcel(entries, response, controller);
        }
    }

    private ArrayList<Entry> retrieveEntries(Account account, String commaSeparated,
            EntryController controller) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        String[] idStrs = commaSeparated.split(",");

        for (String idStr : idStrs) {
            Entry entry = null;
            try {
                long id = Long.decode(idStr.trim());
                entry = controller.get(account, id);
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

    private void exportXML(Account account, ArrayList<Entry> entries, HttpServletResponse response) {
        try {
            String xmlDocument = IceXmlSerializer.serializeToJbeiXml(account, entries);

            // write to file
            String saveName = "data.xml";

            byte[] bytes = xmlDocument.getBytes();
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);

            response.setContentType("text/xml");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=" + saveName);

            OutputStream os = response.getOutputStream();
            DataInputStream is = new DataInputStream(byteInputStream);

            int read = 0;

            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
            os.close();

        } catch (IOException e) {
            Logger.error(e);
        } catch (UtilityException e) {
            Logger.error(e);
        }
    }

    private void exportExcel(ArrayList<Entry> entries, HttpServletResponse response,
            EntryController controller) {
        try {
            String data = IceXlsSerializer.serialize(controller, entries);

            // write to file
            String saveName = "data.xls";
            byte[] bytes = data.getBytes();
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(bytes);

            response.setContentType("application/vnd.ms-excel");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=" + saveName);

            OutputStream os = response.getOutputStream();
            DataInputStream is = new DataInputStream(byteInputStream);

            int read = 0;
            while ((read = is.read(bytes)) != -1) {
                os.write(bytes, 0, read);
            }
            os.flush();
            os.close();

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

                AccountController controller = new AccountController();
                return controller.getAccountBySessionKey(sid);
            }
        }
        return null;
    }
}
