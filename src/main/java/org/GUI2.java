package org;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class GUI2 {

    private TextField filePathA;
    private TextField filePath2;

    private File selectedWatchFolder;
    private File selectedTargetFolder;

    public void show(Stage stage) {
        Label l1 = new Label("監視フォルダー");

        Button watchButton = new Button("監視フォルダを選択");
        Button select2 = new Button("移動先フォルダを選択");
        Button startButton = new Button("開始");

        filePathA = new TextField();
        filePathA.setEditable(false);

        filePath2 = new TextField();
        filePath2.setEditable(false);

        watchButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("監視フォルダを選択");
            File file = chooser.showDialog(stage);
            if (file != null) {
                selectedWatchFolder = file;
                filePathA.setText(file.getAbsolutePath());
            }
        });

        select2.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("移動先フォルダを選択");
            File file = chooser.showDialog(stage);
            if (file != null) {
                selectedTargetFolder = file;
                filePath2.setText(file.getAbsolutePath());
            }
        });

        startButton.setOnAction(e -> {
            if (selectedWatchFolder == null || selectedTargetFolder == null) {
                System.out.println("フォルダを選択してください");
                return;
            }

            System.out.println("監視フォルダ: " + selectedWatchFolder.getAbsolutePath());
            System.out.println("移動先フォルダ: " + selectedTargetFolder.getAbsolutePath());

            // ここで監視処理を開始したいなら呼ぶ
            // FileWatcher fw = new FileWatcher();
            // fw.watchservice(selectedWatchFolder, selectedTargetFolder, ... , ...);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));

        grid.add(l1, 0, 0);
        grid.add(watchButton, 0, 1);
        grid.add(filePathA, 1, 1);

        grid.add(select2, 0, 2);
        grid.add(filePath2, 1, 2);

        grid.add(startButton, 0, 3);

        VBox root = new VBox(10, grid);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 800, 300);
        stage.setTitle("ordex");
        stage.setScene(scene);
        stage.show();
    }
}