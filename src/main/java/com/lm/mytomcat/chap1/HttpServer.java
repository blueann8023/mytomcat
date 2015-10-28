package com.lm.mytomcat.chap1;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class HttpServer {

    public static final String WEB_ROOT = System.getProperty("user.dir") +  File.separator+"webroot";

    public static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

    private boolean shutdown = false;

    public static void main(String[] args) {
        log.info("System Get Property : " + System.getProperty("user.dir")+ File.separator +"webroot");

        HttpServer server = new HttpServer();

        server.await();
    }

    public void await() {
        ServerSocket server = null;

        try {
            server = new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(!shutdown){
            Socket client = null;
            InputStream input = null;
            OutputStream output = null;  
           
            try {
                client  = server.accept();
                input = client.getInputStream();
                output = client.getOutputStream();
                
                //create request obj
                Request request = new Request(input);
                request.parse();
                
                //create response
                Response response = new Response(output);
                response.setRequest(request);
                response.sendStaticResource();
                
                client.close();
                
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                log.error("error message : " + e.toString());
            }

        }
    }
}
