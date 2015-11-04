package com.lm.mytomcat.chap3.connector.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.util.CookieTools;

import com.lm.mytomcat.chap3.connector.ResponseStream;
import com.lm.mytomcat.chap3.connector.ResponseWriter;

@Slf4j
@RequiredArgsConstructor
public class HttpResponse implements HttpServletResponse {
    @Setter
    HttpRequest request;
    @NonNull
    OutputStream outputStream;
    PrintWriter printWriter;

    //
    protected byte[] buffer = new byte[Constants.BUFFER_SIZE];
    protected int bufferCount = 0;
    protected boolean committed = false;
    @Getter
    protected int contentCount = 0;
    @Getter
    @Setter
    protected int contentLength = -1;//
    @Getter
    @Setter
    protected String contentType = null;//
    @Setter
    protected String characterEncoding = null;//

    protected ArrayList cookies = new ArrayList();//
    protected HashMap headers = new HashMap();//

    protected final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    protected String message = getStatusMessage(HttpServletResponse.SC_OK);
    protected int status = HttpServletResponse.SC_OK;

    protected String getStatusMessage(int status) {
        switch (status) {
        case SC_OK:
            return ("OK");
        case SC_ACCEPTED:
            return ("Accepted");
        case SC_BAD_GATEWAY:
            return ("Bad Gateway");
        case SC_BAD_REQUEST:
            return ("Bad Request");
        case SC_CONFLICT:
            return ("Conflict");
        case SC_CONTINUE:
            return ("Continue");
        case SC_CREATED:
            return ("Created");
        case SC_EXPECTATION_FAILED:
            return ("Expectation Failed");
        case SC_FORBIDDEN:
            return ("Forbidden");
        case SC_GATEWAY_TIMEOUT:
            return ("Gateway Timeout");
        case SC_GONE:
            return ("Gone");
        case SC_HTTP_VERSION_NOT_SUPPORTED:
            return ("HTTP Version Not Supported");
        case SC_INTERNAL_SERVER_ERROR:
            return ("Internal Server Error");
        case SC_LENGTH_REQUIRED:
            return ("Length Required");
        case SC_METHOD_NOT_ALLOWED:
            return ("Method Not Allowed");
        case SC_MOVED_PERMANENTLY:
            return ("Moved Permanently");
        case SC_MOVED_TEMPORARILY:
            return ("Moved Temporarily");
        case SC_MULTIPLE_CHOICES:
            return ("Multiple Choices");
        case SC_NO_CONTENT:
            return ("No Content");
        case SC_NON_AUTHORITATIVE_INFORMATION:
            return ("Non-Authoritative Information");
        case SC_NOT_ACCEPTABLE:
            return ("Not Acceptable");
        case SC_NOT_FOUND:
            return ("Not Found");
        case SC_NOT_IMPLEMENTED:
            return ("Not Implemented");
        case SC_NOT_MODIFIED:
            return ("Not Modified");
        case SC_PARTIAL_CONTENT:
            return ("Partial Content");
        case SC_PAYMENT_REQUIRED:
            return ("Payment Required");
        case SC_PRECONDITION_FAILED:
            return ("Precondition Failed");
        case SC_PROXY_AUTHENTICATION_REQUIRED:
            return ("Proxy Authentication Required");
        case SC_REQUEST_ENTITY_TOO_LARGE:
            return ("Request Entity Too Large");
        case SC_REQUEST_TIMEOUT:
            return ("Request Timeout");
        case SC_REQUEST_URI_TOO_LONG:
            return ("Request URI Too Long");
        case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
            return ("Requested Range Not Satisfiable");
        case SC_RESET_CONTENT:
            return ("Reset Content");
        case SC_SEE_OTHER:
            return ("See Other");
        case SC_SERVICE_UNAVAILABLE:
            return ("Service Unavailable");
        case SC_SWITCHING_PROTOCOLS:
            return ("Switching Protocols");
        case SC_UNAUTHORIZED:
            return ("Unauthorized");
        case SC_UNSUPPORTED_MEDIA_TYPE:
            return ("Unsupported Media Type");
        case SC_USE_PROXY:
            return ("Use Proxy");
        case 207: // WebDAV
            return ("Multi-Status");
        case 422: // WebDAV
            return ("Unprocessable Entity");
        case 423: // WebDAV
            return ("Locked");
        case 507: // WebDAV
            return ("Insufficient Storage");
        default:
            return ("HTTP Response Status " + status);
        }
    }

