package com.github.stasangelov.reviewanalytics.client.service;

import com.github.stasangelov.reviewanalytics.client.model.ProductSummaryDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

/**
 * Сервис-синглтон для управления "корзиной сравнения".
 * Хранит список товаров, которые пользователь выбрал для сравнения.
 * Использует ObservableList, чтобы UI мог "слушать" изменения в списке.
 */
public class ComparisonService {

    private static ComparisonService instance;

    // Максимальное количество товаров, которые можно добавить в сравнение
    private static final int MAX_ITEMS = 4;

    // ObservableList будет автоматически уведомлять "слушателей" об изменениях
    private final ObservableList<ProductSummaryDto> itemsToCompare = FXCollections.observableArrayList();

    private ComparisonService() {}

    /**
     * Возвращает единственный экземпляр сервиса.
     */
    public static synchronized ComparisonService getInstance() {
        if (instance == null) {
            instance = new ComparisonService();
        }
        return instance;
    }

    /**
     * Добавляет товар в список для сравнения.
     * @param product Товар для добавления.
     * @return true, если товар был успешно добавлен, false в противном случае (например, если лимит достигнут).
     */
    public boolean addProduct(ProductSummaryDto product) {
        if (itemsToCompare.size() >= MAX_ITEMS) {
            showAlert("Лимит достигнут", "Можно сравнивать не более " + MAX_ITEMS + " товаров одновременно.");
            return false; // Лимит достигнут
        }
        if (!itemsToCompare.isEmpty()) {
            String firstCategory = itemsToCompare.get(0).getCategoryName();
            if (!product.getCategoryName().equals(firstCategory)) {
                showAlert("Неверная категория", "Можно сравнивать только товары из одной категории (" + firstCategory + ").");
                return false;
            }
        }
        if (itemsToCompare.stream().anyMatch(p -> p.getProductId().equals(product.getProductId()))) {
            return true; // Товар уже там, считаем это успехом
        }
        itemsToCompare.add(product);
        return true;
    }

    /**
     * Удаляет товар из списка сравнения.
     * @param product Товар для удаления.
     */
    public void removeProduct(ProductSummaryDto product) {
        itemsToCompare.removeIf(p -> p.getProductId().equals(product.getProductId()));
    }

    /**
     * Возвращает наблюдаемый список товаров для сравнения.
     * UI может привязаться к этому списку для автоматического обновления.
     */
    public ObservableList<ProductSummaryDto> getItemsToCompare() {
        return itemsToCompare;
    }

    /**
     * Проверяет, находится ли товар уже в списке сравнения.
     * @param productId ID товара для проверки.
     * @return true, если товар в списке.
     */
    public boolean contains(Long productId) {
        return itemsToCompare.stream().anyMatch(p -> p.getProductId().equals(productId));
    }

    /**
     * Очищает весь список сравнения.
     */
    public void clear() {
        itemsToCompare.clear();
    }

    /**
     * НОВЫЙ МЕТОД: Вспомогательный метод для показа Alert.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
