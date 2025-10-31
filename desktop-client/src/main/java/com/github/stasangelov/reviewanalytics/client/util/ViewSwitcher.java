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
     * Переключает сцену в указанном окне.
     * Это основной, универсальный метод для смены вида.
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
     * Удобный метод-обертка, который извлекает Stage из события (например, нажатия кнопки)
     * и вызывает основной метод `switchScene`.
     */
    public static void switchScene(ActionEvent event, String fxmlFile) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        switchScene(stage, fxmlFile);
    }

    /**
     * Специализированный метод для перехода на главный экран приложения.
     * Загружает `main-view.fxml`, устанавливает предопределенный размер окна и заголовок.
     */
    public static void switchToMainView(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1300, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("ReviewAnalytics - Панель управления");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Показывает новое модальное (всплывающее) окно поверх текущего.
     * Основное окно блокируется до тех пор, пока модальное не будет закрыто.
     */
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