package com.lm.mytomcat.chap3.connector.http;

import java.io.OutputStream;
import java.net.Socket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import com.lm.mytomcat.chap3.connector.ServletProcessor;
import com.lm.mytomcat.chap3.connector.StaticResourceProcessor;

@RequiredArgsConstructor
public class HttpProcessor {

    @NonNull
    private HttpConnector connector;
    
    private HttpRequest request;
    
    private HttpRequestLine requestLine = new HttpRequestLine();
    
    private HttpResponse response;

   /* protected String method = null;
    protected String queryString = null;*/
    
   /* protected StringManager sm =
            StringManager.getManager("com.lm.mytomcat.chap3.connector.http");*/
    
    public void process(Socket socket){
        SocketInputStream  input = null;
        OutputStream output = null;
        try{
            input = new SocketInputStream(socket.getInputStream(),2048);
            output = socket.getOutputStream();
            
            request = new HttpRequest(input);
            response = new HttpResponse(output);
            response.setRequest(request);
            response.setHeader("Server", "Lm Servlet Container");
            
            parseRequest(input,output);
            //parseHeaders(input);
            
            if (request.getRequestURI().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
              }
              else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
              }
             
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void parseRequest(SocketInputStream input , OutputStream output){
        //input.readHttpRequestLine(requestLine);
    }
}
