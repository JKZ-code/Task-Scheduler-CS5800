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
    private Button addD;


    private List<HBox> additionalFields = new ArrayList<>();
    private static final int MAX_FIELDS = 3;

    private String numbers;
    private String ids;
    private Map<Integer, TaskResponse> numberToTask = new HashMap<>();
    private ObservableList<TaskDisplay> taskDisplayList = FXCollections.observableArrayList();

    private Integer selectedTaskNumber = null;

    private TaskService taskService = new TaskService();

    private static final String BUTTONSTYLE = "-fx-background-color: linear-gradient(to bottom right, #9e20a0, #5a3375);\n" +
            "    -fx-text-fill: #fff;\n" +
            "    -fx-font-size: 14px;\n" +
            "    -fx-cursor: hand;" +
            "   -fx-pref-width: 37; " +
            "   -fx-pref-height: 35;";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //the button to add rows for dependencies input
        addD.setOnAction(event -> addNewRow());

        // the table to show added tasks
        showData();

        // the button to submit task
        addBtn.setOnAction(event ->addTask());

        // to update a task, click on the task in the table first to choose
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

        // when click delete button, delete the task
        deleteBtn.setOnAction(event -> deleteTask());

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
     * we allow at most 3 dependencies, when there are 3 dependencies, disable addD button
     */
    private void updateAddDState() {
        for (Node node : multiInputContainer.getChildren()){
            if (node instanceof HBox row) {
                for (Node child : row.getChildren()){
                    if (child instanceof Button btn && "+".equals(btn.getText())){
                        btn.setDisable(multiInputContainer.getChildren().size() >= MAX_FIELDS);
                    }
                }
            }
        }
    }

    /**
     * Add a new row to input dependencies, maximum dependencies number is 3
     */
    private void addNewRow(){
        if (multiInputContainer.getChildren().size() < MAX_FIELDS) {
            HBox newRow = createNewRow();
            multiInputContainer.getChildren().add(newRow);
            additionalFields.add(newRow);

            updateAddDState();
        }
    }

    private HBox createNewRow(){
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-pref-width: 150; -fx-pref-height: 37;");
        TextField nextD = new TextField();
        nextD.setStyle("-fx-pref-width: 84; -fx-pref-height: 35;");
        Button addMoreD = new Button("+");
        addMoreD.setStyle(BUTTONSTYLE);
        Button removeD = new Button("-");
        removeD.setStyle(BUTTONSTYLE);
        addMoreD.setOnAction(event -> addNewRow());
        removeD.setOnAction(event -> removeRow(row));
        row.getChildren().addAll(nextD, addMoreD, removeD);
        return row;
    }

    /**
     * the second and third row can be removed
     * @param row
     */
    private void removeRow(HBox row){
        multiInputContainer.getChildren().remove(row);
        additionalFields.remove(row);

        updateAddDState();
    }

    /**
     * Show added tasks in our Table View to help users choose dpendencies
     */
    private void showData(){
        col_number.setCellValueFactory(new PropertyValueFactory<>("number"));
        col_task.setCellValueFactory(new PropertyValueFactory<>("name"));
        col_priority.setCellValueFactory(new PropertyValueFactory<>("weight"));
        col_estimatedduration.setCellValueFactory(new PropertyValueFactory<>("estimatedDuration"));
        col_duedate.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        col_dependencies.setCellValueFactory(new PropertyValueFactory<>("dependencies"));
        tableView.setItems(taskDisplayList);
    }

    /**
     * gather dependencies entered by users and save relevant information in Str numbers and ids
     * numbers: String of task no. in current list (separated by ',') -- help users choose future dependencies
     * ids: String of task id returned from backend (separated by ',') -- send to backend
     */
    private void getAllDependencies(){
        StringBuilder numbers = new StringBuilder();
        StringBuilder ids = new StringBuilder();
        for (Node node : multiInputContainer.getChildren()) {
            if (node instanceof HBox row) {
                for (Node component : row.getChildren()) {
                    if (component instanceof TextField text) {
                        String value = text.getText().trim();

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

    private void verifyVals(){
        if (task.getText() == null || task.getText().trim().isEmpty()) {
            showAlert("Task name cannot be empty.");
            return;
        }

        if (weight.getText() == null || weight.getText().trim().isEmpty()) {
            showAlert("Weight cannot be empty.");
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

        if (duedate.getValue() == null) {
            showAlert("Please select a due date.");
            return;
        }

        if (estimatedduration.getText() == null || estimatedduration.getText().trim().isEmpty()) {
            showAlert("Estimated duration cannot be empty.");
        }
        try{
            Integer.parseInt(estimatedduration.getText());
        }catch(NumberFormatException e){
            showAlert("Eatimated duration must be a valid number.");
        }
    }

    private Task formTask() {
        verifyVals();
        String taskName = task.getText();
        int weightVal= Integer.parseInt(weight.getText());
        LocalDate dueDateVal = duedate.getValue();
        int durationVal = Integer.parseInt(estimatedduration.getText());

        return new Task(
                taskName,
                weightVal,
                durationVal,
                dueDateVal,
                this.ids
        );
    }

    /**
     * Send one task to backend
     * and add to map with the id returned from backend
     */
    private void addTask() {
        // save input dependencies
        getAllDependencies();
        try{
            Task curTask = formTask();

            // call backend api to save task
            TaskResponse returnedTask = taskService.createTask(curTask);

            // for display and future operation purpose, save one more record to a map
            int taskNumber = numberToTask.size() + 1;
            numberToTask.put(taskNumber, returnedTask);

            //if save successfully to backend, display at our frontend TableView
            taskDisplayList.add(new TaskDisplay(taskNumber, curTask, numbers));
            showAlert("Task successfully added!");

            clearFields();
        } catch (Exception e) {
            showAlert("Failed to submit task: " + e.getMessage());
        }
    }

    /**
     * used when we update a record, click a row twice in our Table View to update that record
     * after click, the contents of this task will be populated back to our input fields
     * @param selected
     */
    private void handleRowClick(TaskDisplay selected) {
        this.selectedTaskNumber = selected.getNumber();
        this.task.setText(selected.getName());
        this.weight.setText(String.valueOf(selected.getWeight()));
        this.duedate.setValue(LocalDate.parse(selected.getDueDate()));
        this.estimatedduration.setText(String.valueOf(selected.getEstimatedDuration()));
        System.out.println(selected.getDependencies());
        populateDependencies(selected.getDependencies());
    }

    private void populateDependencies(String dependencies) {
        multiInputContainer.getChildren().clear();

        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        String[] deps = dependencies.split(",");

        for (int i = 0; i < deps.length; i++) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-pref-width: 150; -fx-pref-height: 37;");
            TextField nextD = new TextField(deps[i].trim());
            Button addMoreD = new Button("+");
            addMoreD.setStyle(BUTTONSTYLE);
            Button removeD = new Button("-");
            removeD.setStyle(BUTTONSTYLE);
            removeD.setOnAction(event -> removeRow(row));
            row.getChildren().addAll(nextD, addMoreD, removeD);
            multiInputContainer.getChildren().add(row);
            addMoreD.setOnAction(event -> addNewRow());
            updateAddDState();
        }

    }


    /**
     * update a task
     */
    private void updateTask() {
        if (selectedTaskNumber == null) {
            showAlert("Please select a task.");
            return;
        }
        getAllDependencies();
        try{
            Task updatedTask = formTask();

            Long curId = numberToTask.get(selectedTaskNumber).getId();

            // call bakend api to update task saved in database
            TaskResponse returnedTask = taskService.updateTask(updatedTask, curId);

            // update task saved in our map
            numberToTask.put(selectedTaskNumber, returnedTask);

            // update the task in our tableView
            for (int i = 0; i < taskDisplayList.size(); i++) {
                if (taskDisplayList.get(i).getNumber() == selectedTaskNumber) {
                    TaskDisplay updatedTaskDisplay = new TaskDisplay(selectedTaskNumber, updatedTask, numbers);
                    taskDisplayList.set(i, updatedTaskDisplay);
                    break;
                }
            }

            clearFields();
        } catch (Exception e) {
            showAlert("Failed to update task: " + e.getMessage());
        }
        selectedTaskNumber = null;
    }


    /**
     * delete a task
     */
    private void deleteTask() {
        if (selectedTaskNumber == null) {
            showAlert("Please select a task.");
            return;
        }

        try {
            Long curId = numberToTask.get(selectedTaskNumber).getId();

            // delete from backend
            taskService.deleteTask(curId);

            //delete from our map
            numberToTask.remove(selectedTaskNumber);

            //delete from tableView
            taskDisplayList.removeIf(task ->
                task.getNumber() == selectedTaskNumber
            );

            clearFields();
        } catch (Exception e) {
            showAlert("Failed to delete task: " + e.getMessage());
        }
        selectedTaskNumber = null;
    }

    private void switchToResultPage(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("result-display.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Clear inputs after submit
     */
    private void clearFields() {
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
                if (node instanceof TextField text) {
                    text.clear();
                }
            }
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
