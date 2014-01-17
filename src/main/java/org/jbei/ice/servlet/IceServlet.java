package org.jbei.ice.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jbei.ice.lib.account.SessionHandler;
import org.jbei.ice.lib.common.logging.Logger;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.servlet.action.Action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Hector Plahar
 */
public class IceServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Result result = new Result();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }

            HttpSession session = request.getSession(false);

            Request userRequest = gson.fromJson(sb.toString(), Request.class);
            String userId = session == null ? null : (String) session.getAttribute("userid");
            if (userRequest == null || userRequest.getAction() == null) {
                sendResponse(gson, new Result(false, "Invalid request", CodeConstant.REQUEST_INVALID), response);
                return;
            }

            switch (userRequest.getAction()) {
                case "retrieve":
                    PartData part = new PartData();
                    part.setName("From Server");
                    result.setData(part);

                    break;
                case "login":
//                    AccountAction accountAction = gson.fromJson(sb.toString(), AccountAction.class);
//                    if (accountAction == null || accountAction.getParams() == null) {
//                        result.setErrorMessage("Invalid action for login");
//                        break;
//                    }
//
//                    AccountTransfer accountTransfer = accountAction.getParams();
//                    AccountTransfer account;
//
//                    try {
//                        Authenticator authenticator = new Authenticator();
//                        account = authenticator.authenticate(accountTransfer.getUserId(),
//                                                             accountTransfer.getPassword());
//                    } catch (AuthenticationException ae) {
//                        Logger.error(ae);
//                        result.setErrorMessage("Error authenticating account credentials");
//                        break;
//                    }
//
//                    if (account == null) {
//                        result.setErrorMessage("Invalid login credentials");
//                        break;
//                    }
//                    result.setData(account);
//                    session = request.getSession(true);
//                    session.setAttribute("userid", accountTransfer.getUserId());
                    break;

                case "logout":
                    if (session != null) {
                        session.invalidate();
                        SessionHandler.invalidateSession(userId);
                    }
                    result.setSuccess(true);
                    result.setMessage("User " + userId + "successfully logged out");
                    break;

                default:
                    // create account does not require user to be logged in
                    boolean isRegister = "create".equals(userRequest.getAction())
                            && "account".equalsIgnoreCase(userRequest.getEntity());

                    // check if the user is logged in
                    if ((session == null || userId == null) && !isRegister) {
                        result.setErrorMessage("User is not logged in");
                        result.setCode(CodeConstant.SESSION_INVALID);
                        break;
                    }

                    Logger.info(userId + ": " + userRequest.toString());
                    // map the request to a specific action that can be handled
                    Action entityAction = ActionHelperFactory.getAction(gson, userRequest, sb.toString());
                    if (entityAction == null) {
                        result.setErrorMessage("System has no handler for '" + userRequest.getAction() + "'");
                        result.setCode(CodeConstant.REQUEST_INVALID);
                        break;
                    }
                    entityAction.setUserId(userId);
                    result = entityAction.getExecutor().execute();
            }
            sendResponse(gson, result, response);
        } catch (Exception e) {
            result.setSuccess(false);
            String errorMsg = e.getMessage();
            if (errorMsg == null)
                errorMsg = e.toString();
            result.setMessage("Error " + errorMsg);
            result.setCode(CodeConstant.SERVER_ERROR);
            Logger.error(errorMsg, e);
            sendResponse(gson, result, response);
        }
    }

    protected void sendResponse(Gson gson, Result result, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String message = gson.toJson(result);
        response.getOutputStream().println(message);
        response.getOutputStream().flush();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
