package org;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Orchestrator {
   public static void start() throws IOException {
       Scanner sc = new Scanner(System.in);
       GUI GUI = new GUI();
       SelectFile sf = new SelectFile();
       Controller CR = new Controller();
       File SFilePath = sf.Filechooser(1);
       File TFilePath = sf.Filechooser(2);
       MessegeSystem ms = new MessegeSystem();
       String base = System.getProperty("user.home");
       File file = new File(base, "Documents/ordexsave/ordex.properties");
       ArrayList<String> keyword;
       ArrayList<String> extension;
       PropertySettings prop = new PropertySettings();
       String YN = "";
       if(file.exists()){
           System.out.println("ファイルが見つかりました\n新しく設定しますか？");
           YN = sc.nextLine();
       }
       if (YN.equalsIgnoreCase("yes")) {
           keyword = ms.keymessege();
           extension = ms.extmessege();
           prop.Write(keyword, extension); // ここで書き込んでいるよ
       }




       prop.Read(file);
       Ruledate date = new Ruledate();
       FileWatcher fw = new FileWatcher();
       fw.watchservice(SFilePath, TFilePath, date.pkey(), date.pext());
   }
}
