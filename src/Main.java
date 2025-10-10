import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        GUI gui = new GUI();
       SelectFile sf = new SelectFile();
       File FilePath = sf.Filechooser(1);
       FileWatcher fw = new FileWatcher();
       fw.watchservice(FilePath);
       System.out.println("Selected file: " + FilePath);



    }
}
