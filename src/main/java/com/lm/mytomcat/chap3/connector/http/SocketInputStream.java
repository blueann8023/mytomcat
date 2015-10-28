package com.lm.mytomcat.chap3.connector.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;


public class SocketInputStream extends InputStream{
    //constants
    private static final byte CR = (byte) '\r';
    private static final byte LF = (byte) '\n';
    private static final byte SP = (byte) ' ';
    private static final byte HT = (byte) '\t';
    private static final byte COLON = (byte) ':';
    private static final int LC_OFFSET = 'A' - 'a';// Lower case offset.
    
    // Underlying input stream.construtor
    protected InputStream inputStream;
    
    protected byte buf[];//buffer .  construtor instance
    protected int count;//Last valid byte in buffer.
    protected int pos;//Position in the buffer.
    
    
    /*protected static StringManager sm =
        StringManager.getManager(Constants.Package);*/
    
    public SocketInputStream(InputStream inputStream, int bufferSize) {
        this.inputStream = inputStream;
        buf = new byte[bufferSize];
    }

    public void readHttpRequestLine(HttpRequestLine httpRequestLine) throws EOFException{
        
        //recyling check 避免HttpRequestLine在其他地方被改写了 确保是ok的 
        if(httpRequestLine.methodEnd != 0 ){
            httpRequestLine.recycle();
        }
        
        //check for a blank line
        int chr = 0;
        do{//skip cr lf
            try {
                chr = read();
            } catch (IOException e) {
                chr = -1;
            }
        }while(chr == CR || chr == LF);
        
        if(chr == -1){
            throw new EOFException();
        }
    }
    
    @Override
    public int read() throws IOException {
        if(pos >= count){
            fill();
            if(pos >= count)
                return -1;
        }
        return buf[pos++] & 0xff;//0xff = int -1 
    }
    // Fill the internal buffer using data from the undelying input stream.
    protected void fill() throws IOException{
        pos  = 0;
        count = 0; 
        int nRead = inputStream.read(buf , 0 ,buf.length);
        if(nRead > 0){
            count = nRead ;
        }
    }
    
    
}
