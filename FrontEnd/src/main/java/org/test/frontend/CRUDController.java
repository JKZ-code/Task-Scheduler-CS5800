package org.test.frontend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

public class CRUDController implements Initializable {

    @FXML
    private Button addBtn;

    @FXML
    private TableColumn<TaskDisplay, String> col_dependencies;

    @FXML
    private TableColumn<TaskDisplay, String> col_duedate;

    @FXML
    private TableColumn<TaskDisplay, Integer> col_estimatedduration;

    @FXML
    private TableColumn<TaskDisplay, Integer> col_number;

    @FXML
    private TableColumn<TaskDisplay, Integer> col_priority;

    @FXML
    private TableColumn<TaskDisplay, String> col_task;

    @FXML
    private Button deleteBtn;

    @FXML
    private DatePicker duedate;

    @FXML
    private TextField estimatedduration;

    @FXML
    private TextField weight;

    @FXML
    private Button scheduleBtn;

    @FXML
    private TextField task;

    @FXML
    private Button updateBtn;

    @FXML
    private TableView<TaskDisplay> tableView;

    @FXML
    private VBox multiInputContainer;

    @FXML
    private TextField firstD;

    @FXML
    private HBox initialHBox;

    @FXML
    private Button addD;


    private List<HBox> additionalFields = new ArrayList<>();
    private static final int MAX_FIELDS = 3;

    private String numbers;
    private String ids;
    private Map<Integer, TaskResponse> numberToTask = new HashMap<>();
    private ObservableList<TaskDisplay> taskDisplayList = FXCollections.observableArrayList();

    private Integer selectedTaskNumber = null;


    private TaskService taskService = new TaskService();
    private TaskService2 taskService2 = new TaskService2();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //the button to add rows for dependencies input
        addD.setOnAction(event -> addNewRow());

        // the table to show added tasks
        col_number.setCellValueFactory(new PropertyValueFactory<>("number"));
        col_task.setCellValueFactory(new PropertyValueFactory<>("name"));
        col_priority.setCellValueFactory(new PropertyValueFactory<>("weight"));
        col_estimatedduration.setCellValueFactory(new PropertyValueFactory<>("estimatedDuration"));
        col_duedate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        col_dependencies.setCellValueFactory(new PropertyValueFactory<>("dependencies"));
        tableView.setItems(taskDisplayList);

        // the button to submit task
        addBtn.setOnAction(event ->addTask());

