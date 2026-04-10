package org;

import java.util.ArrayList;
public class Ruledate {
    static ArrayList<String> keyword = new ArrayList<>();
    static ArrayList<String> extension = new ArrayList<>();
    public void regi(ArrayList<String> key,ArrayList<String> ext){
        this.keyword = key;
        this.extension = ext;
    }
    public ArrayList pkey(){
        return this.keyword;
    }
    public ArrayList pext(){
        return this.extension;
    }
}
