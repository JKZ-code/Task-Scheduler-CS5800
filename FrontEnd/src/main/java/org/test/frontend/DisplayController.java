package org.test.frontend;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DisplayController implements Initializable {
    @FXML
    private VBox displayBox;

    private TaskService taskService = new TaskService();

    private static final String HBOXSTYLE = "-fx-pref-width: 617; -fx-pref-height: 46;";
    private static final String NOTEXTSTYLE = "-fx-pref-width: 46; -fx-pref-height: 46; " +
            "-fx-border-color: linear-gradient(to bottom right, #9e20a0, #5a3375);" +
            "-fx-border-radius: 8px;\n" +
            "-fx-border-width: 2px;";
    private static final String NAMETEXTSTYLE = "-fx-pref-width: 571; -fx-pref-height: 46;" +
            "-fx-border-color: linear-gradient(to bottom right, #9e20a0, #5a3375);" +
            "-fx-border-radius: 8px; -fx-border-width: 2px;" +
            "-fx-margin-left: 10px;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        scheduleTask();
    }

    private void scheduleTask() {
        displayBox.getChildren().clear();
        try{
            List<String> taskNames= taskService.getSchedule();
            if (taskNames == null || taskNames.isEmpty()) {
                return;
            }

            for (int i = 0; i < taskNames.size(); i++) {
                HBox row = new HBox();
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle(HBOXSTYLE);
                TextField noField = new TextField(String.valueOf(i+1));
                TextField nameField = new TextField(taskNames.get(i));
                noField.setStyle(NOTEXTSTYLE);
                nameField.setStyle(NAMETEXTSTYLE);
                row.getChildren().addAll(noField, nameField);
                displayBox.getChildren().add(row);
            }
        }catch(Exception e){
            showAlert("Failed to schedule: " + e.getMessage());
        }

    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
