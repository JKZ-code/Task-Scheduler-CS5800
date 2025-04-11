module org.test.frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires okhttp3;
    
    requires static okio;

    opens org.test.frontend to javafx.fxml;
    exports org.test.frontend;
}