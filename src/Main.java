import java.io.File;
public class Main {
    static SelectFile sf = new SelectFile();

    public static void main(String[] args){
        GUI.frame();
        File FilePath = sf.Filechooser(1);



    }
}
