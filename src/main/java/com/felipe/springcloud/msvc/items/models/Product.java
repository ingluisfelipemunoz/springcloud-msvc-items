package com.felipe.springcloud.msvc.items.models;

import java.time.LocalDate;

public class Product {
    private Long id;
    private String name;
    private Double price;
    private LocalDate createdAt;
    private int port;

    public Product(Long id, String name, Double price, LocalDate createdAt, int port) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.createdAt = createdAt;
        this.port = port;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
