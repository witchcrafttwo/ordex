import javax.swing.*;
import java.io.File;
public class SelectFile {
    public static File Filechooser(int modeselect){
        String Title = null;
        JFileChooser chooser = new JFileChooser();
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
        int file = chooser.showOpenDialog(null);
        Filename = chooser.getSelectedFile();
        System.out.println(Filename);
        return Filename;


    }
}
