package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NavController {

    @FXML Button dashboardNavButton;
    @FXML private Button comparisonNavButton;
    @FXML private Button usersNavButton;
    @FXML private Button reviewsNavButton;

    // Ссылка на главный контроллер, чтобы вызывать его методы
    private MainController mainController;

    // Метод для "инъекции" главного контроллера
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // Скрываем кнопки для роли ANALYST
        if (!SessionManager.getInstance().hasRole("ADMIN")) {
            usersNavButton.setVisible(false);
            usersNavButton.setManaged(false);
            reviewsNavButton.setVisible(false);
            reviewsNavButton.setManaged(false);
        }
    }

    public void setActiveNavButton(Button activeButton) {
        // Сначала убираем активный стиль со всех кнопок
        dashboardNavButton.getStyleClass().remove("nav-button-active");
        comparisonNavButton.getStyleClass().remove("nav-button-active");
        reviewsNavButton.getStyleClass().remove("nav-button-active");
        usersNavButton.getStyleClass().remove("nav-button-active");

        // Добавляем активный стиль к нужной кнопке
        if (activeButton != null) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    @FXML void showDashboard() {
        mainController.showDashboard();
        setActiveNavButton(dashboardNavButton);
    }
    @FXML void showComparison() {
        mainController.showComparison();
        setActiveNavButton(comparisonNavButton);
    }
    @FXML void showReviewManagement() {
        mainController.showReviewManagement();
        setActiveNavButton(reviewsNavButton);
    }
    @FXML void showUserManagement() {
        mainController.showUserManagement();
        setActiveNavButton(usersNavButton);
    }

    @FXML
    void onLogout(ActionEvent event) {
        mainController.onLogout(event);
    }
}