package com.lm.mytomcat.chap2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Request implements ServletRequest{



    @NonNull
    private InputStream inputStream;
    @Getter
    private String uri;

    private String parseUri(String requestString) {
        log.info(" parseUri() start");
        int index1, index2;
        index1 = requestString.indexOf(Constants.SPACE);
        if (index1 != -1) {
            index2 = requestString.indexOf(Constants.SPACE, index1 + 1);
            if (index2 > index1) {
                return requestString.substring(index1 + 1, index2);
            }
        }
        log.info(" parseUri() end");
        return "";
    }

    public void parse() {
        log.info(" parse() start");
        StringBuffer requestStringBuffer = new StringBuffer();
        String requestString = new String();
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while((requestString = bufferedreader.readLine()) != null && !requestString.equals("")){
                requestStringBuffer.append(requestString).append("\r\n");
            }
        } catch (IOException e) {
            log.error("error message {}",e.getMessage() );
            e.printStackTrace();
        }
       /* int i = 0;
        byte[] bufferArray = new byte[BUFFER_SIZE];//request的inputStream 一直在 直到浏览器返回-1 
        try {

            i = inputStream.read(bufferArray, 0, BUFFER_SIZE);
            for (int j = 0; j < i; j++) {
                requestString.append((char) bufferArray[j]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // 读取socket的字节流到 字节数组中 再填充的Thread安全的StringBuffer中
        log.info("requestString : " + requestStringBuffer.toString());
        uri = parseUri(requestStringBuffer.toString());
        log.info("parse() end");
    }

    public Object getAttribute(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getAttributeNames() {
        // TODO Auto-generated method stub
        return null;
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
        return 0;
    }

    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    public ServletInputStream getInputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getParameter(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Enumeration getParameterNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] getParameterValues(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map getParameterMap() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getProtocol() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getScheme() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getServerPort() {
        // TODO Auto-generated method stub
        return 0;
    }

    public BufferedReader getReader() throws IOException {
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

}
