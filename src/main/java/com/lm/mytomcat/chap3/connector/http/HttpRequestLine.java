package com.lm.mytomcat.chap3.connector.http;

public class HttpRequestLine {
    
    public char[] method;
    public int methodEnd;
    public char[] uri;
    public int uriEnd;
    public char[] protocol;
    public int protocolEnd;
    
    public void recycle() {
       methodEnd = 0; 
       uriEnd = 0;
       protocolEnd = 0;
    }
}
