package org;

import javax.swing.*;
import java.io.File;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SelectFile extends Application {
    public void start(Stage stage){
        Application.launch(SelectFile.class);

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle( "ディレクトリ選択" );
        String base = System.getProperty("user.home");
        directoryChooser.setInitialDirectory( new File(base,"Downloads") );
        File f4 = directoryChooser.showDialog( stage );
    }
    public  File Filechooser(int modeselect){
        String Title = null;
        JFileChooser chooser = new JFileChooser("C:\\Users\\yunre\\Downloads");
        File Filename = new File(System.getProperty("user.dir")); //現在のディレクトリが初期値のはず
        //ディレクトリ選択のみ
        if(modeselect == 1){
            Title = ("監視するディレクトリを選択してください");
        }
        if(modeselect == 2){
            Title = ("移動させるディレクトリを選択してください");
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
