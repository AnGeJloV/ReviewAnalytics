package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.ClientApplication;
import com.github.stasangelov.reviewanalytics.client.service.SessionManager;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainContentPane;
    @FXML
    private Label viewTitle;
    @FXML
    private Button exportButton;
    @FXML
    private NavController navPanelController;

    private Object currentViewController;

    @FXML
    public void initialize() {
        // "Пробрасываем" ссылку на самого себя в NavController
        if (navPanelController != null) {
            navPanelController.setMainController(this);
        }

        // Показываем дашборд по умолчанию при запуске
        showDashboard();

        navPanelController.setActiveNavButton(navPanelController.dashboardNavButton);
    }

    // Эти методы теперь вызываются из NavController
    public void showDashboard() {
        loadView("dashboard-view.fxml", "Информационная панель");
    }

    public void showComparison() {
        loadView("comparison-view.fxml", "Сравнительный анализ");
    }

    public void showReviewManagement() {
        loadView("admin-view.fxml", "Управление отзывами");
    }

    public void showUserManagement() {
        loadView("user-management-view.fxml", "Управление пользователями");
    }

    private void loadView(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(ClientApplication.class.getResource(fxmlFile));
            mainContentPane.setCenter(loader.load());
            viewTitle.setText(title);

            currentViewController = loader.getController();

            exportButton.setVisible(
                    currentViewController instanceof DashboardController ||
                            currentViewController instanceof ComparisonController ||
                            currentViewController instanceof ProductDetailsController
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void exportCurrentViewToPdf(ActionEvent event) {
        if (currentViewController instanceof DashboardController) {
            ((DashboardController) currentViewController).exportDashboardToPdf(event);
        } else if (currentViewController instanceof ComparisonController) {
            ((ComparisonController) currentViewController).exportViewToPdf(event);
        }
    }

    public void onLogout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
        Stage currentStage = (Stage) mainContentPane.getScene().getWindow();
        ViewSwitcher.switchScene(currentStage, "login-view.fxml");
    }
}