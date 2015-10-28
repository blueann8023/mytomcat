package com.lm.mytomcat.chap2;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResponseFacade implements ServletResponse{

    @NonNull
    private ServletResponse servletResponse ;
    
    public String getCharacterEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getContentType() {
        // TODO Auto-generated method stub
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setCharacterEncoding(String charset) {
        // TODO Auto-generated method stub
        
    }

    public void setContentLength(int len) {
        // TODO Auto-generated method stub
        
    }

    public void setContentType(String type) {
        // TODO Auto-generated method stub
        
    }

    public void setBufferSize(int size) {
        // TODO Auto-generated method stub
        
    }

    public int getBufferSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void flushBuffer() throws IOException {
        // TODO Auto-generated method stub
        
    }

    public void resetBuffer() {
        // TODO Auto-generated method stub
        
    }

    public boolean isCommitted() {
        // TODO Auto-generated method stub
        return false;
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

}
