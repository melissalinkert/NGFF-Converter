package com.glencoesoftware.convert.tables;

import com.glencoesoftware.convert.JobState;
import com.glencoesoftware.convert.tasks.BaseTask;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class TaskStatusTableCell extends TableCell<BaseTask, Void> {
    private final Label mainLabel = new Label();

    private final Tooltip labelTooltip = new Tooltip("Ready");

    private final HBox container = new HBox();

    {
        container.setAlignment(Pos.CENTER);
        mainLabel.setTooltip(labelTooltip);
        mainLabel.getStyleClass().add("status-cell");
    }

    @Override
    public void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : container);
        if (empty) return;
        container.getChildren().clear();
        BaseTask current = getTableView().getItems().get(getIndex());
        labelTooltip.setText("Task OK");
        mainLabel.setText(current.getStatusString());
        switch (current.status) {
            case COMPLETED -> {
                mainLabel.setGraphic(JobState.getStatusIcon(current.status, 15));
                labelTooltip.setText("Task successful");
                container.getChildren().add(mainLabel);
            }
            case RUNNING -> container.getChildren().addAll(current.getProgressWidget());
            case WARNING -> {
                mainLabel.setGraphic(JobState.getStatusIcon(current.status, 15));
                labelTooltip.setText(current.warningMessage);
                container.getChildren().add(mainLabel);
            }
            case FAILED -> {
                labelTooltip.setText("Task Failed");
                mainLabel.setGraphic(JobState.getStatusIcon(current.status, 15));
                container.getChildren().add(mainLabel);
            }
            default -> {
                mainLabel.setGraphic(JobState.getStatusIcon(current.status, 15));
                container.getChildren().add(mainLabel);
            }
        }
    }
}