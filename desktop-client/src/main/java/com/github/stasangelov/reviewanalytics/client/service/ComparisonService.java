package com.github.stasangelov.reviewanalytics.client.service;

import com.github.stasangelov.reviewanalytics.client.model.analytics.product.ProductSummaryDto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.github.stasangelov.reviewanalytics.client.util.AlertFactory;

/**
 * Сервис-синглтон для управления "корзиной сравнения".
 * Хранит глобально доступный список товаров, которые пользователь выбрал для анализа.
 * Использует {@link ObservableList}, чтобы UI мог автоматически реагировать на изменения.
 */
public class ComparisonService {

    // --- Поля и синглтон ---
    private static ComparisonService instance;
    // Максимальное количество товаров, которые можно добавить в сравнение
    private static final int MAX_ITEMS = 4;
    // ObservableList будет автоматически уведомлять "слушателей" об изменениях
    private final ObservableList<ProductSummaryDto> itemsToCompare = FXCollections.observableArrayList();

    /**
     * Приватный конструктор для реализации паттерна "Синглтон".
     */
    private ComparisonService() {}

    /**
     * Возвращает единственный экземпляр сервиса.
     * Метод синхронизирован для потокобезопасности при первом создании.
     */
    public static synchronized ComparisonService getInstance() {
        if (instance == null) {
            instance = new ComparisonService();
        }
        return instance;
    }

    //================================================================================
    // Публичные методы API
    //================================================================================

    /**
     * Добавляет товар в список для сравнения.
     * Проверяет на соответствие категории и на превышение лимита.
     */
    public boolean addProduct(ProductSummaryDto product) {
        if (itemsToCompare.size() >= MAX_ITEMS) {
            AlertFactory.showWarning("Лимит достигнут", "Можно сравнивать не более " + MAX_ITEMS + " товаров одновременно.");
            return false;
        }
        if (!itemsToCompare.isEmpty()) {
            String firstCategory = itemsToCompare.get(0).getCategoryName();
            if (!product.getCategoryName().equals(firstCategory)) {
                AlertFactory.showWarning("Неверная категория", "Можно сравнивать только товары из одной категории (" + firstCategory + ").");
                return false;
            }
        }
        if (itemsToCompare.stream().anyMatch(p -> p.getProductId().equals(product.getProductId()))) {
            return true;
        }
        itemsToCompare.add(product);
        return true;
    }

    /**
     * Удаляет товар из списка сравнения.
     */
    public void removeProduct(ProductSummaryDto product) {
        itemsToCompare.removeIf(p -> p.getProductId().equals(product.getProductId()));
    }

    /**
     * Возвращает наблюдаемый (observable) список товаров для сравнения.
     * UI-компоненты могут подписываться на изменения этого списка.
     */
    public ObservableList<ProductSummaryDto> getItemsToCompare() {
        return itemsToCompare;
    }

    /**
     * Проверяет, находится ли товар с указанным ID уже в списке сравнения.
     */
    public boolean contains(Long productId) {
        return itemsToCompare.stream().anyMatch(p -> p.getProductId().equals(productId));
    }

    /**
     * Полностью очищает список товаров для сравнения.
     */
    public void clear() {
        itemsToCompare.clear();
    }
}
