package com.dongnguyen248.add2num.web.model;

import java.util.UUID;

public record AdditionStatusResponse(
        UUID jobId,
        JobStatus status,
        ProgressUpdate progress,
        AdditionResult result,
        String error) {
}