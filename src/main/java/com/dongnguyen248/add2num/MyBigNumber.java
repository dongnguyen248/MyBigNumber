package com.dongnguyen248.add2num;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds non-negative decimal numbers represented as strings using long addition.
 *
 * <p>This utility class has no mutable shared state. Input strings are assumed to contain only decimal
 * digits and are not validated. Results use canonical decimal formatting: leading zeroes are removed
 * while preserving one zero for a zero result.</p>
 */
public final class MyBigNumber {

    private static final Logger log = LoggerFactory.getLogger(MyBigNumber.class);

    private MyBigNumber() {
    }

    /**
     * Adds two non-negative decimal numbers without progress notifications.
     *
     * @param stn1 the first decimal number
     * @param stn2 the second decimal number
     * @return the canonical decimal sum of the inputs
     */
    public static String sum(String stn1, String stn2) {
        return sum(stn1, stn2, null);
    }

    /**
     * Adds two non-negative decimal numbers and notifies a listener after each input column.
     *
     * <p>The listener is invoked synchronously in the calling thread. A remaining final carry is appended
     * to the result but does not generate an additional notification.</p>
     *
     * @param stn1 the first decimal number
     * @param stn2 the second decimal number
     * @param listener the optional progress listener
     * @return the canonical decimal sum of the inputs
     */
    public static String sum(String stn1, String stn2, ProgressListener listener) {
        long startTime = System.nanoTime();
        int totalSteps = Math.max(stn1.length(), stn2.length());

        // Allocate result array: at most totalSteps digits + 1 extra slot for a final carry.
        // Index 0 is reserved for the potential carry; digits are written right-to-left.
        char[] result = new char[totalSteps + 1];
        int writeIndex = totalSteps; // start filling from the last position

        int firstIndex = stn1.length() - 1;
        int secondIndex = stn2.length() - 1;
        int carry = 0;
        int firstDigit;
        int secondDigit;
        int carryIn;
        int digitSum;
        int resultDigit;

        log.info("Starting addition: stn1={} (length={}), stn2={} (length={})",
                stn1, stn1.length(), stn2, stn2.length());

        for (int completedSteps = 1; completedSteps <= totalSteps; completedSteps++) {
            firstDigit = firstIndex >= 0 ? stn1.charAt(firstIndex--) - '0' : 0;
            secondDigit = secondIndex >= 0 ? stn2.charAt(secondIndex--) - '0' : 0;
            carryIn = carry;
            digitSum = firstDigit + secondDigit + carryIn;
            resultDigit = digitSum % 10;
            carry = digitSum / 10;
            result[writeIndex--] = (char) ('0' + resultDigit);

            log.debug("col={} d1={} d2={} carryIn={} sum={} digit={} carryOut={}",
                    completedSteps, firstDigit, secondDigit, carryIn, digitSum, resultDigit, carry);

            if (listener != null) {
                listener.onProgress(completedSteps, totalSteps, resultDigit, carry);
            }
        }

        // writeIndex is now 0; use slot 0 for the carry if present
        int startOffset;
        if (carry > 0) {
            result[0] = (char) ('0' + carry);
            startOffset = 0;
        } else {
            startOffset = 1; // skip the unused carry slot
        }

        String sum = canonicalize(result, startOffset, totalSteps + 1);
        long elapsedNanos = System.nanoTime() - startTime;
        log.info("Completed addition: result={} steps={} durationNanos={}", sum, totalSteps, elapsedNanos);
        return sum;
    }

    private static String canonicalize(char[] digits, int offset, int length) {
        // Skip leading zeros, but always keep at least the last digit.
        while (offset < length - 1 && digits[offset] == '0') {
            offset++;
        }
        return new String(digits, offset, length - offset);
    }
}