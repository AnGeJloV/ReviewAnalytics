package com.github.stasangelov.reviewanalytics.client.util;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Класс-утилита, предназначенный для упрощения навигации между различными сценами (окнами) в приложении.
 * Предоставляет статические методы для загрузки FXML-файлов и их установки в текущее окно (Stage),
 * скрывая повторяющийся код, связанный с переключением.
 */

public class ViewSwitcher {

    /**
     * Переключает сцену в указанном окне (Stage).
     * @param stage Окно, в котором нужно сменить сцену.
     * @param fxmlFile FXML-файл новой сцены.
     */
    public static void switchScene(Stage stage, String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource(fxmlFile));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Старый метод, который работает для событий от Node (например, кнопок).
     * Мы его оставим, так как он используется в других местах.
     */
    public static void switchScene(ActionEvent event, String fxmlFile) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        switchScene(stage, fxmlFile);
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

    public static void showModalWindow(String fxmlFile, String title) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource(fxmlFile));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}