package com.sellion.mobile.model;

import java.math.BigDecimal;
import java.util.Map;

public class PromoAction {
    private Long id;
    private String title;
    private String managerId;
    private String endDate; // Передаем строкой для простоты парсинга из JSON
    private Map<Long, BigDecimal> items; // ID товара -> Процент акции

    public PromoAction() {}

    // Геттеры
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getEndDate() { return endDate; }
    public Map<Long, BigDecimal> getItems() { return items; }

    // Сеттеры
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setItems(Map<Long, BigDecimal> items) { this.items = items; }
}
