package com.lm.mytomcat.chap3.startup;

import com.lm.mytomcat.chap3.connector.http.HttpConnector;

public final class Bootstrap {
    
    public static void main(String[] args){
        HttpConnector connector = new HttpConnector();
        connector.start();
    }
    
}
