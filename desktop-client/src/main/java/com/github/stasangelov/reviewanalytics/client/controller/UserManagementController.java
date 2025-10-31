package com.github.stasangelov.reviewanalytics.client.controller;

import com.github.stasangelov.reviewanalytics.client.model.UserManagementDto;
import com.github.stasangelov.reviewanalytics.client.service.ApiException;
import com.github.stasangelov.reviewanalytics.client.service.UserService;
import com.github.stasangelov.reviewanalytics.client.util.AlertFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;

public class UserManagementController {

    @FXML private TableView<UserManagementDto> usersTable;
    @FXML private TableColumn<UserManagementDto, String> nameCol;
    @FXML private TableColumn<UserManagementDto, String> emailCol;
    @FXML private TableColumn<UserManagementDto, String> roleCol;
    @FXML private TableColumn<UserManagementDto, String> statusCol;
    @FXML private TableColumn<UserManagementDto, Void> actionRoleCol;
    @FXML private TableColumn<UserManagementDto, Void> actionStatusCol;

    private final UserService userService = new UserService();
    private final ObservableList<UserManagementDto> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
    }

    private void setupTable() {
        usersTable.setItems(userList);

        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        emailCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        roleCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().isActive() ? "Активен" : "Заблокирован"));

        // --- Колонка для смены роли ---
        actionRoleCol.setCellFactory(param -> new TableCell<>() {
            private final ComboBox<String> roleComboBox = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "ANALYST"));

            {
                roleComboBox.setOnAction(event -> {
                    UserManagementDto user = getTableView().getItems().get(getIndex());
                    String newRole = roleComboBox.getValue();
                    if (newRole != null && !newRole.equals(user.getRole())) {
                        changeUserRole(user, newRole);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserManagementDto user = getTableView().getItems().get(getIndex());
                    roleComboBox.setValue(user.getRole());
                    setGraphic(roleComboBox);
                }
            }
        });

        // --- Колонка для смены статуса ---
        actionStatusCol.setCellFactory(param -> new TableCell<>() {
            private final Button toggleStatusBtn = new Button();

            {
                toggleStatusBtn.setOnAction(event -> {
                    UserManagementDto user = getTableView().getItems().get(getIndex());
                    changeUserStatus(user, !user.isActive());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    UserManagementDto user = getTableView().getItems().get(getIndex());
                    toggleStatusBtn.setText(user.isActive() ? "Заблокировать" : "Разблокировать");
                    setGraphic(toggleStatusBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                final List<UserManagementDto> users = userService.getAllUsers();
                Platform.runLater(() -> userList.setAll(users));
            } catch (IOException e) {
                Platform.runLater(() -> AlertFactory.showError("Ошибка сети", e.getMessage()));
            }
        }).start();
    }

    private void changeUserRole(UserManagementDto user, String newRole) {
        new Thread(() -> {
            try {
                userService.changeRole(user.getId(), newRole);
                Platform.runLater(this::loadUsers); // Перезагружаем список для обновления
            } catch (ApiException e) {
                Platform.runLater(() -> AlertFactory.showError("Операция запрещена", e.getMessage()));
                Platform.runLater(this::loadUsers); // Откатываем ComboBox к старому значению
            } catch (IOException e) {
                Platform.runLater(() -> AlertFactory.showError("Ошибка сети", e.getMessage()));
            }
        }).start();
    }

    private void changeUserStatus(UserManagementDto user, boolean newStatus) {
        new Thread(() -> {
            try {
                userService.changeStatus(user.getId(), newStatus);
                Platform.runLater(this::loadUsers);
            } catch (ApiException e) {
                Platform.runLater(() -> AlertFactory.showError("Операция запрещена", e.getMessage()));
            } catch (IOException e) {
                Platform.runLater(() -> AlertFactory.showError("Ошибка сети", e.getMessage()));
            }
        }).start();
    }
}
