package org.jbei.ice.server.servlet;

import org.apache.commons.io.IOUtils;
import org.jbei.ice.controllers.ControllerFactory;
import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.entry.model.Entry;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.lib.utils.IceXlsSerializer;
import org.jbei.ice.lib.utils.IceXmlSerializer;
import org.jbei.ice.lib.utils.UtilityException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class EntryExportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

        Set<String> typeSet = new HashSet<>();
        List<Entry> entries = retrieveEntries(account, commaSeparated, typeSet);
        if (entries == null || entries.isEmpty())
            return;

        switch (type.toLowerCase()) {
            case "xml":
                exportXML(account, entries, response);
                break;

            case "excel":
            default:
                exportExcel(entries, typeSet, response);
                break;
        }
    }

    private List<Entry> retrieveEntries(Account account, String commaSeparated, Set<String> types) {
        LinkedList<Entry> entries = new LinkedList<>();
        String[] idStrs = commaSeparated.split(",");

        for (String idStr : idStrs) {
            Entry entry;
            try {
                long id = Long.decode(idStr.trim());
                entry = ControllerFactory.getEntryController().get(account, id);
            } catch (NumberFormatException nfe) {
                Logger.error("Could not convert string id to long : " + idStr);
                continue;
            } catch (ControllerException e) {
                Logger.error(e);
                continue;
            }

            if (entry != null) {
                types.add(entry.getRecordType().toUpperCase());
                entries.add(entry);
            }
        }
        return entries;
    }

    private void exportXML(Account account, List<Entry> entries, HttpServletResponse response) {
        try {
            String xmlDocument = IceXmlSerializer.serializeToJbeiXml(account, entries);

            // write to file
            String saveName = "data.xml";
            byte[] bytes = xmlDocument.getBytes();
            response.setContentType("text/xml");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=" + saveName);
            IOUtils.write(bytes, response.getOutputStream());
        } catch (IOException | UtilityException e) {
            Logger.error(e);
        }
    }

    private void exportExcel(List<Entry> entries, Set<String> types, HttpServletResponse response) {
        try {
            String data;
            try {
                data = IceXlsSerializer.serialize(entries, new TreeSet<>(types));
            } catch (ControllerException e) {
                Logger.error(e);
                return;
            }

            // write to file
            String saveName = "data.xls";
            byte[] bytes = data.getBytes();
            response.setContentType("application/vnd.ms-excel");
            response.setContentLength(bytes.length);
            response.setHeader("Content-Disposition", "attachment;filename=" + saveName);
            IOUtils.write(bytes, response.getOutputStream());
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

                AccountController controller = ControllerFactory.getAccountController();
                return controller.getAccountBySessionKey(sid);
            }
        }
        return null;
    }
}
