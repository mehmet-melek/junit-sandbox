package com.melek;

public class AccountDetailsTestWithoutJunit {

    AccountDetails accountDetails = new AccountDetails(
            "Anita",
            011545454d,
            111111,
            555d,
            "Savings");

    void validateName() {
        if (accountDetails.getName().matches("^[a-zA-Z]*$")) {
            System.out.println("Test passed: Name is valid");
        } else {
            System.out.println("Test failed: Name is invalid");
        }
    }

/*
    public static void main(String[] args) {
        AccountDetailsTestWithoutJunit accountDetailsTestWithoutJunit = new AccountDetailsTestWithoutJunit();
        accountDetailsTestWithoutJunit.validateName();
    }
*/

}
