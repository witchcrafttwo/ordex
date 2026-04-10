package org;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ArrayList;


public class PropertySettings {
    private static final String FileName = "ordex.properties";
    File Path;
    public void Write(ArrayList<String> keyword ,ArrayList<String> extension){
        Properties write = new Properties();
        int index =0;
        for(String key : keyword){
            write.setProperty("keyword"+index,key);
            index++;
        }
        index = 0;
        for(String ext : extension){
            write.setProperty("extension"+index,ext);
            index++;
        }

        String base = System.getProperty("user.home"); //use.homeっていうのはC/Users/userのホームパス
        File savePath = new File(base,"Documents/ordexsave/ordex.properties");//親pathと子pathを結合してくれる神
        savePath.getParentFile().mkdirs();
        Path = savePath;
        try (OutputStreamWriter out = new OutputStreamWriter(
                    new FileOutputStream(savePath,false),
                    StandardCharsets.UTF_8


            )){
            write.store(out,"japanes");

        } catch(IOException e){
            System.out.println("書き込みエラー"+e.getMessage());
        }


    }


    public void Read(File path){
        Properties read = new Properties();
        ArrayList<String> key = new ArrayList<>();
        ArrayList<String> ext = new ArrayList<>();
        Map<String,String> readdate = new HashMap<>();
        try(Reader reader = new FileReader(path)){
                read.load(reader);
                for(int index = 0;;index++) {
                    String keyword = read.getProperty("keyword"+index);
                    String extension = read.getProperty("extension"+ index);
                    if(keyword == null && extension == null ){
                        break;
                    }
                    if(keyword !=null ) {
                        key.add(read.getProperty("keyword" + index));
                    }
                    if(extension != null){
                        ext.add(read.getProperty("extension"+index));

                    }
                }
        } catch(IOException e){
            e.getMessage();
        }
        Ruledate date = new Ruledate();
        date.regi(key,ext);//ここでruledateクラスに読み込んだarraylistを保存する

    }
    }