        // to update a task, first click on the task in the table first
        tableView.setRowFactory(tv -> {
            TableRow<TaskDisplay> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    TaskDisplay selected = row.getItem();
                    handleRowClick(selected);
                }
            });
            return row;
        });

        // when click update button, update the task content
        updateBtn.setOnAction(event -> updateTask());

        // the button to show the sheduled result
        scheduleBtn.setOnAction(event -> {
            try{
                switchToResultPage(event);
            } catch (IOException e) {
                showAlert("Failed to show result: " + e.getMessage());
            }
        });
    }


    /**
     * Add a new row to input dependencies, maximum dependencies number is 3
     */
    private void addNewRow(){
        if (multiInputContainer.getChildren().size() < MAX_FIELDS) {
            HBox newRow = createNewRow();
            multiInputContainer.getChildren().add(newRow);
            additionalFields.add(newRow);

            if (multiInputContainer.getChildren().size() >= MAX_FIELDS) {
                addD.setDisable(true);
            }
        }
    }

    private HBox createNewRow(){
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-pref-width: 150; -fx-pref-height: 37;");
        TextField nextD = new TextField();
        nextD.setStyle("-fx-pref-width: 84; -fx-pref-height: 35;");
        Button addD = new Button("+");
        addD.setStyle("-fx-background-color: linear-gradient(to bottom right, #9e20a0, #5a3375);\n" +
                "    -fx-text-fill: #fff;\n" +
                "    -fx-font-size: 14px;\n" +
                "    -fx-cursor: hand;" +
                "   -fx-pref-width: 37; " +
                "   -fx-pref-height: 35;");
        Button removeD = new Button("-");
        removeD.setStyle("-fx-background-color: linear-gradient(to bottom right, #9e20a0, #5a3375);\n" +
                "    -fx-text-fill: #fff;\n" +
                "    -fx-font-size: 14px;\n" +
                "    -fx-cursor: hand; " +
                "   -fx-pref-width: 37;" +
                "   -fx-pref-height: 35;");
        addD.setOnAction(event -> addNewRow());
        removeD.setOnAction(event -> removeRow(row));
        row.getChildren().addAll(nextD, addD, removeD);
        return row;
    }

    /**
     * the second and third row can be removed
     * @param row
     */
    private void removeRow(HBox row){
        multiInputContainer.getChildren().remove(row);
        additionalFields.remove(row);
        if (multiInputContainer.getChildren().size() < MAX_FIELDS) {
            addD.setDisable(false);
        }
    }

    /**
     * gather dependencies entered by users and save relevant information in Str numbers and ids
     * numbers: String of task no. in current list (separated by ',') -- help users choose future dependencies
     * ids: String of task id returned from backend (separated by ',') -- send to backend
     */
    private void getAllValues(){
        StringBuilder numbers = new StringBuilder();
        StringBuilder ids = new StringBuilder();
        for (Node node : multiInputContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                for (Node component : row.getChildren()) {
                    if (component instanceof TextField) {
                        TextField textField = (TextField) component;
                        String value = textField.getText().trim();

                        if (!value.isEmpty()) {
                            int key = Integer.parseInt(value);
                            if (!numberToTask.containsKey(key)) {
                                showAlert("No such task found.");
                                return;
                            }

                            if (!numbers.isEmpty()) {
                                numbers.append(",");
                            }
                            numbers.append(value);
                            Long curId = numberToTask.get(key).getId();
                            if (!ids.isEmpty()) ids.append(",");
                            ids.append(curId);
                        }
                        }
                        break;
                    }
                }
            }
        this.numbers = numbers.toString();
        this.ids = ids.toString();
    }

    /**
     * Send one record to backend and add to map with the id returned from backend
     */
    private void addTask() {
        getAllValues();
        try{
            if (task.getText().trim().isEmpty()) {
                showAlert("Task name cannot be empty.");
                return;
            }
            int weightVal;
            try{
                weightVal = Integer.parseInt(weight.getText());
            }catch(NumberFormatException e){
                showAlert("Task weight must be a valid integer.");
                return;
            }
            if (weightVal < 1 || weightVal > 10) {
                showAlert("Task weight must between 1 and 10.");
            }

            LocalDate dueDateVal = duedate.getValue();
            if (dueDateVal == null) {
                showAlert("Please select a due date.");
                return;
            }

            int durationVal;
            try{
                durationVal = Integer.parseInt(estimatedduration.getText());
            }catch(NumberFormatException e){
                showAlert("Eatimated duration must be a valid number.");
                return;
            }
//            System.out.println("ids: " + ids);
//            System.out.println("numbers: " + numbers);

            Task curTask = new Task(
                    task.getText(),
                    weightVal,
                    durationVal,
                    dueDateVal,
                    ids
            );

            TaskResponse returnedTask = taskService.createTask(curTask);
            //TaskResponse returnedTask = taskService2.post(curTask);
            int taskNumber = numberToTask.size() + 1;
            numberToTask.put(taskNumber, returnedTask);
//            for (int key : numberToTask.keySet()) {
//                System.out.println("key: " + key);
//                TaskResponse cur = numberToTask.get(key);
//                System.out.println("name:" + cur.getName());
//                System.out.println("id " + cur.getId());
//            }
            taskDisplayList.add(new TaskDisplay(taskNumber, curTask, numbers));
            showAlert("Task successfully added!");

            //clear input form after one submission
            task.clear();
            weight.clear();
            estimatedduration.clear();
            duedate.setValue(null);
            ObservableList<Node> children = multiInputContainer.getChildren();
            for (int i = 1; i < children.size(); i++) {// Remove all HBoxes except the first one
                if (children.get(i) instanceof HBox) {
                    children.remove(i);
                    i--;
                }
            }
            if (!children.isEmpty()) {// Clear the TextField in the first (or remaining) HBox
                HBox remainingHBox = (HBox) children.get(0); // Get the first HBox
                for (Node node : remainingHBox.getChildren()) {
                    if (node instanceof TextField) {
                        TextField textField = (TextField) node;
                        textField.clear();
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Failed to submit task: " + e.getMessage());
        }
    }

    private void handleRowClick(TaskDisplay selected) {
        this.selectedTaskNumber = selected.getNumber();
        this.task.setText(selected.getName());
        this.weight.setText(String.valueOf(selected.getWeight()));
        this.duedate.setValue(LocalDate.parse(selected.getDueDate()));
        this.estimatedduration.setText(String.valueOf(selected.getEstimatedDuration()));
        populateDependencies(selected.getDependencies());
    }

    private void populateDependencies(String dependencies) {
        multiInputContainer.getChildren().clear();

        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        String[] deps = dependencies.split(",");
        firstD.setText(deps[0]);
        for (int i = 1; i < deps.length; i++) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-pref-width: 150; -fx-pref-height: 37;");
            TextField nextD = new TextField(deps[i].trim());
            Button removeD = new Button("-");
            removeD.setStyle("-fx-background-color: linear-gradient(to bottom right, #9e20a0, #5a3375);\n" +
                    "    -fx-text-fill: #fff;\n" +
                    "    -fx-font-size: 14px;\n" +
                    "    -fx-cursor: hand; " +
                    "   -fx-pref-width: 37;" +
                    "   -fx-pref-height: 35;");
            removeD.setOnAction(event -> removeRow(row));
            row.getChildren().addAll(nextD, removeD);
            multiInputContainer.getChildren().add(row);
        }
    }

    private void updateTask() {
        if (selectedTaskNumber == null) {
            showAlert("Please select a task.");
            return;
        }
        getAllValues();
        try{
            if (task.getText().trim().isEmpty()) {
                showAlert("Task name cannot be empty.");
                return;
            }
            int weightVal;
            try{
                weightVal = Integer.parseInt(weight.getText());
            }catch(NumberFormatException e){
                showAlert("Task weight must be a valid integer.");
                return;
            }
            if (weightVal < 1 || weightVal > 10) {
                showAlert("Task weight must between 1 and 10.");
            }

            LocalDate dueDateVal = duedate.getValue();
            if (dueDateVal == null) {
                showAlert("Please select a due date.");
                return;
            }

            int durationVal;
            try{
                durationVal = Integer.parseInt(estimatedduration.getText());
            }catch(NumberFormatException e){
                showAlert("Eatimated duration must be a valid number.");
                return;
            }

            Task updatedTask = new Task(
                    task.getText(),
                    weightVal,
                    durationVal,
                    dueDateVal,
                    ids
            );

            Long curId = numberToTask.get(selectedTaskNumber).getId();
            TaskResponse returnedTask = taskService.updateTask(updatedTask, curId);
            numberToTask.put(selectedTaskNumber, returnedTask);
            for (int i = 0; i < taskDisplayList.size(); i++) {
                if (taskDisplayList.get(i).getNumber() == selectedTaskNumber) {
                    TaskDisplay updatedTaskDisplay = new TaskDisplay(selectedTaskNumber, updatedTask, numbers);
                    taskDisplayList.set(i, updatedTaskDisplay);
                    break;
                }
            }

            //clear input form after one submission
            task.clear();
            weight.clear();
            estimatedduration.clear();
            duedate.setValue(null);
            ObservableList<Node> children = multiInputContainer.getChildren();
            for (int i = 1; i < children.size(); i++) {// Remove all HBoxes except the first one
                if (children.get(i) instanceof HBox) {
                    children.remove(i);
                    i--;
                }
            }
            if (!children.isEmpty()) {// Clear the TextField in the first (or remaining) HBox
                HBox remainingHBox = (HBox) children.get(0); // Get the first HBox
                for (Node node : remainingHBox.getChildren()) {
                    if (node instanceof TextField) {
                        TextField textField = (TextField) node;
                        textField.clear();
                    }
                }
            }
        } catch (Exception e) {
            showAlert("Failed to update task: " + e.getMessage());
        }

        selectedTaskNumber = null;
    }

    private void switchToResultPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/test/frontend/result-display.fxml"));
        System.out.println(getClass().getResource("/org/test/frontend/result-display.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
