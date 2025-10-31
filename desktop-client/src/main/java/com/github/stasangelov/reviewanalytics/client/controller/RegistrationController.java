package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.auth.RegistrationRequest;
import com.github.stasangelov.reviewanalytics.client.model.user.UserDto;
import com.github.stasangelov.reviewanalytics.client.service.ApiCallback;
import com.github.stasangelov.reviewanalytics.client.service.AuthService;
import com.github.stasangelov.reviewanalytics.client.util.AlertFactory;
import com.github.stasangelov.reviewanalytics.client.util.ViewSwitcher;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Контроллер для окна регистрации нового пользователя (`registration-view.fxml`).
 * Отвечает за сбор данных (имя, email, пароль), базовую валидацию,
 * отправку запроса на регистрацию и обработку ответа от сервера.
 */
public class RegistrationController {

    // --- FXML Поля ---
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label infoLabel;

    // --- Зависимости ---
    private final AuthService authService = new AuthService();

    //================================================================================
    // Обработчики событий (FXML)
    //================================================================================

    /**
     * Обрабатывает нажатие на кнопку "Зарегистрироваться".
     * Проверяет, что поля не пустые, создает DTO и отправляет запрос на сервер.
     */
    @FXML
    protected void onRegisterButtonClick(ActionEvent event) {
        // Сначала скрываем старые ошибки
        setInfoLabel(null);

        // Получаем и очищаем данные от лишних пробелов
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            setInfoLabel("Все поля должны быть заполнены");
            return;
        }

        RegistrationRequest request = new RegistrationRequest();
        request.setName(nameField.getText());
        request.setEmail(emailField.getText());
        request.setPassword(passwordField.getText());

        authService.register(request, new ApiCallback<>() {
            @Override
            public void onSuccess(UserDto result) {
                Platform.runLater(() -> {
                    AlertFactory.showInfo("Регистрация прошла успешно!", "Пользователь " + result.getName() + " был создан. Теперь вы можете войти.");
                    ViewSwitcher.switchScene(event, "login-view.fxml");
                });
            }

            @Override
            public void onFailure(Exception e) {
                Platform.runLater(() -> setInfoLabel("Ошибка сети: " + e.getMessage()));
            }

            @Override
            public void onError(int code, String message) {
                Platform.runLater(() -> setInfoLabel("Ошибка (" + code + "): " + message));
            }
        });
    }

    /**
     * Обрабатывает нажатие на гиперссылку "Уже есть аккаунт? Войти".
     * Переключает сцену на окно входа.
     */
    @FXML
    protected void onLoginLinkClick(ActionEvent event) {
        ViewSwitcher.switchScene(event, "login-view.fxml");
    }

    //================================================================================
    // Вспомогательные методы
    //================================================================================

    /**
     * Управляет видимостью и текстом метки для отображения ошибок (`infoLabel`).
     */
    private void setInfoLabel(String message) {
        if (message == null || message.isEmpty()) {
            infoLabel.setText("");
            infoLabel.setManaged(false);
            infoLabel.setVisible(false);
        } else {
            infoLabel.setText(message);
            infoLabel.setManaged(true);
            infoLabel.setVisible(true);
        }
    }
}