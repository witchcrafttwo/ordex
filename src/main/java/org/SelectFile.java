package org;

import javax.swing.*;
import java.io.File;
public class SelectFile {
    public  File Filechooser(int modeselect){
        String Title = null;
        JFileChooser chooser = new JFileChooser("C:\\Users\\yunre\\Downloads");
        File Filename = new File(System.getProperty("user.dir")); //現在のディレクトリが初期値のはず
        //ディレクトリ選択のみ
        if(modeselect == 1){
            Title = ("監視するディレクトリを選択してくれい");
        }
        if(modeselect == 2){
            Title = ("移動させるディレクトリを選択してくれい");
        }
        try {
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle(Title);
            int selected = chooser.showOpenDialog(null);
            Filename = chooser.getSelectedFile().getAbsoluteFile();//java.io.Fileオプションとして取得する
            if (selected == JFileChooser.APPROVE_OPTION) {
                System.out.println("選択できました");
            }
        }catch(NullPointerException e){
            System.out.println("選択をキャンセルしました");
        }

        System.out.println(Filename + "を選択しました");
        return Filename;


    }
}
