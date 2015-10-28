package com.lm.mytomcat.chap1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Request {

    private static final int BUFFER_SIZE = 2048;
    private static final char SPACE = ' ';

    @NonNull
    private InputStream inputStream;
    @Getter
    private String uri;

    private String parseUri(String requestString) {
        log.info(" parseUri() start");
        int index1, index2;
        index1 = requestString.indexOf(SPACE);
        if (index1 != -1) {
            index2 = requestString.indexOf(SPACE, index1 + 1);
            if (index2 > index1) {
                return requestString.substring(index1 + 1, index2);
            }
        }
        log.info(" parseUri() end");
        return "";
    }

    public void parse() {
        log.info(" parse() start");
        StringBuffer requestStringBuffer = new StringBuffer();
        String requestString = new String();
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while((requestString = bufferedreader.readLine()) != null && !requestString.equals("")){
                requestStringBuffer.append(requestString).append("\r\n");
            }
        } catch (IOException e) {
            log.error("error message {}",e.getMessage() );
            e.printStackTrace();
        }
       /* int i = 0;
        byte[] bufferArray = new byte[BUFFER_SIZE];//request的inputStream 一直在 直到浏览器返回-1 
        try {

            i = inputStream.read(bufferArray, 0, BUFFER_SIZE);
            for (int j = 0; j < i; j++) {
                requestString.append((char) bufferArray[j]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // 读取socket的字节流到 字节数组中 再填充的Thread安全的StringBuffer中
        log.info("requestString : " + requestStringBuffer.toString());
        uri = parseUri(requestStringBuffer.toString());
        log.info("parse() end");
    }

}
