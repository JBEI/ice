package org.jbei.ice.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.jbei.ice.lib.utils.UtilityException;

/**
 * IP address based filter.
 * It takes init-param "allow", as comma separated regex expressions of
 * ip addresses.
 * 
 * @author tham
 * 
 */
public class IceIpFilter implements Filter {
    protected Pattern[] allow = new Pattern[0];

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (isAllowed(request.getRemoteAddr())) {
            filterChain.doFilter(request, response);
        } else {
            HttpServletResponse response1 = (HttpServletResponse) response;
            response1.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        String allowedList = arg0.getInitParameter("allow");
        try {
            allow = calculateAllow(allowedList);
        } catch (UtilityException e) {
            throw new ServletException(e);
        }
    }

    /**
     * adapted from org.apache.catalina.valves.RequestFilterValve.process
     * 
     * @param property
     * @param request
     * @param response
     */
    protected boolean isAllowed(String property) {
        boolean result = false;
        for (int i = 0; i < allow.length; i++) {
            if (allow[i].matcher(property).matches()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * adapted from org.apache.catalina.valves.RequestFilterValve
     * 
     * @param allowedList
     * @return
     * @throws UtilityException
     */
    protected Pattern[] calculateAllow(String allowedList) throws UtilityException {
        if (allowedList == null) {
            return (new Pattern[0]);
        }
        allowedList.trim();
        if (allowedList.length() < 1) {
            return (new Pattern[0]);
        }
        allowedList += ",";

        ArrayList<Pattern> reAllowedList = new ArrayList<Pattern>();
        while (allowedList.length() > 0) {
            int comma = allowedList.indexOf(',');
            if (comma < 0)
                break;
            String pattern = allowedList.substring(0, comma).trim();
            try {
                reAllowedList.add(Pattern.compile(pattern));
            } catch (PatternSyntaxException e) {
                throw new UtilityException(e);
            }
            allowedList = allowedList.substring(comma + 1);
        }

        Pattern reArray[] = new Pattern[reAllowedList.size()];
        return (reAllowedList.toArray(reArray));
    }

}
