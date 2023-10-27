package com.glencoesoftware.convert.dialogs;

import com.glencoesoftware.convert.App;
import com.glencoesoftware.convert.tasks.BaseTask;
import com.glencoesoftware.convert.workflows.BaseWorkflow;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.prefs.BackingStoreException;

public class ConfigureJobDialog {

    private final ch.qos.logback.classic.Logger LOGGER =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(this.getClass());

    private boolean multiMode = false;

    private ObservableList<BaseTask> tasks = null;
    private int taskIndex = 0;
    private BaseTask currentTask = null;
    private ObservableList<BaseWorkflow> jobs;
    @FXML
    private BorderPane configureJob;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private VBox standardSettings;
    @FXML
    private VBox advancedSettings;
    @FXML
    private Accordion advancedPane;
    @FXML
    private Label mainLabel;
    @FXML
    private Label taskLabel;
    @FXML
    private TitledPane advancedExpando;

    public void initialize() {
        HBox expandoHeader = new HBox(5);
        expandoHeader.setMaxWidth(Double.MAX_VALUE);
        expandoHeader.setAlignment(Pos.CENTER_LEFT);
        Label expandoText = new Label("Show Advanced Settings...");
        expandoText.getStyleClass().add("expando-title");
        Region expandoSpacer = new Region();
        FontIcon collapseButton = new FontIcon("bi-caret-down-fill");
        expandoHeader.getChildren().addAll(expandoText, expandoSpacer, collapseButton);
        HBox.setHgrow(expandoSpacer, Priority.ALWAYS);
        expandoHeader.setPrefWidth(350);
        advancedExpando.expandedProperty().addListener((e, ov, nv) -> {
            if (nv) {
                expandoText.setText("Hide advanced settings...");
                collapseButton.setIconLiteral("bi-caret-up-fill");
            } else {
                expandoText.setText("Show advanced settings...");
                collapseButton.setIconLiteral("bi-caret-down-fill");
            }
        });
        advancedExpando.setGraphic(expandoHeader);
        advancedExpando.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    }

    private void resetDialog() {
        this.jobs = null;
        standardSettings.getChildren().clear();
        advancedSettings.getChildren().clear();
        standardSettings.setFillWidth(true);
        standardSettings.setMaxWidth(Double.MAX_VALUE);
        this.tasks = null;
        this.taskIndex = 0;
        this.currentTask = null;
        prevButton.setDisable(true);
        nextButton.setDisable(false);
}

    public void initData(ObservableList<BaseWorkflow> jobs, int taskIndex) {
        resetDialog();
        this.jobs = jobs;
        if (jobs.isEmpty()) {
            System.out.println("No jobs given?");
            return;
        }
        multiMode = jobs.size() > 1;


        BaseWorkflow jobSample = this.jobs.get(0);
        if (multiMode) mainLabel.setText("Configuring %d jobs".formatted(this.jobs.size()));
        else mainLabel.setText("Configuring %s".formatted(jobSample.firstInput.getName()));

        this.tasks = jobSample.tasks;
        jobSample.prepareGUI();
        this.taskIndex = taskIndex;
        displayTaskSettings();
    }

