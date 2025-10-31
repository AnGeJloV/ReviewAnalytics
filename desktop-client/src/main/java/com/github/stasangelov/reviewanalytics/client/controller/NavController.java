package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import lombok.Setter;

/**
 * Контроллер для боковой навигационной панели (`main-nav.fxml`).
 * Отвечает исключительно за обработку нажатий на навигационные кнопки
 * и делегирование этих действий главному контроллеру {@link MainController}.
 */
public class NavController {

    // --- FXML Поля ---
    @FXML Button dashboardNavButton;
    @FXML private Button comparisonNavButton;
    @FXML private Button usersNavButton;
    @FXML private Button reviewsNavButton;

    //================================================================================
    // Инициализация и настройка
    //================================================================================

    /**
     * Вызывается после загрузки FXML-файла.
     * Проверяет роль текущего пользователя и скрывает административные кнопки,
     * если у пользователя нет прав доступа.
     */
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

    /**
     * Ссылка на главный контроллер для вызова его публичных методов (например, для смены вида).
     * -- SETTER --
     * Устанавливает ссылку на главный контроллер.
     * Этот метод вызывается из {@link MainController} после загрузки FXML.
     */
    @Setter
    private MainController mainController;

    //================================================================================
    // Обработчики событий (FXML)
    //================================================================================

    /**
     * Обрабатывает нажатие на кнопку "Дашборд".
     * Делегирует загрузку вида главному контроллеру и устанавливает эту кнопку как активную.
     */
    @FXML void showDashboard() {
        mainController.showDashboard();
        setActiveNavButton(dashboardNavButton);
    }

    /**
     * Обрабатывает нажатие на кнопку "Сравнение".
     */
    @FXML void showComparison() {
        mainController.showComparison();
        setActiveNavButton(comparisonNavButton);
    }

    /**
     * Обрабатывает нажатие на кнопку "Отзывы".
     */
    @FXML void showReviewManagement() {
        mainController.showReviewManagement();
        setActiveNavButton(reviewsNavButton);
    }

    /**
     * Обрабатывает нажатие на кнопку "Пользователи".
     */
    @FXML void showUserManagement() {
        mainController.showUserManagement();
        setActiveNavButton(usersNavButton);
    }

    /**
     * Обрабатывает нажатие на кнопку "Выход".
     * Делегирует действие главному контроллеру.
     */
    @FXML
    void onLogout(ActionEvent event) {
        mainController.onLogout(event);
    }

    //================================================================================
    // Вспомогательные методы
    //================================================================================

    /**
     * Управляет CSS-классами для визуального выделения активной навигационной кнопки.
     * Снимает выделение со всех кнопок и применяет стиль к одной, переданной в аргументе.
     */
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
}