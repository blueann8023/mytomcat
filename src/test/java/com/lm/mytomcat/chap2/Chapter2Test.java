package com.lm.mytomcat.chap2;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Chapter2Test {

    public static void main(String[] args){
        File classPath = new File(Constants.WEB_ROOT);
        try {
            String repository = new URL("file",null,classPath.getCanonicalPath() + File.separator).toString();
            
            System.out.print(classPath.getCanonicalPath());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            System.out.print(classPath.getCanonicalPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
