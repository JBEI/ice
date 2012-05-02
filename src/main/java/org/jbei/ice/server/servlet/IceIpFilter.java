package org.jbei.ice.server.servlet;

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

/**
 * IP address based Servlet filter. Limits access to the gd-ice instance to specified ip addresses.
 * <p>
 * It takes init-param "allow", as comma separated regex expressions of ip addresses.
 * 
 * @author Timothy Ham
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
        allow = calculateAllow(allowedList);
    }

    /**
     * adapted from org.apache.catalina.valves.RequestFilterValve.process
     * 
     * @param property
     */
    protected boolean isAllowed(String property) {
        boolean result = false;
        for (Pattern element : allow) {
            if (element.matcher(property).matches()) {
                result = true;
            }
        }
        return result;
    }

    /**
     * adapted from org.apache.catalina.valves.RequestFilterValve
     * 
     * @param allowedList
     * @return {@link Pattern} array.
     * @throws ServletException
     */
    protected Pattern[] calculateAllow(String allowedList) throws ServletException {
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
            if (comma < 0) {
                break;
            }
            String pattern = allowedList.substring(0, comma).trim();
            try {
                reAllowedList.add(Pattern.compile(pattern));
            } catch (PatternSyntaxException e) {
                throw new ServletException(e);
            }
            allowedList = allowedList.substring(comma + 1);
        }

        Pattern reArray[] = new Pattern[reAllowedList.size()];
        return (reAllowedList.toArray(reArray));
    }
}
