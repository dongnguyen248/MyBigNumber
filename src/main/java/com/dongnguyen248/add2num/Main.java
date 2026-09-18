package com.dongnguyen248.add2num;

/**
 * Runs the library's example addition through the Maven exec plugin.
 */
public final class Main {

    private Main() {
    }

    /**
     * Prints the sum of the example operands from the task specification.
     *
     * @param args ignored command-line arguments
     */
    public static void main(String[] args) {
        System.out.println(MyBigNumber.sum("1234", "897"));
    }
}