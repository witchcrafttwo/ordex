package org;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        boolean startInTray = getParameters().getRaw().contains("--tray");
        GUI2 gui = new GUI2(startInTray);
        gui.show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}











