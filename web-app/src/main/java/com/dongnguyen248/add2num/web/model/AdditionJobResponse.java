package com.dongnguyen248.add2num.web.model;

import java.util.UUID;

public record AdditionJobResponse(UUID jobId, String eventsUrl, String statusUrl) {
}