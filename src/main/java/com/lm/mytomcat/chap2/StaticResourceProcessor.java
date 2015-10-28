package com.lm.mytomcat.chap2;

public class StaticResourceProcessor {

    public void process(Request request, Response response) {
        response.sendStaticResource();
    }

}
