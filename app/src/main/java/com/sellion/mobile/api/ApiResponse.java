package com.sellion.mobile.api;

public class ApiResponse<T> {
    private String status;
    private String message;
    private T data; // Сюда попадет ваш List или объект

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}

