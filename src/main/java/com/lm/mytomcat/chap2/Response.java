package com.lm.mytomcat.chap2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Response implements ServletResponse{

    @Setter
    private Request request;
    @NonNull
    private OutputStream outputStream;
    private PrintWriter printWriter;

    public void sendStaticResource(){
        log.info("sendStaticResource start" );
        byte[] bufferArray = new byte[Constants.BUFFER_SIZE];
        FileInputStream fis = null;
        int i = 0;
        try {
            File file = new  File(Constants.WEB_ROOT ,request.getUri());   //File(String parent, String child) Creates a new File instance from a parent pathname string and a child pathname string.
            if (file.exists()) {
                fis = new FileInputStream(file);//not found file exception 
                
                while (( i = fis.read(bufferArray, 0, Constants.BUFFER_SIZE)) != -1) {//ioexception
                    outputStream.write(bufferArray, 0, i);//ioexception
                }
            }else{
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n"+
                                        "Content-Type:text/html\r\n" +
                                        "Content-Length:23\r\n" + 
                                        "\r\n" +
                                        "<h1>File Not Found</h1>" ;
                outputStream.write(errorMessage.getBytes());
            }
        }catch (Exception e) {
            log.error("error message {}" , e.getMessage());
            e.printStackTrace();
        }finally{
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("error message {}",e.getMessage() );
                    e.printStackTrace();
                }
            }
        }
        log.info("sendStaticResource end" );
    }

    public String getCharacterEncoding() {
       
        return null;
    }

    public String getContentType() {
       
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
     
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        
        
        printWriter = new PrintWriter(outputStream,true);//autoflush is true , enable to println , not enable to print
        
        return printWriter;
    }

    public void setCharacterEncoding(String charset) {
       
        
    }

    public void setContentLength(int len) {
      
        
    }

    public void setContentType(String type) {
        // TODO Auto-generated method stub
        
    }

    public void setBufferSize(int size) {
       
        
    }

    public int getBufferSize() {
      
        return 0;
    }

    public void flushBuffer() throws IOException {
      
        
    }

    public void resetBuffer() {
      
        
    }

    public boolean isCommitted() {
      
        return false;
    }

    public void reset() {
       
        
    }

    public void setLocale(Locale loc) {
      
        
    }

    public Locale getLocale() {
       
        return null;
    }

}
