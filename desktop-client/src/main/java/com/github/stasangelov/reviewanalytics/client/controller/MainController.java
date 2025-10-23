package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.service.SessionManager;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;

import java.io.IOException;

public class MainController {

    @FXML
    private Menu adminMenu;

    @FXML
    public void initialize() {
        if (!SessionManager.getInstance().hasRole("ADMIN")) {
            adminMenu.setVisible(false);
        }
    }

    @FXML
    void onGoToAdminPanel(ActionEvent event) {
        try {
            ViewSwitcher.showModalWindow("admin-view.fxml", "Администрирование");
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть панель администрирования.");
            alert.showAndWait();
        }
    }

    @FXML
    void onLogout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
        ViewSwitcher.switchScene(event, "login-view.fxml");
    }
}