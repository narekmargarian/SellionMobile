package com.sellion.mobile.model;

import java.util.List;

public class CategoryGroupDto {
    private String categoryName;
    private List<Product> products;

    public String getCategoryName() { return categoryName; }
    public List<Product> getProducts() { return products; }
}
