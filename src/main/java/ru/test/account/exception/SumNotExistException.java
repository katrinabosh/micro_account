package ru.test.account.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class SumNotExistException extends Exception {

    public SumNotExistException(UUID personId, BigDecimal sum) {
        super("Person " + personId + " doesn't have sum " + sum.longValue());
    }
}
