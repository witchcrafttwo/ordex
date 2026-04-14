module ordex.gradle.main {
    requires java.datatransfer;
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;


    opens org to javafx.fxml;
    exports org;
}