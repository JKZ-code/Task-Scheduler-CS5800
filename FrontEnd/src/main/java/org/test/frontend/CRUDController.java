package org.test.frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CRUDController implements Initializable {

    @FXML
    private Button addBtn;

    @FXML
    private TableColumn<?, ?> col_dependencies;

    @FXML
    private TableColumn<?, ?> col_duedate;

    @FXML
    private TableColumn<?, ?> col_estimatedduration;

    @FXML
    private TableColumn<?, ?> col_number;

    @FXML
    private TableColumn<?, ?> col_priority;

    @FXML
    private TableColumn<?, ?> col_task;

    @FXML
    private Button deleteBtn;

    @FXML
    private TextField dependencies;

    @FXML
    private TextField duedate;

    @FXML
    private TextField estimatedduration;

    @FXML
    private TextField priority;

    @FXML
    private Button scheduleBtn;

    @FXML
    private TextField task;

    @FXML
    private Button updateBtn;

    @FXML
    private TableView<TaskDisplay> tableView;

    private TaskService taskService = new TaskService();
    private Map<Integer, Task> taskMap = new HashMap<>();
    private ObservableList<TaskDisplay> taskDisplayList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        col_number.setCellValueFactory(new PropertyValueFactory<>("number"));
        col_task.setCellValueFactory(new PropertyValueFactory<>("task"));
        col_priority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        col_estimatedduration.setCellValueFactory(new PropertyValueFactory<>("estimatedDuration"));
        col_duedate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        col_dependencies.setCellValueFactory(new PropertyValueFactory<>("dependencies"));

        tableView.setItems(taskDisplayList);
        addBtn.setOnAction(event ->addTask());

    }

    private void addTask() {
        try{
            int number = 0;
            if (dependencies != null && dependencies.getText() != null && !dependencies.getText().isEmpty()){
                number = Integer.parseInt(dependencies.getText());
            }
            String pre = number == 0? "" : taskMap.get(number).getTask();
            Task curTask = new Task(
                    task.getText(),
                    priority.getText(),
                    estimatedduration.getText(),
                    duedate.getText(),
                    pre
            );
            int taskNumber = taskMap.size() + 1;
            taskMap.put(taskNumber, curTask);
            taskDisplayList.add(new TaskDisplay(taskNumber, curTask));
            showAlert("Task successfully added!");

            // Clear the input fields
            task.clear();
            priority.clear();
            estimatedduration.clear();
            duedate.clear();
            dependencies.clear();

        } catch (Exception e) {
            showAlert("Failed to submit task: " + e.getMessage());
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
