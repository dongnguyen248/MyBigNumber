package com.dongnguyen248.add2num;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProgressListenerTest {

    @Test
    void invokesTheListenerOnceForEachInputColumn() {
        List<ProgressEvent> events = new ArrayList<>();

        String result = MyBigNumber.sum("1234", "897", recordingListener(events));

        assertEquals("2131", result);
        assertEquals(4, events.size());
        assertEquals(List.of(1, 2, 3, 4), events.stream().map(ProgressEvent::completedSteps).toList());
        assertEquals(List.of(4, 4, 4, 4), events.stream().map(ProgressEvent::totalSteps).toList());
    }

    @Test
    void reportsOnePercentMilestonesForOneHundredColumns() {
        List<ProgressEvent> events = new ArrayList<>();

        MyBigNumber.sum("1".repeat(100), "0", recordingListener(events));

        assertEquals(100, events.size());
        for (int index = 0; index < events.size(); index++) {
            ProgressEvent event = events.get(index);
            assertEquals(index + 1, event.completedSteps());
            assertEquals(100, event.totalSteps());
            assertEquals(index + 1, event.completedSteps() * 100 / event.totalSteps());
        }
    }

    @Test
    void doesNotNotifyForTheFinalCarryColumn() {
        List<ProgressEvent> events = new ArrayList<>();

        String result = MyBigNumber.sum("999", "1", recordingListener(events));

        assertEquals("1000", result);
        assertEquals(3, events.size());
        assertEquals(List.of(0, 0, 0), events.stream().map(ProgressEvent::resultDigit).toList());
        assertEquals(List.of(1, 1, 1), events.stream().map(ProgressEvent::carry).toList());
    }

    @Test
    void acceptsANullListener() {
        assertEquals("100", MyBigNumber.sum("99", "1", null));
    }

    private static ProgressListener recordingListener(List<ProgressEvent> events) {
        return (completedSteps, totalSteps, resultDigit, carry) ->
                events.add(new ProgressEvent(completedSteps, totalSteps, resultDigit, carry));
    }

    private record ProgressEvent(int completedSteps, int totalSteps, int resultDigit, int carry) {
    }
}