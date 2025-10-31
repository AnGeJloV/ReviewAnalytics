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

/**
 * Главный контроллер приложения (`main-view.fxml`).
 * Выступает в роли "дирижера", управляя основной структурой окна:
 * - Загружает различные "виды" (views) в центральную область.
 * - Взаимодействует с контроллером навигационной панели {@link NavController}.
 * - Обрабатывает общие действия, такие как выход из системы и экспорт в PDF.
 */
public class MainController {

    // --- FXML Поля ---
    @FXML private BorderPane mainContentPane;
    @FXML private Label viewTitle;
    @FXML private Button exportButton;

    /**
     * Контроллер вложенной навигационной панели.
     * JavaFX автоматически внедряет его, так как fx:id="navPanel" в FXML.
     */
    @FXML private NavController navPanelController;

    /**
     * Хранит ссылку на контроллер текущего отображаемого вида (например, DashboardController).
     * Это позволяет вызывать методы дочерних контроллеров (например, для экспорта в PDF).
     */
    private Object currentViewController;

    //================================================================================
    // Инициализация
    //================================================================================

    /**
     * Вызывается после загрузки FXML-файла.
     * Устанавливает связь с NavController и загружает дашборд по умолчанию.
     */
    @FXML
    public void initialize() {
        if (navPanelController != null) {
            navPanelController.setMainController(this);
        }

        showDashboard();
        navPanelController.setActiveNavButton(navPanelController.dashboardNavButton);
    }

    //================================================================================
    // Управление навигацией (вызываются из NavController)
    //================================================================================

    /**
     * Загружает вид "Информационная панель" в центральную область.
     */
    public void showDashboard() {
        loadView("dashboard-view.fxml", "Информационная панель");
    }

    /**
     * Загружает вид "Сравнительный анализ" в центральную область.
     */
    public void showComparison() {
        loadView("comparison-view.fxml", "Сравнительный анализ");
    }

    /**
     * Загружает вид "Управление отзывами" в центральную область.
     */
    public void showReviewManagement() {
        loadView("admin-view.fxml", "Управление отзывами");
    }

    /**
     * Загружает вид "Управление пользователями" в центральную область.
     */
    public void showUserManagement() {
        loadView("user-management-view.fxml", "Управление пользователями");
    }

    //================================================================================
    // Обработчики общих действий
    //================================================================================

    /**
     * Обрабатывает нажатие на кнопку "Экспорт в PDF".
     * Делегирует выполнение экспорта контроллеру текущего активного вида.
     */
    @FXML
    void exportCurrentViewToPdf(ActionEvent event) {
        if (currentViewController instanceof DashboardController) {
            ((DashboardController) currentViewController).exportDashboardToPdf(event);
        } else if (currentViewController instanceof ComparisonController) {
            ((ComparisonController) currentViewController).exportViewToPdf(event);
        }
    }

    /**
     * Обрабатывает нажатие на кнопку "Выход".
     * Очищает сессию и возвращает пользователя на экран входа.
     */
    public void onLogout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
        Stage currentStage = (Stage) mainContentPane.getScene().getWindow();
        ViewSwitcher.switchScene(currentStage, "login-view.fxml");
    }

    //================================================================================
    // Вспомогательные методы
    //================================================================================

    /**
     * Универсальный метод для загрузки FXML-файла в центральную область {@code mainContentPane}.
     */
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
}