    public void finishResponse() {
        sendHeaders();
        // Flush and close the appropriate output mechanism
        if (printWriter != null) {
            log.info("printWriter start");

            printWriter.flush();
            printWriter.close();
            log.info("printWriter end");
        }
    }

    public void write(int b) throws IOException {
        if (bufferCount >= buffer.length)
            flushBuffer();
        buffer[bufferCount++] = (byte) b;
        contentCount++;
    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        // If the whole thing fits in the buffer, just put it there
        if (len == 0)
            return;
        if (len <= (buffer.length - bufferCount)) {
            System.arraycopy(b, off, buffer, bufferCount, len);
            bufferCount += len;
            contentCount += len;
            return;
        }

        // Flush the buffer and start writing full-buffer-size chunks
        flushBuffer();
        int iterations = len / buffer.length;
        int leftoverStart = iterations * buffer.length;
        int leftoverLen = len - leftoverStart;
        for (int i = 0; i < iterations; i++)
            write(b, off + (i * buffer.length), buffer.length);

        // Write the remainder (guaranteed to fit in the buffer)
        if (leftoverLen > 0)
            write(b, off + leftoverStart, leftoverLen);
    }

    public void sendStaticResource() {
        log.info("sendStaticResource start");
        byte[] bufferArray = new byte[Constants.BUFFER_SIZE];
        FileInputStream fis = null;
        int i = 0;
        try {
            File file = new File(Constants.WEB_ROOT, request.getRequestURI());
            if (file.exists()) {
                fis = new FileInputStream(file);// not found file exception

                while ((i = fis.read(bufferArray, 0, Constants.BUFFER_SIZE)) != -1) {// ioexception
                    outputStream.write(bufferArray, 0, i);// ioexception
                }
            } else {
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n" + "Content-Type:text/html\r\n" + "Content-Length:23\r\n" + "\r\n"
                        + "<h1>File Not Found</h1>";
                outputStream.write(errorMessage.getBytes());
            }
        } catch (Exception e) {
            log.error("error message {}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("error message {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        log.info("sendStaticResource end");
    }

    protected void sendHeaders() {
        log.info("sendHeaders start");
        if (isCommitted()){
            log.info("sendHeaders return");            
            return;
        }
        OutputStreamWriter osr = null;
        try {
            osr = new OutputStreamWriter(getStream(), getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            osr = new OutputStreamWriter(getStream());
        }

        final PrintWriter outputWriter = new PrintWriter(osr);
        outputWriter.print(this.getProtocol());
        outputWriter.print(" ");
        outputWriter.print(status);
        if (message != null) {
            outputWriter.print(" ");
            outputWriter.print(message);
        }
        outputWriter.print("\r\n");
        // Send the content-length and content-type headers (if any)
        if (getContentType() != null) {
            outputWriter.print("Content-Type: " + getContentType() + "\r\n");
        }
        if (getContentLength() >= 0) {
            outputWriter.print("Content-Length: " + getContentLength() + "\r\n");
        }
        // Send all specified headers (if any)
        synchronized (headers) {
            Iterator names = headers.keySet().iterator();
            while (names.hasNext()) {
                String name = (String) names.next();
                ArrayList values = (ArrayList) headers.get(name);
                Iterator items = values.iterator();
                while (items.hasNext()) {
                    String value = (String) items.next();
                    outputWriter.print(name);
                    outputWriter.print(": ");
                    outputWriter.print(value);
                    outputWriter.print("\r\n");
                }
            }
        }
        synchronized (cookies) {
            Iterator items = cookies.iterator();
            while (items.hasNext()) {
                Cookie cookie = (Cookie) items.next();
                outputWriter.print(CookieTools.getCookieHeaderName(cookie));
                outputWriter.print(": ");
                outputWriter.print(CookieTools.getCookieHeaderValue(cookie));
                outputWriter.print("\r\n");
            }
        }

        // Send a terminating blank line to mark the end of the headers
        outputWriter.print("\r\n");
        outputWriter.flush();

        committed = true;
        log.info("sendHeaders end");
    }

    protected String getProtocol() {
        return request.getProtocol();
    }

    public OutputStream getStream() {
        return this.outputStream;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getCharacterEncoding() {
        if (characterEncoding == null)
            return ("ISO-8859-1");
        else
            return (characterEncoding);
    }

    public PrintWriter getWriter() throws IOException {
        // TODO Auto-generated method stub
        ResponseStream newStream = new ResponseStream(this);
        newStream.setCommit(false);
        OutputStreamWriter osr = new OutputStreamWriter(newStream, getCharacterEncoding());
        printWriter = new ResponseWriter(osr);
        
        return printWriter;
    }

    public void setBufferSize(int size) {
        // TODO Auto-generated method stub

    }

    public int getBufferSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void flushBuffer() throws IOException {
        log.info("flushBuffer start");
        // TODO Auto-generated method stub
        if (bufferCount > 0) {
            try {
                outputStream.write(buffer, 0, bufferCount);
            } finally {
                bufferCount = 0;
            }
        }
        log.info("flushBuffer end");

    }

    public void resetBuffer() {
        // TODO Auto-generated method stub

    }

    public boolean isCommitted() {
        // TODO Auto-generated method stub
        return committed;
    }

    public void reset() {
        // TODO Auto-generated method stub

    }

    public void setLocale(Locale loc) {
        // TODO Auto-generated method stub

    }

    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    public void addCookie(Cookie cookie) {
        // TODO Auto-generated method stub

    }

    public boolean containsHeader(String name) {
        // TODO Auto-generated method stub
        synchronized (headers) {
            return (headers.get(name) != null);
        }
    }

    public String encodeURL(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeRedirectURL(String url) {
        // TODO Auto-generated method stub
        return null;
    }

    public String encodeUrl(String url) {
        // TODO Auto-generated method stub
        return encodeURL(url);
    }

    public String encodeRedirectUrl(String url) {
        // TODO Auto-generated method stub
        return encodeRedirectURL(url);
    }

    public void sendError(int sc, String msg) throws IOException {
        // TODO Auto-generated method stub

    }

    public void sendError(int sc) throws IOException {
        // TODO Auto-generated method stub

    }

    public void sendRedirect(String location) throws IOException {
        // TODO Auto-generated method stub

    }

    public void setDateHeader(String name, long date) {
        // TODO Auto-generated method stub

    }

    public void addDateHeader(String name, long date) {
        // TODO Auto-generated method stub
        if (isCommitted())
            addHeader(name, format.format(new Date(date)));
    }

    public void setHeader(String name, String value) {
        // TODO Auto-generated method stub

    }

    public void addHeader(String name, String value) {
        // TODO Auto-generated method stub
        if (isCommitted())
            return;
        synchronized (headers) {
            ArrayList values = (ArrayList) headers.get(name);
            if (values == null) {
                values = new ArrayList();
                headers.put(name, values);
            }

            values.add(value);
        }
    }

    public void setIntHeader(String name, int value) {
        // TODO Auto-generated method stub

    }

    public void addIntHeader(String name, int value) {
        // TODO Auto-generated method stub
        if (isCommitted())
            return;
        addHeader(name, "" + value);
    }

    public void setStatus(int sc) {
        // TODO Auto-generated method stub

    }

    public void setStatus(int sc, String sm) {
        // TODO Auto-generated method stub

    }
}
