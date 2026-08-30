package com.devsuperior.dsmeta.dto;

public class SaleSummaryDTO {

    private String name;
    private Double totalAmount;

    public SaleSummaryDTO(String name, Double totalAmount) {
        this.name = name;
        this.totalAmount = totalAmount;
    }

    public String getName() {
        return name;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }
}
