import javax.swing.*;
import java.io.File;
public class SelectFile {
    public static void Filechooser(){
        JFileChooser chooser = new JFileChooser();
        File Filename = new File(System.getProperty("user.dir"));
        //ディレクトリ選択のみ
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("フォルダを選択してください");
        int file = chooser.showOpenDialog(null);
        Filename = chooser.getSelectedFile();
        System.out.println(Filename);


    }
}
