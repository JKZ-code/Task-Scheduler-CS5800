module org.test.frontend {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.test.frontend to javafx.fxml;
    exports org.test.frontend;
}