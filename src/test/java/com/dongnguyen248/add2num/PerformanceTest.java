package com.dongnguyen248.add2num;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Tag("performance")
class PerformanceTest {

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void addsTwoHundredThousandDigitOperandsWithinTheTimeLimit() {
        String operand = "9".repeat(100_000);
        String expected = "1" + "9".repeat(99_999) + "8";

        assertEquals(expected, MyBigNumber.sum(operand, operand));
    }
}