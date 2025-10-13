import javax.swing.*;
import java.io.File;
public class SelectFile {
    public  File Filechooser(int modeselect){
        String Title = null;
        JFileChooser chooser = new JFileChooser("C:\\Users\\yunre\\Downloads");
        File Filename = new File(System.getProperty("user.dir"));
        //ディレクトリ選択のみ
        if(modeselect == 1){
            Title = ("監視するディレクトリを選択してくれい");
        }
        if(modeselect == 2){
            Title = ("移動させるディレクトリを選択してくれい");
        }
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(Title);
        int selected = chooser.showOpenDialog(null);
        Filename = chooser.getSelectedFile().getAbsoluteFile();//java.io.Fileオプションとして取得する
        if(selected == JFileChooser.APPROVE_OPTION){
            System.out.println("選択できました");
        }
        if(selected == JFileChooser.CANCEL_OPTION){
            System.out.println("選択がキャンセルされました");
        }
        System.out.println(Filename + "を選択しました");
        return Filename;


    }
}
