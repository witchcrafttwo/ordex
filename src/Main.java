import java.io.File;
public class Main {
    public static void main(String[] args){
        GUI gui = new GUI();
        SelectFile sf = new SelectFile();
        File FilePath = sf.Filechooser(1);
        System.out.println("Selected file: " + FilePath);



    }
}
