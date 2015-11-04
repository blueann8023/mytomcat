package com.lm.mytomcat.chap3.connector.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;

import com.lm.mytomcat.chap3.connector.RequestStream;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class HttpRequest implements HttpServletRequest {

    // be used for constructor
    @NonNull
    protected InputStream inputStream;

    // be used for requestline
    @Setter @Getter
    private String method;
    @Setter @Getter
    private String requestURI;
    @Setter @Getter
    private String queryString;
    @Setter @Getter
    private String protocol;

    // important info
    @Setter
    private String requestedSessionId;
    @Setter
    private boolean requestedSeesionURL;
    @Setter
    private boolean requestSessionCookie;

    // header info
    @Setter
    private String contentType;
    @Setter
    private int contentLength;

    // for servlet important obj
    protected Map<String, List<String>> headers = new HashMap<String, List<String>>();
    protected ArrayList cookies = new ArrayList();
    protected ParameterMap parameters = null;

    // The reader that has been returned by <code>getReader</code>, if any.
    protected ServletInputStream servletInputStream = null;
    @Getter
    protected BufferedReader reader = null;

    // for parseParam
    protected boolean parsed = false;

    //
    @Setter @Getter
    private String serverName;
    @Setter @Getter
    private int serverPort;
    @Setter
    private Socket socket;
    @Setter
    private InetAddress inetAddress;
    //
    @Setter
    protected String pathInfo = null;
    protected HashMap attributes = new HashMap();
    @Setter
    protected String authorization = null;
    @Setter
    protected String contextPath = "";
    protected static ArrayList empty = new ArrayList();

    protected SimpleDateFormat formats[] = { new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US), new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };

    protected void parseParameters() {
        log.info("parseParameters Start");
        if (parsed)
            return;
        ParameterMap results = parameters;
        if (results == null) {
            results = new ParameterMap();
        }
        results.setLocked(false);
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }

        // Parse any parameters specified in the query string
        String queryString = getQueryString();
        try {
            RequestUtil.parseParameters(results, queryString, encoding);
        } catch (UnsupportedEncodingException e) {
            ;
        }

        // Parse any parameter specified in the input stream
        String contentType = getContentType();
        if (contentType == null) {
            contentType = "";
        }
        int semicolonIndex = contentType.indexOf(';');
        if (semicolonIndex >= 0) {
            contentType = contentType.substring(0, semicolonIndex).trim();
        } else {
            contentType = contentType.trim();
        }

        if ("POST".equals(getMethod()) && (getContentLength() > 0) && "application/x-www-form-urlencoded".equals(contentType)) {
            try {
                int max = getContentLength();
                int len = 0;
                byte[] buf = new byte[getContentLength()];
                ServletInputStream is = getInputStream();
                while (len < max) {
                    int next = is.read(buf, len, max - len);
                    if (next < 0)
                        break;
                    len += next;
                }
                is.close();
                if (len < max) {
                    throw new RuntimeException("Content length mismatch");
                }
                RequestUtil.parseParameters(results, buf, encoding);
            } catch (UnsupportedEncodingException ue) {
                ;
            } catch (IOException e) {
                throw new RuntimeException("Content read fail");
            }
        }

        results.setLocked(true);
        parsed = true;
        parameters = results;
        log.info("parseParameters End");
    }

    public void addHeader(String name, String value) {
        name = name.toLowerCase();
        synchronized (headers) {
            ArrayList values = (ArrayList) headers.get(name);
            if (values == null) {
                values = new ArrayList();
                headers.put(name, values);
            }
            values.add(value);
        }
    }

    public void addCookie(Cookie cookie) {
        synchronized (cookies) {
            cookies.add(cookie);
        }
    }

    /* implement HttpServletRequest */
    public Object getAttribute(String name) {
        // TODO Auto-generated method stub
        synchronized (attributes) {
            return attributes.get(name);
        }
    }

    public Enumeration getAttributeNames() {
        // TODO Auto-generated method stub
        synchronized (attributes) {
            return (new Enumerator(attributes.keySet()));
        }
    }

    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        // TODO Auto-generated method stub

    }

    public int getContentLength() {
        // TODO Auto-generated method stub
        return contentLength;
    }

    public String getContentType() {
        // TODO Auto-generated method stub
        return contentType;
    }

    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        if (reader != null) {
            throw new IllegalStateException("getInputStream has been called");
        }
        if (servletInputStream == null) {
            servletInputStream = createInputStream();
        }
        return servletInputStream;
    }

    public ServletInputStream createInputStream() throws IOException {
        return (new RequestStream(this));
    }

    public InputStream getStream() {
        return inputStream;
    }

    public String getParameter(String name) {
        // TODO Auto-generated method stub
        parseParameters();
        String values[] = (String[]) parameters.get(name);
        if (values != null)
            return (values[0]);
        else
            return null;
    }

    public Enumeration getParameterNames() {
        // TODO Auto-generated method stub
        parseParameters();
        return (new Enumerator(parameters.keySet()));
    }

    public String[] getParameterValues(String name) {
        // TODO Auto-generated method stub
        parseParameters();
        String values[] = (String[]) parameters.get(name);
        if (values != null)
            return (values);
        else
            return null;
    }

    public Map getParameterMap() {
        // TODO Auto-generated method stub
        parseParameters();
        return (this.parameters);
    }

    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRemoteAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRemoteHost() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAttribute(String name, Object o) {
        // TODO Auto-generated method stub

    }

    public void removeAttribute(String name) {
        // TODO Auto-generated method stub

    }

    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getLocales() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isSecure() {
        // TODO Auto-generated method stub
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRealPath(String path) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getRemotePort() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLocalAddr() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getLocalPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getAuthType() {
        // TODO Auto-generated method stub
        return null;
    }

    public Cookie[] getCookies() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getDateHeader(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getHeader(String name) {
        // TODO Auto-generated method stub
        name = name.toLowerCase();
        synchronized (headers) {
          ArrayList values = (ArrayList) headers.get(name);
          if (values != null)
            return ((String) values.get(0));
          else
            return null;
        }
    }

    public Enumeration getHeaders(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getHeaderNames() {
        // TODO Auto-generated method stub
        synchronized (headers) {
            return (new Enumerator(headers.keySet()));
        }
    }

    public int getIntHeader(String name) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getPathInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPathTranslated() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getContextPath() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRemoteUser() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isUserInRole(String role) {
        // TODO Auto-generated method stub
        return false;
    }

    public Principal getUserPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRequestedSessionId() {
        // TODO Auto-generated method stub
        return null;
    }

    public StringBuffer getRequestURL() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getServletPath() {
        // TODO Auto-generated method stub
        return null;
    }

    public HttpSession getSession(boolean create) {
        // TODO Auto-generated method stub
        return null;
    }

    public HttpSession getSession() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isRequestedSessionIdValid() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {
        // TODO Auto-generated method stub
        return isRequestedSessionIdFromURL();
    }

}
