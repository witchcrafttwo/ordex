module ordex.main {
    requires java.datatransfer;
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires com.sun.jna;
    requires com.sun.jna.platform;


    opens org to javafx.fxml;
    exports org;
}
