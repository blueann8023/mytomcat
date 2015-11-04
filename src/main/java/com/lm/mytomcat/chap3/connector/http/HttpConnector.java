package com.lm.mytomcat.chap3.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpConnector implements Runnable {
    
    boolean stopped = false;
    @Getter
    private String scheme = "http" ;

    public void run() {
        log.info("connector run Start");
        ServerSocket serverSocket = null; 
        try{
          
            serverSocket = new  ServerSocket(8080 ,1 , InetAddress.getByName("127.0.0.1"));
            log.info("connector run serverSocket {}",serverSocket);
        }catch(IOException e){
            e.printStackTrace();
        }
        while(!stopped){
            Socket socket = null;
            try{
                socket = serverSocket.accept(); 
                log.info("connector run socket {}",socket);
            }catch(Exception e){
                e.printStackTrace();
                continue;
            }
            HttpProcessor processor = new HttpProcessor(this);
            log.info("connector run processor {}",processor);
            processor.process(socket);
        }
        log.info("connector run End");
    }
    
    public void start(){
        log.info("connector start Start");
        Thread thread = new Thread(this);
        thread.start();
        log.info("connector start End");
    }

}
