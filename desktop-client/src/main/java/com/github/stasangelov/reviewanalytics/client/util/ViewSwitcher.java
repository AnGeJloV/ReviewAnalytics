package com.github.stasangelov.reviewanalytics.client.util;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Класс-утилита, предназначенный для упрощения навигации между различными сценами (окнами) в приложении.
 * Предоставляет статические методы для загрузки FXML-файлов и их установки в текущее окно (Stage),
 * скрывая повторяющийся код, связанный с переключением.
 */

public class ViewSwitcher {

    public static void switchScene(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchToMainView(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("ReviewAnalytics - Панель управления");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}