package org.test.frontend;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DisplayController implements Initializable {
    @FXML
    private VBox displayBox;

    @FXML
    private Button backBtn;

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


    private ActionEvent eventRef;

    public void setEvent(ActionEvent event) {
        this.eventRef = event;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        scheduleTask();
        backBtn.setOnAction(this::switchToCRUDPage);
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
            showDecisionAlert("Failed to schedule: " + e.getMessage(), eventRef);
        }

    }

//    private void showAlert(String message) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Information");
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }

    private void showDecisionAlert(String message, ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Scheduling Failed");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n\nDo you want to update your tasks?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");

        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == yesButton) {
            switchToCRUDPage(event);
        } else {
            Platform.exit();
        }
    }

    private void switchToCRUDPage(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/test/frontend/task-crud.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Task Editor");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