    private void displayTaskSettings() {
        standardSettings.getChildren().clear();
        advancedSettings.getChildren().clear();
        prevButton.setDisable(this.taskIndex == 0);
        nextButton.setDisable(this.taskIndex == tasks.size() - 1);
        this.currentTask = tasks.get(this.taskIndex);
        this.taskLabel.setText("Settings for: %s".formatted(this.currentTask.getName()));
        ArrayList<Node> baseSettings = this.currentTask.getStandardSettings();
        ArrayList<Node> advSettings = this.currentTask.getAdvancedSettings();
        if (baseSettings != null) standardSettings.getChildren().addAll(baseSettings);
        if (advSettings != null) {
            advancedSettings.getChildren().addAll(advSettings);
            advancedPane.setVisible(true);
        } else {
            advancedPane.setVisible(false);
        }

    }
    @FXML
    private void applySettings() {
        System.out.println("Applying settings");
        // Different handling if configuring multiple jobs
        if (multiMode) {
            // Iterate through tasks
            for (int i = 0; i < this.tasks.size(); i++) {
                // Fetch settings from the template task used in the dialog
                BaseTask task = this.tasks.get(i);
                // Remember to apply settings to the first task
                task.applySettings();
                task.updateStatus();
                // Iterate through the other selected jobs and apply the same settings
                for (int j = 1; j < this.jobs.size(); j++) {
                    BaseWorkflow otherJob = this.jobs.get(j);
                    BaseTask otherTask = otherJob.tasks.get(i);
                    otherTask.cloneValues(task);
                    otherTask.applySettings();
                    otherTask.updateStatus();
                }
            }
            System.out.println("Done");
            return;
        }
        // If there's just one task we do it this easy way.
        for (BaseTask task : this.tasks) {
            task.applySettings();
            task.updateStatus();
        }
        System.out.println("Done");
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) configureJob.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void nextTask() {
        this.taskIndex += 1;
        displayTaskSettings();
    }
    @FXML
    private void prevTask() {
        this.taskIndex -= 1;
        displayTaskSettings();
    }
    @FXML
    private void restoreDefaults() {
        ButtonType thisTask = new ButtonType("This Task");
        ButtonType allTasks = new ButtonType("All Tasks");
        Alert choice = new Alert(Alert.AlertType.INFORMATION,
                "Reset settings for this task (%s) or all tasks?".formatted(currentTask.getName()),
                ButtonType.CANCEL, thisTask, allTasks
        );
        choice.setTitle("Restore defaults");
        choice.setHeaderText("Choose which settings to reset");
        choice.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(App.class.getResource("Alert.css")).toExternalForm());
        Button thisTaskButton = (Button) choice.getDialogPane().lookupButton(thisTask);
        thisTaskButton.setDefaultButton(true);
        choice.showAndWait().ifPresent(response -> {
            if (response == thisTask) {
                for (BaseWorkflow job: jobs) {
                    BaseTask task = job.tasks.get(taskIndex);
                    task.resetToDefaults();
                    task.updateStatus();
                }
            } if (response == allTasks) {
                for (BaseWorkflow job: jobs) {
                    for (BaseTask task : job.tasks) {
                        task.resetToDefaults();
                        task.updateStatus();
                    }
                }
            }
        });
        this.jobs.get(0).prepareGUI();
    }
    @FXML
    private void setDefaults() {
        ButtonType thisTask = new ButtonType("This Task");
        ButtonType allTasks = new ButtonType("All Tasks");
        Alert choice = new Alert(Alert.AlertType.INFORMATION,
                "Set defaults for this task (%s) or all tasks?".formatted(currentTask.getName()),
                ButtonType.CANCEL, thisTask, allTasks
        );
        choice.setTitle("Set defaults");
        choice.setHeaderText("Choose which settings to reset");
        choice.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(App.class.getResource("Alert.css")).toExternalForm());
        Button thisTaskButton = (Button) choice.getDialogPane().lookupButton(thisTask);
        thisTaskButton.setDefaultButton(true);
        choice.showAndWait().ifPresent(response -> {
            if (response == thisTask) {
                try {
                    currentTask.setDefaults();
                } catch (BackingStoreException e) {
                    LOGGER.error("Failed to set defaults: " + e);
                }
            } if (response == allTasks) {
                for (BaseTask task : tasks) {
                    try {
                        task.setDefaults();
                    } catch (BackingStoreException e) {
                        LOGGER.error("Failed to set defaults: " + e);
                    }
                }
            }
        });
    }
    @FXML
    private void applyToAll() {
        BaseWorkflow thisJob = currentTask.parent;
        Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
        choice.setTitle("Apply to all");
        choice.setHeaderText("Are you sure?");
        choice.setContentText(
                "Apply these settings to all '%s' jobs in the job list?".formatted(thisJob.getFullName())
        );
        choice.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(App.class.getResource("Alert.css")).toExternalForm());
        choice.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                List<BaseWorkflow> allJobs = currentTask.parent.controller.jobList.getItems();
                int count = 0;
                for (BaseWorkflow job: allJobs) {
                    if (job == thisJob) {
                        continue;
                    }
                    if (job.getClass().equals( thisJob.getClass())) {
                        LOGGER.info("Cloning values from " + thisJob.getFullName());
                        job.cloneSettings(thisJob);
                        count++;
                    }
                };
                LOGGER.info("Copied values to " + count + " instances");
            }
        });
    }
}
