package com.dongnguyen248.add2num;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MyBigNumberSumTest {

    @Test
    void addsTheExampleFromTheSpecification() {
        assertEquals("2131", MyBigNumber.sum("1234", "897"));
    }

    @Test
    void addsNumbersWithDifferentLengths() {
        assertEquals("1000001", MyBigNumber.sum("999999", "2"));
    }

    @Test
    void propagatesCarryAcrossAllColumns() {
        assertEquals("10000", MyBigNumber.sum("9999", "1"));
    }

    @Test
    void retainsAFinalCarryInTheResult() {
        assertEquals("1000", MyBigNumber.sum("999", "1"));
    }

    @Test
    void addsZero() {
        assertEquals("42", MyBigNumber.sum("0", "42"));
        assertEquals("0", MyBigNumber.sum("0", "0"));
    }

    @Test
    void canonicalizesLeadingZeroes() {
        assertEquals("1", MyBigNumber.sum("000", "1"));
        assertEquals("0", MyBigNumber.sum("000", "000"));
    }

    @Test
    void supportsNumbersLongerThanPrimitiveTypes() {
        String nines = "9".repeat(200);
        assertEquals("1" + "0".repeat(200), MyBigNumber.sum(nines, "1"));
    }

    @Test
    void twoArgumentOverloadDelegatesWithANullListener() {
        assertEquals("579", MyBigNumber.sum("123", "456"));
    }
}