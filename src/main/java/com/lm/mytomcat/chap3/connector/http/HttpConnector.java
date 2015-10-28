package com.lm.mytomcat.chap3.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.Getter;

public class HttpConnector implements Runnable {
    
    boolean stopped ;
    @Getter
    private String scheme = "http" ;

    public void run() {
        ServerSocket serverSocket = null; 
        try{
            serverSocket = new  ServerSocket(8080 ,1 , InetAddress.getByName("127.0.0.1"));
        }catch(IOException e){
            e.printStackTrace();
        }
        while(!stopped){
            Socket socket = null;
            try{
                socket = serverSocket.accept(); 
            }catch(Exception e){
                e.printStackTrace();
                continue;
            }
            HttpProcessor processor = new HttpProcessor(this);
            processor.process(socket);
        }
    }
    
    public void start(){
        Thread thread = new Thread(this);
        thread.start();
    }

}
