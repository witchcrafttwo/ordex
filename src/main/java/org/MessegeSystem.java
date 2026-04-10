package org;

import java.util.ArrayList;
import java.util.Scanner;

public class MessegeSystem {
    public ArrayList keymessege(){
        Scanner sc = new Scanner(System.in);
        ArrayList<String> keyword = new ArrayList<String>();
        do{
            System.out.println("keywordを入力してね。ESCと入力すると次にいくよ");
            keyword.add(sc.nextLine());
        }while(!keyword.contains("ESC"));
        keyword.remove(keyword.indexOf("ESC"));
        return keyword;
    }
    public ArrayList extmessege(){
        Scanner sc = new Scanner(System.in);
        ArrayList<String> extension = new ArrayList<String>();
        do{
            System.out.println("拡張子を入力してね。例: .txt");
            extension.add(sc.nextLine());
        }while(!extension.contains("ESC"));
        extension.remove(extension.indexOf("ESC"));
        return extension;

    }
}
