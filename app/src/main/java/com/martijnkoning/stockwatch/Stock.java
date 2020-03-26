package com.martijnkoning.stockwatch;

class Stock {

    private String symbol;
    private String company;
    private double price;
    private double priceChange;
    private double percentage;


    Stock(String symbol, String company, double price, double priceChange, double percentage) {
        this.symbol = symbol;
        this.company = company;
        this.price = price;
        this.priceChange = priceChange;
        this.percentage = percentage * 100;
    }

    Stock(String symbol, String company) {
        this.symbol = symbol;
        this.company = company;
    }

    String getSymbol() {
        return symbol;
    }

    String getCompany() {
        return company;
    }

    double getPrice() {
        return price;
    }

    double getPriceChange() {
        return priceChange;
    }

    double getPercentage() {
        return percentage;
    }

    void setPrice() {
        this.price = 0;
    }

    void setPriceChange() {
        this.priceChange = 0;
    }

    void setPercentage() {
        this.percentage = 0;
    }
}

