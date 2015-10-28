package com.lm.mytomcat.chap1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Response {

    private static final int BUFFER_SIZE = 2048;

    @Setter
    private Request request;
    @NonNull
    private OutputStream outputStream;

    public void sendStaticResource(){
        log.info("sendStaticResource start" );
        byte[] bufferArray = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        int i = 0;
        try {
            File file = new  File(HttpServer.WEB_ROOT ,request.getUri());   //File(String parent, String child) Creates a new File instance from a parent pathname string and a child pathname string.
            if (file.exists()) {
                fis = new FileInputStream(file);//not found file exception 
                
                while (( i = fis.read(bufferArray, 0, BUFFER_SIZE)) != -1) {//ioexception
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

}
