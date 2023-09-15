package com.glencoesoftware.convert.tables;

import com.glencoesoftware.convert.tasks.BaseTask;
import javafx.scene.control.*;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;


public class ButtonTableCell extends TableCell<BaseTask, Void> {

    private final Button configureButton = new Button("");

    private final FontIcon cog = new FontIcon("bi-gear-fill");


    {
        cog.setIconSize(20);
        cog.setIconColor(Paint.valueOf("BLUE"));
        configureButton.setGraphic(cog);

        configureButton.setOnAction(ext -> {
            BaseTask task = getTableView().getItems().get(getIndex());
            System.out.println("Configuring task " + task.toString());
        });

    }

    @Override
    public void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : configureButton);
    }
}
