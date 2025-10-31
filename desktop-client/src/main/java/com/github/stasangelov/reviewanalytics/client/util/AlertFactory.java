package com.github.stasangelov.reviewanalytics.client.util;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.SVGPath;

import java.net.URL;

public class AlertFactory {

    private static void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);

        DialogPane dialogPane = alert.getDialogPane();
        URL cssUrl = ClientApplication.class.getResource("dashboard.css");
        if (cssUrl != null) {
            dialogPane.getStylesheets().add(cssUrl.toExternalForm());
        }
        dialogPane.getStyleClass().add("custom-alert");

        SVGPath icon = new SVGPath();
        Label headerLabel = new Label(header);
        headerLabel.getStyleClass().add("header-label");

        // Выбираем иконку в зависимости от типа алерта
        switch (alertType) {
            case WARNING:
            case ERROR:
                icon.setContent("M13,14H11V10H13M13,18H11V16H13M1,21H23L12,2L1,21Z"); // Знак восклицания в треугольнике
                break;
            case INFORMATION:
            default:
                icon.setContent("M11,9H13V7H11M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M11,17H13V11H11V17Z"); // Знак 'i' в круге
                break;
        }

        HBox headerBox = new HBox(10, icon, headerLabel);
        HBox.setHgrow(headerLabel, Priority.ALWAYS);
        alert.getDialogPane().setHeader(headerBox);
        alert.setContentText(content);

        alert.setGraphic(null);
        alert.showAndWait();
    }

    public static void showWarning(String header, String content) {
        showAlert(Alert.AlertType.WARNING, "Внимание", header, content);
    }

    public static void showError(String header, String content) {
        showAlert(Alert.AlertType.ERROR, "Ошибка", header, content);
    }

    public static void showInfo(String header, String content) {
        showAlert(Alert.AlertType.INFORMATION, "Информация", header, content);
    }
}