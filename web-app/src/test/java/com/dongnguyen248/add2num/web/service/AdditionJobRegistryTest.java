package com.dongnguyen248.add2num.web.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongnguyen248.add2num.web.model.AdditionResult;
import com.dongnguyen248.add2num.web.model.JobStatus;
import com.dongnguyen248.add2num.web.model.ProgressUpdate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdditionJobRegistryTest {

    @Test
    void deliversSnapshotThenProgressAndCompletionToItsSubscriber() {
        AdditionJobRegistry registry = new AdditionJobRegistry(Duration.ofMinutes(5));
        UUID jobId = registry.create();
        List<AdditionJobRegistry.JobEvent> events = new ArrayList<>();

        registry.subscribe(jobId, events::add);
        registry.markRunning(jobId);
        registry.updateProgress(jobId, new ProgressUpdate(jobId, 1, 2, 50, 4, 1), true);
        registry.complete(jobId, new AdditionResult("14", 2, 10));

        assertThat(events).extracting(AdditionJobRegistry.JobEvent::name)
                .containsExactly("status", "status", "progress", "completed");
        assertThat(events.get(3).status().result().sum()).isEqualTo("14");
        assertThat(events.get(3).status().status()).isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    void returnsTerminalSnapshotToLateSubscribers() {
        AdditionJobRegistry registry = new AdditionJobRegistry(Duration.ofMinutes(5));
        UUID jobId = registry.create();
        registry.complete(jobId, new AdditionResult("1000", 3, 10));
        List<AdditionJobRegistry.JobEvent> events = new ArrayList<>();

        registry.subscribe(jobId, events::add);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).name()).isEqualTo("completed");
        assertThat(events.get(0).status().result().sum()).isEqualTo("1000");
    }

    @Test
    void keepsJobProgressIsolated() {
        AdditionJobRegistry registry = new AdditionJobRegistry(Duration.ofMinutes(5));
        UUID firstJobId = registry.create();
        UUID secondJobId = registry.create();

        registry.updateProgress(firstJobId, new ProgressUpdate(firstJobId, 1, 3, 33, 7, 0), true);

        assertThat(registry.find(firstJobId).orElseThrow().progress().percentage()).isEqualTo(33);
        assertThat(registry.find(secondJobId).orElseThrow().progress()).isNull();
    }

    @Test
    void removesCompletedJobsAfterTheirRetentionPeriod() {
        AdditionJobRegistry registry = new AdditionJobRegistry(Duration.ZERO);
        UUID jobId = registry.create();
        registry.complete(jobId, new AdditionResult("2", 1, 10));

        registry.removeExpiredJobs(Instant.now());

        assertThat(registry.find(jobId)).isEmpty();
    }
}