package com.dongnguyen248.add2num;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("performance")
class BenchmarkTest {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkTest.class);

    @Test
    void benchmarkOldVsNew() {
        // Prepare large numbers for benchmarking
        String operand1 = "9".repeat(1_000_000);
        String operand2 = "8".repeat(1_000_000);

        // 1. Warmup phase (let JIT compiler kick in)
        log.info("Starting warmup phase...");
        for (int i = 0; i < 50; i++) {
            sumOld(operand1, operand2);
            sumNew(operand1, operand2);
            sumCharArray(operand1, operand2);
        }

        // 2. Measure Old Version
        log.info("Benchmarking Old Version...");
        long startOld = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            sumOld(operand1, operand2);
        }
        long endOld = System.nanoTime();

        // 3. Measure New Version
        log.info("Benchmarking New Version...");
        long startNew = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            sumNew(operand1, operand2);
        }
        long endNew = System.nanoTime();

        // 4. Measure CharArray Version
        log.info("Benchmarking CharArray Version...");
        long startCharArray = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            sumCharArray(operand1, operand2);
        }
        long endCharArray = System.nanoTime();

        long timeOldMs      = (endOld      - startOld)      / 1_000_000;
        long timeNewMs      = (endNew      - startNew)      / 1_000_000;
        long timeCharArrayMs = (endCharArray - startCharArray) / 1_000_000;

        System.out.println("=========================================================================");
        System.out.println("Benchmark Results (100 iterations of 1,000,000 digits addition):");
        System.out.println("Old Version      (Variables inside loop)         : " + timeOldMs       + " ms");
        System.out.println("New Version      (Variables outside loop)        : " + timeNewMs       + " ms");
        System.out.println("CharArray Version (char[] right-to-left, no reverse): " + timeCharArrayMs + " ms");
        System.out.println("=========================================================================");
    }

    /**
     * The original implementation with variables declared inside the loop.
     */
    private String sumOld(String stn1, String stn2) {
        int totalSteps = Math.max(stn1.length(), stn2.length());
        StringBuilder result = new StringBuilder(totalSteps + 1);
        int firstIndex = stn1.length() - 1;
        int secondIndex = stn2.length() - 1;
        int carry = 0;

        for (int completedSteps = 1; completedSteps <= totalSteps; completedSteps++) {
            int firstDigit = firstIndex >= 0 ? stn1.charAt(firstIndex--) - '0' : 0;
            int secondDigit = secondIndex >= 0 ? stn2.charAt(secondIndex--) - '0' : 0;
            int carryIn = carry;
            int digitSum = firstDigit + secondDigit + carryIn;
            int resultDigit = digitSum % 10;
            carry = digitSum / 10;
            result.append(resultDigit);
        }

        if (carry > 0) {
            result.append(carry);
        }
        return canonicalize(result.reverse());
    }

    /**
     * The optimized implementation with variables declared outside the loop.
     */
    private String sumNew(String stn1, String stn2) {
        int totalSteps = Math.max(stn1.length(), stn2.length());
        StringBuilder result = new StringBuilder(totalSteps + 1);
        int firstIndex = stn1.length() - 1;
        int secondIndex = stn2.length() - 1;
        int carry = 0;
        int firstDigit;
        int secondDigit;
        int carryIn;
        int digitSum;
        int resultDigit;

        for (int completedSteps = 1; completedSteps <= totalSteps; completedSteps++) {
            firstDigit = firstIndex >= 0 ? stn1.charAt(firstIndex--) - '0' : 0;
            secondDigit = secondIndex >= 0 ? stn2.charAt(secondIndex--) - '0' : 0;
            carryIn = carry;
            digitSum = firstDigit + secondDigit + carryIn;
            resultDigit = digitSum % 10;
            carry = digitSum / 10;
            result.append(resultDigit);
        }

        if (carry > 0) {
            result.append(carry);
        }
        return canonicalize(result.reverse());
    }

    /**
     * Version 3: uses a pre-allocated char[] written right-to-left.
     * No StringBuilder, no reverse() — the final String is built directly
     * via new String(char[], offset, length).
     */
    private String sumCharArray(String stn1, String stn2) {
        int totalSteps = Math.max(stn1.length(), stn2.length());
        char[] result = new char[totalSteps + 1];
        int writeIndex = totalSteps;
        int firstIndex  = stn1.length() - 1;
        int secondIndex = stn2.length() - 1;
        int carry = 0;
        int firstDigit;
        int secondDigit;
        int carryIn;
        int digitSum;
        int resultDigit;

        for (int completedSteps = 1; completedSteps <= totalSteps; completedSteps++) {
            firstDigit  = firstIndex  >= 0 ? stn1.charAt(firstIndex--)  - '0' : 0;
            secondDigit = secondIndex >= 0 ? stn2.charAt(secondIndex--) - '0' : 0;
            carryIn    = carry;
            digitSum   = firstDigit + secondDigit + carryIn;
            resultDigit = digitSum % 10;
            carry      = digitSum / 10;
            result[writeIndex--] = (char) ('0' + resultDigit);
        }

        int startOffset;
        if (carry > 0) {
            result[0] = (char) ('0' + carry);
            startOffset = 0;
        } else {
            startOffset = 1;
        }

        return canonicalize(result, startOffset, totalSteps + 1);
    }

    private String canonicalize(StringBuilder reversedResult) {
        int firstNonZeroIndex = 0;
        while (firstNonZeroIndex < reversedResult.length() - 1
                && reversedResult.charAt(firstNonZeroIndex) == '0') {
            firstNonZeroIndex++;
        }
        return reversedResult.substring(firstNonZeroIndex);
    }

    private String canonicalize(char[] digits, int offset, int length) {
        while (offset < length - 1 && digits[offset] == '0') {
            offset++;
        }
        return new String(digits, offset, length - offset);
    }
}
