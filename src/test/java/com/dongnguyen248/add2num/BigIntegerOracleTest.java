package com.dongnguyen248.add2num;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BigIntegerOracleTest {

    private static final int SAMPLE_COUNT = 5_000;
    private static final long RANDOM_SEED = 24_824L;

    @Test
    void matchesBigIntegerForRandomDecimalInputs() {
        Random random = new Random(RANDOM_SEED);

        for (int sample = 0; sample < SAMPLE_COUNT; sample++) {
            String firstNumber = randomDecimal(random, random.nextInt(200) + 1);
            String secondNumber = randomDecimal(random, random.nextInt(200) + 1);
            String expected = new BigInteger(firstNumber).add(new BigInteger(secondNumber)).toString();
            int sampleIndex = sample;

            assertEquals(expected, MyBigNumber.sum(firstNumber, secondNumber),
                () -> "Failed sample " + sampleIndex);
        }
    }

    private static String randomDecimal(Random random, int length) {
        StringBuilder number = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            number.append(random.nextInt(10));
        }
        return number.toString();
    }
}