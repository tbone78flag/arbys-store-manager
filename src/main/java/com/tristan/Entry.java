package com.tristan;

import java.time.LocalDate;

public class Entry {
    private LocalDate date;
    private double sales;
    private double labor;
    private double profit;
    private String notes;

    public Entry(LocalDate date, double sales, double labor, String notes) {
        this.date = date;
        this.sales = sales;
        this.labor = labor;
        this.profit = sales - labor;
        this.notes = notes;
    }

    public LocalDate getDate() {
        return date; }
    public double getSales() {
        return sales; }
    public double getLabor() {
        return labor; }
    public double getProfit() {
        return profit; }
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setSales(double sales) {
        this.sales = sales;
    }

    public void setLabor(double labor) {
        this.labor = labor;
    }

}
