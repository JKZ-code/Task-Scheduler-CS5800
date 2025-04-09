package org.test.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DisplayController {
    @FXML
    private VBox displayBox;

    @FXML
    private HBox displayRow;

    @FXML
    private TextField firstNumber;

    @FXML
    private TextField firstTask;

    private TaskService taskService;

}
