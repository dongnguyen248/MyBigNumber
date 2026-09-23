package com.dongnguyen248.add2num;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * C0 coverage tests for Main.
 * Covers: main() method (prints sum of "1234" + "897").
 */
class MainTest {

    @Test
    void main_printsSumToStdout() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
        try {
            Main.main(new String[]{});
        } finally {
            System.setOut(originalOut);
        }
        assertEquals("2131", baos.toString().trim());
    }
}