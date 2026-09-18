package com.dongnguyen248.add2num;

/**
 * Receives progress notifications for each processed input column during addition.
 *
 * <p>The callback runs synchronously in the thread that calls {@link MyBigNumber#sum(String, String,
 * ProgressListener)}. Implementations must remain lightweight and must not perform blocking or expensive
 * work in {@code onProgress}; a 100,000-digit addition produces 100,000 callbacks. Consumers should
 * throttle or batch UI rendering when necessary.</p>
 */
@FunctionalInterface
public interface ProgressListener {

    /**
     * Called after one input column has been added.
     *
     * @param completedSteps the number of processed input columns, starting at 1
     * @param totalSteps the total number of input columns, equal to the longer input length
     * @param resultDigit the digit stored for the processed column
     * @param carry the carry passed to the next column
     */
    void onProgress(int completedSteps, int totalSteps, int resultDigit, int carry);
}