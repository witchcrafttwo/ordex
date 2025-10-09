import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame implements ActionListener {
    JPanel main;
    JTextField test1;
    JLabel l1;
    public GUI() {
        super("ordex");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main = new JPanel(new GridLayout(2, 2));
        add(main);

    }



    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
