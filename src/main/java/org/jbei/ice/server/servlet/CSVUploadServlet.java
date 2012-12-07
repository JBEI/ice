package org.jbei.ice.server.servlet;

import java.io.InputStreamReader;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.jbei.ice.controllers.common.ControllerException;
import org.jbei.ice.lib.account.AccountController;
import org.jbei.ice.lib.account.model.Account;
import org.jbei.ice.lib.utils.Utils;
import org.jbei.ice.shared.dto.ConfigurationKey;

import au.com.bytecode.opencsv.CSVReader;
import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;
import org.apache.commons.fileupload.FileItem;

/**
 * @author Hector Plahar
 */
public class CSVUploadServlet extends UploadAction {
    private static final long serialVersionUID = 1L;

    private Account isLoggedIn(AccountController controller, Cookie[] cookies) throws ControllerException {
        final String COOKIE_NAME = Utils.getConfigValue(ConfigurationKey.COOKIE_NAME);

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String sid = cookie.getValue();
                if (sid == null || sid.isEmpty())
                    return null;

                if (!AccountController.isAuthenticated(sid))
                    return null;

                return controller.getAccountBySessionKey(sid);
            }
        }
        return null;
    }

    @Override
    public String executeAction(HttpServletRequest request, List<FileItem> sessionFiles) throws UploadActionException {
        // TODO : validate user
        if (sessionFiles.isEmpty())
            return "No files";

        FileItem fileItem = sessionFiles.get(0);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(fileItem.getInputStream());
            try (CSVReader csvReader = new CSVReader(inputStreamReader)) {
                String[] line = csvReader.readNext();
                for (String l : line) {
                    System.out.print(l + " ");
                }
                System.out.println();
            }
        } catch (Exception c) {
            return c.getMessage();
        }

        removeSessionFileItems(request);
        return "";
    }
}
