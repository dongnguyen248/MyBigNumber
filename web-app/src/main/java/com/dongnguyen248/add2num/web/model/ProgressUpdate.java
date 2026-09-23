package com.dongnguyen248.add2num.web.model;

import java.util.UUID;

public record ProgressUpdate(
        UUID jobId,
        int completedSteps,
        int totalSteps,
        int percentage,
        int resultDigit,
        int carry) {
}