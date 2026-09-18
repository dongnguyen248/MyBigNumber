package com.dongnguyen248.add2num;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class ConcurrencyTest {

    @Test
    void supportsConcurrentCallsToTheStaticApi() throws Exception {
        List<Callable<String>> tasks = List.of(
                () -> repeatedlySum("999999", "1", "1000000"),
                () -> repeatedlySum("123456789", "987654321", "1111111110"),
                () -> repeatedlySum("000", "1", "1"),
                () -> repeatedlySum("0", "0", "0"));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());

        try {
            List<Future<String>> results = executor.invokeAll(tasks);
            for (Future<String> result : results) {
                assertEquals("completed", result.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static String repeatedlySum(String firstNumber, String secondNumber, String expected) {
        for (int iteration = 0; iteration < 1_000; iteration++) {
            if (!expected.equals(MyBigNumber.sum(firstNumber, secondNumber))) {
                return "failed";
            }
        }
        return "completed";
    }
}