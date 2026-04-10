package org;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class GUI extends JFrame implements ActionListener {
    JPanel main;
    JPanel Panel1;
    JPanel Panel2;
    JPanel PanelA;
    JPanel PanelB;
    JPanel PanelC;
    JPanel PanelD;
    JPanel Panel7;
    JTextField FilepathA;
    JTextField Filepath2;
    JTextField Filepath3;
    JTextField Filepath4;
    JButton watchbutton;
    JButton select2;
    JButton select3;
    JLabel l1;
    SelectFile sf = new SelectFile();  //ディレクトリ選択のやつ
    FileWatcher fw = new FileWatcher(); //ファイル監視のやつ
    public GUI() {
        super("ordex");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main = new JPanel(new GridLayout(3, 1));
        add(main);
        Font font = new Font("Serif", Font.PLAIN, 30);

        Panel1 = new JPanel(new GridLayout(1, 2));
        PanelA = new JPanel(new GridBagLayout());
        PanelB = new JPanel(new GridBagLayout());
        watchbutton = new JButton("監視");
        watchbutton.addActionListener(this);
        FilepathA = new JTextField();
        FilepathA.setEditable(false);


        l1     = new JLabel("監視フォルダー");
        l1.setFont(font);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.weighty = 9;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0; gbc.gridy =0;
        PanelA.add(l1, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        PanelA.add(watchbutton, gbc);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.insets = new Insets(6,6,6,6);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        PanelA.add(FilepathA, gbc);

        Panel1.add(PanelA);
        Panel1.add(PanelB);
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
        File file = sf.Filechooser(1);

        }

    }

