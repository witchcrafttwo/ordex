import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    public GUI() {
        super("ordex");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main = new JPanel(new GridLayout(2, 2));
        add(main);
        Font font = new Font("Serif", Font.PLAIN, 30);

        Panel1 = new JPanel(new GridLayout(2, 1));
        Filepath1 = new JTextField();
        Filepath2 = new JTextField();
        Filepath1.setEditable(false);//入力はブロックして表示は可能にする
        Filepath2.setEditable(false);
        select1 = new JButton("Select File");


    }



    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
