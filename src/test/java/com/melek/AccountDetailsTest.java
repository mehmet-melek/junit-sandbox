package com.melek;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountDetailsTest {

    AccountDetails accountDetails = new AccountDetails(
            "Anita",
            011545454d,
            111111,
            555d,
            "Savings");

    @Test
    void validateName() {
        assertTrue(accountDetails.getName().matches("^[a-zA-Z]*$"));
    }

    @Test
    void validateBalance() {
        assertTrue(accountDetails.getBalance() >= 0);
    }

}
