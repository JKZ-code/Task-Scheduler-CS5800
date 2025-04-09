module org.test.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires okhttp3;


    opens org.test.frontend to javafx.fxml;
    exports org.test.frontend;
}