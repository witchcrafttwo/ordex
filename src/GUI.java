import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GUI extends JFrame implements ActionListener {
    JPanel main;
    JPanel Panel1;
    JPanel Panel2;
    JPanel Panel3;
    JPanel Panel4;
    JPanel Panel5;
    JPanel Panel6;
    JPanel Panel7;
    JTextField Filepath1;
    JTextField Filepath2;
    JTextField Filepath3;
    JTextField Filepath4;
    JButton select1;
    JButton select2;
    JButton select3;
    JLabel l1;
    SelectFile sf = new SelectFile();  //ディレクトリ選択のやつ
    FileWatcher fw = new FileWatcher(); //ファイル監視のやつ
    public GUI() {
        super("ordex");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main = new JPanel(new GridLayout(2, 1));
        add(main);
        Font font = new Font("Serif", Font.PLAIN, 30);

        Panel1 = new JPanel(new GridLayout(1, 2));
        Filepath1 = new JTextField();
        Filepath1.setEditable(false);//入力はブロックして表示は可能にする
        select1 = new JButton("Select File");
        select1.addActionListener(this);
        Panel1.add(select1);
        Panel1.add(Filepath1);
        main.add(Panel1);

        Panel2 = new JPanel(new GridLayout(1, 2));
        Filepath2 = new JTextField();
        Filepath2.setEditable(false);
        select2 = new JButton("Select File");
        Panel2.add(select2);
        Panel2.add(Filepath2);
        main.add(Panel2);

        setVisible(true);


    }



    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if(e.getSource() == select1){
            System.out.println("アクションリスナーテスト");
         File file  = sf.Filechooser(1);

        }

    }
}
