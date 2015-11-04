package com.lm.mytomcat.chap3;

import com.lm.mytomcat.chap3.connector.http.HttpRequest;
import com.lm.mytomcat.chap3.connector.http.HttpResponse;

public class StaticResourceProcessor {

    public void process(HttpRequest request, HttpResponse response) {
        // TODO Auto-generated method stub
        response.sendStaticResource();
    }

}
