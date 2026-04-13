package org;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private static String[] launchArgs = new String[0];

    @Override
    public void start(Stage stage) {
        GUI2 gui = new GUI2();
        boolean startHidden = false;
        for (String arg : launchArgs) {
            if ("--hidden".equalsIgnoreCase(arg)) {
                startHidden = true;
                break;
            }
        }
        gui.show(stage, startHidden);
    }

    public static void main(String[] args) {
        launchArgs = args;
        launch(args);
    }
}










