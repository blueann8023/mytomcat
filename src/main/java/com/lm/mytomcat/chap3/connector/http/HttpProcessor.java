package com.lm.mytomcat.chap3.connector.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;

import com.lm.mytomcat.chap3.ServletProcessor;
import com.lm.mytomcat.chap3.StaticResourceProcessor;

@Slf4j
@RequiredArgsConstructor
public class HttpProcessor {

    @NonNull
    private HttpConnector connector;

    private HttpRequest request;

    // be uesed for parseRequestLine
    private HttpRequestLine requestLine = new HttpRequestLine();

    private HttpResponse response;

    /*
     * protected String method = null; protected String queryString = null;
     */

    protected StringManager sm = StringManager.getManager("com.lm.mytomcat.chap3.connector.http");

    public void process(Socket socket) {
        SocketInputStream input = null;
        OutputStream output = null;
        try {
            input = new SocketInputStream(socket.getInputStream(), 2048);
            log.debug("processor process input {}", input);
            output = socket.getOutputStream();

            request = new HttpRequest(input);
            log.debug("processor process request {}", request);
            parseRequest(input, output);

            parseHeaders(input);

            response = new HttpResponse(output);
            response.setRequest(request);
            response.setHeader("Server", "Lm Servlet Container");

            log.debug("processor process request.getRequestURI() {}", request.getRequestURI());
            if (request.getRequestURI().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseHeaders(SocketInputStream input) throws IOException, ServletException {
        log.info("processor process parseHeaders Start");
        while (true) {
            log.debug("processor process while Start");
            HttpHeader header = new HttpHeader();
            // read the next header
            input.readHeader(header);
            log.debug("processor process while Start");
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    log.info("processor process parseHeaders End");
                    return;
                } else {
                    throw new ServletException(sm.getString("httpProcessor.parseHeaders.colon"));
                }
            }
            String name = new String(header.name, 0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);
            request.addHeader(name, value);

            // do something for some headers ,ignore others.
            if (name.equals("cookie")) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    // override anything requested in the URL
                    if (cookies[i].getName().equals("jsessionid")) {
                        if (!request.isRequestedSessionIdFromCookie()) {
                            request.setRequestedSessionId(cookies[i].getValue());
                            request.setRequestedSeesionURL(false);
                            request.setRequestSessionCookie(true);
                        }
                    }
                    request.addCookie(cookies[i]);
                }
            } else if (name.equals("content-length")) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                } catch (Exception e) {
                    throw new ServletException(sm.getString("httpProcessor.parseHeaders.contentLength"));
                }
                request.setContentLength(n);
            } else if (name.equals("content-type")) {
                request.setContentType(value);
            } else {
            }
            log.debug("processor process while End");
        }
    }

    private void parseRequest(SocketInputStream input, OutputStream output) throws ServletException, IOException {

        log.info("processor process parseRequest Start");
        input.readHttpRequestLine(requestLine);

        String method = new String(requestLine.method, 0, requestLine.methodEnd);
        log.debug("processor process parseRequest method {}", method);
        String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);
        log.debug("processor process parseRequest protocol {}", protocol);
        String uri = null;

        // validate the incoming request line
        if (method.length() < 0) {
            throw new ServletException("Miss Http request method");
        } else if (requestLine.uriEnd < 0) {
            throw new ServletException("Miss Http request URI");
        }

        int questionIndex = requestLine.indexOf("?");
        if (questionIndex >= 0) {
            request.setQueryString(new String(requestLine.uri, questionIndex + 1, requestLine.uriEnd - questionIndex - 1));
            uri = new String(requestLine.uri, 0, questionIndex);
        } else {
            request.setQueryString(null);
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        }
        log.debug("processor process parseRequest queryString {}", request.getQueryString());
        // checking for an absolute URI(with the http protocol)
        log.debug("processor process parseRequest uri {}", uri);
        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            // Parseing out protocol and host name;
            if (pos != -1) {
                pos = uri.indexOf("/", pos + 3);
                if (pos == -1) {
                    uri = "";
                } else {
                    uri = uri.substring(pos);
                }
            }
        }

        // Parse any requested session ID out of the Request URI
        String match = ";jsessionid=";
        int semicolonIndex = uri.indexOf(match);
        if (semicolonIndex >= 0) {
            String rest = uri.substring(semicolonIndex + match.length());
            int semicolon2Index = rest.indexOf(";");
            if (semicolon2Index >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2Index));
                rest = rest.substring(semicolon2Index);
            } else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSeesionURL(true);
            uri = uri.substring(0, semicolonIndex) + rest;
        } else {
            request.setRequestedSessionId(null);
            request.setRequestedSeesionURL(false);
        }

        //
        String nomalizeUri = normalize(uri);
        log.debug("processor process parseRequest nomalizeUri {}", nomalizeUri);
        // set corresponding request properties
        request.setMethod(method);
        request.setProtocol(protocol);
        if (nomalizeUri != null) {
            request.setRequestURI(nomalizeUri);
        } else {
            request.setRequestURI(uri);
            throw new ServletException("Invalid URI: '" + uri + "'");
        }

        log.info("processor process parseRequest End");
    }

    private String normalize(String uri) {
        if (uri == null)
            return null;
        // Create a place for the normalized path
        String normalized = uri;

        // Normalize "/%7E" and "/%7e" at the beginning to "/~"
        if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
            normalized = "/~" + normalized.substring(4);

        // Prevent encoding '%', '/', '.' and '\', which are special reserved
        // characters
        if ((normalized.indexOf("%25") >= 0) || (normalized.indexOf("%2F") >= 0) || (normalized.indexOf("%2E") >= 0)
                || (normalized.indexOf("%5C") >= 0) || (normalized.indexOf("%2f") >= 0) || (normalized.indexOf("%2e") >= 0)
                || (normalized.indexOf("%5c") >= 0)) {
            return null;
        }

        if (normalized.equals("/."))
            return "/";

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) + normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) + normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null); // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
        }

        // Declare occurrences of "/..." (three or more dots) to be invalid
        // (on some Windows platforms this walks the directory tree!!!)
        if (normalized.indexOf("/...") >= 0)
            return (null);

        // Return the normalized path that we have completed
        return (normalized);

    }
}
