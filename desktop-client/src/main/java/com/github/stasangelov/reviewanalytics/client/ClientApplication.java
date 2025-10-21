package com.github.stasangelov.reviewanalytics.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Главный класс, который служит точкой входа для JavaFX-приложения.
 * Его основная задача — инициализировать главное окно (Stage) и загрузить
 * самую первую сцену, которую увидит пользователь.
 */

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load()); // Размеры возьмутся из FXML
        stage.setTitle("Вход - ReviewAnalytics");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}