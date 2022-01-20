package com.melek;

public class AccountDetails {

    private String name;
    private Double accountNumber;
    private Integer customerID;
    private Double balance;
    private String accountType;

    public AccountDetails(String name, Double accountNumber, Integer customerID, Double balance, String accountType) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.customerID = customerID;
        this.balance = balance;
        this.accountType = accountType;
    }

    public String getName() {
        return name;
    }

    public Double getAccountNumber() {
        return accountNumber;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public Double getBalance() {
        return balance;
    }

    public String getAccountType() {
        return accountType;
    }

}
