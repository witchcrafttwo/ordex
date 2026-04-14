package org;

import  java.util.Scanner;
public class Controller {
    public String scanner(String msg){
        Scanner sc=new Scanner(System.in);
        System.out.println(msg);
        String str=sc.nextLine();
        sc.close();
        return str;
    }
    public String scanner(){
        Scanner sc=new Scanner(System.in);
        String str=sc.nextLine();
        sc.close();
        return str;
    }
}
