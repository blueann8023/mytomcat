package com.lm.mytomcat.chap3.startup;

import lombok.extern.slf4j.Slf4j;

import com.lm.mytomcat.chap3.connector.http.HttpConnector;

@Slf4j
public final class Bootstrap {
    
    public static void main(String[] args){
        log.info("main Start");
        HttpConnector connector = new HttpConnector();
        connector.start();
        log.info("main End");
    }
    
}
