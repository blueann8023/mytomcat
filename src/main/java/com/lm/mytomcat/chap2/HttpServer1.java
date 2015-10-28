package com.lm.mytomcat.chap2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;


public class HttpServer1 {

    private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
    
    private boolean shutdown = false ;
    public static void main(String[] args){
        HttpServer1 httpServer  = new HttpServer1();
        httpServer.await();
    }
    
    public void await(){
        ServerSocket server = null ; 
        try {
            server = new ServerSocket(8080,1,InetAddress.getByName("127.0.0.1"));
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while(!shutdown){
            Socket client = null;
            InputStream inputStream = null;
            OutputStream outputStream = null; 
            try {
                client = server.accept();
                inputStream = client.getInputStream();
                outputStream = client.getOutputStream();
                
                Request request = new Request(inputStream);
                request.parse();
                
                Response response = new Response(outputStream);
                response.setRequest(request);
                
                if(request.getUri().startsWith("/servlet")){
                    ServletProcessor1 processor = new ServletProcessor1();
                    processor.process(request,response);   
                    // issue :   对于的自己而言response 和request中sendStaticResource parse是public
                    //但是servlet 接到这两个参数后 是不应该可以使用这两个方法 
                    //solution : facade 外观类
                }else if(StringUtils.isNotBlank(request.getUri())){
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request,response);      
                }
                
                client.close();
                
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
    }
}
