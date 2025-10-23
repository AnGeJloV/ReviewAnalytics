package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.service.SessionManager;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Контроллер для главного окна приложения ('main-view.fxml').
 */

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
            ViewSwitcher.showModalWindow("admin-view.fxml", "Управление справочниками");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onLogout(ActionEvent event) {

        System.out.println("Выход...");
    }
}