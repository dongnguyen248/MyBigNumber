package com.dongnguyen248.add2num.web.service;

import com.dongnguyen248.add2num.web.model.AdditionResult;
import com.dongnguyen248.add2num.web.model.AdditionStatusResponse;
import com.dongnguyen248.add2num.web.model.JobStatus;
import com.dongnguyen248.add2num.web.model.ProgressUpdate;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AdditionJobRegistry {

    private final ConcurrentMap<UUID, AdditionJobState> jobs = new ConcurrentHashMap<>();
    private final Duration completedJobRetention;

    @Autowired
    public AdditionJobRegistry(
            @Value("${add2num.completed-job-retention-seconds:300}") long completedJobRetentionSeconds) {
        this(Duration.ofSeconds(completedJobRetentionSeconds));
    }

    AdditionJobRegistry(Duration completedJobRetention) {
        this.completedJobRetention = completedJobRetention;
    }

    public UUID create() {
        UUID jobId = UUID.randomUUID();
        jobs.put(jobId, new AdditionJobState(jobId));
        return jobId;
    }

    public Optional<AdditionStatusResponse> find(UUID jobId) {
        AdditionJobState job = jobs.get(jobId);
        return job == null ? Optional.empty() : Optional.of(job.snapshot());
    }

    public Optional<Subscription> subscribe(UUID jobId, Consumer<JobEvent> subscriber) {
        AdditionJobState job = jobs.get(jobId);
        if (job == null) {
            return Optional.empty();
        }
        return Optional.of(job.subscribe(subscriber));
    }

    public void markRunning(UUID jobId) {
        requireJob(jobId).markRunning();
    }

    public void updateProgress(UUID jobId, ProgressUpdate progress, boolean publishUpdate) {
        requireJob(jobId).updateProgress(progress, publishUpdate);
    }

    public void complete(UUID jobId, AdditionResult result) {
        requireJob(jobId).complete(result);
    }

    public void fail(UUID jobId, String error) {
        requireJob(jobId).fail(error);
    }

    @Scheduled(fixedDelay = 60_000)
    public void removeExpiredJobs() {
        removeExpiredJobs(Instant.now());
    }

    void removeExpiredJobs(Instant now) {
        jobs.entrySet().removeIf(entry -> entry.getValue().isExpired(now, completedJobRetention));
    }

    private AdditionJobState requireJob(UUID jobId) {
        AdditionJobState job = jobs.get(jobId);
        if (job == null) {
            throw new JobNotFoundException(jobId);
        }
        return job;
    }

    public record JobEvent(String name, AdditionStatusResponse status) {
    }

    @FunctionalInterface
    public interface Subscription {
        void unsubscribe();
    }

    public static class JobNotFoundException extends RuntimeException {

        public JobNotFoundException(UUID jobId) {
            super("Addition job not found: " + jobId);
        }
    }

    private static class AdditionJobState {

        private final UUID jobId;
        private final CopyOnWriteArraySet<Consumer<JobEvent>> subscribers = new CopyOnWriteArraySet<>();
        private JobStatus status = JobStatus.PENDING;
        private ProgressUpdate progress;
        private AdditionResult result;
        private String error;
        private Instant finishedAt;

        private AdditionJobState(UUID jobId) {
            this.jobId = jobId;
        }

        private synchronized AdditionStatusResponse snapshot() {
            return new AdditionStatusResponse(jobId, status, progress, result, error);
        }

        private synchronized Subscription subscribe(Consumer<JobEvent> subscriber) {
            deliver(subscriber, new JobEvent(eventName(), snapshot()));
            if (!isTerminal()) {
                subscribers.add(subscriber);
            }
            return () -> subscribers.remove(subscriber);
        }

        private synchronized void markRunning() {
            if (status == JobStatus.PENDING) {
                status = JobStatus.RUNNING;
                publish("status");
            }
        }

        private synchronized void updateProgress(ProgressUpdate update, boolean publishUpdate) {
            if (isTerminal()) {
                return;
            }
            progress = update;
            if (publishUpdate) {
                publish("progress");
            }
        }

        private synchronized void complete(AdditionResult completedResult) {
            if (isTerminal()) {
                return;
            }
            result = completedResult;
            status = JobStatus.COMPLETED;
            finishedAt = Instant.now();
            publish("completed");
            subscribers.clear();
        }

        private synchronized void fail(String failure) {
            if (isTerminal()) {
                return;
            }
            error = failure;
            status = JobStatus.FAILED;
            finishedAt = Instant.now();
            publish("failed");
            subscribers.clear();
        }

        private synchronized boolean isExpired(Instant now, Duration retention) {
            return finishedAt != null && !finishedAt.plus(retention).isAfter(now);
        }

        private boolean isTerminal() {
            return status == JobStatus.COMPLETED || status == JobStatus.FAILED;
        }

        private String eventName() {
            if (status == JobStatus.COMPLETED) {
                return "completed";
            }
            if (status == JobStatus.FAILED) {
                return "failed";
            }
            return progress == null ? "status" : "progress";
        }

        private void publish(String name) {
            JobEvent event = new JobEvent(name, snapshot());
            for (Consumer<JobEvent> subscriber : subscribers) {
                deliver(subscriber, event);
            }
        }

        private void deliver(Consumer<JobEvent> subscriber, JobEvent event) {
            try {
                subscriber.accept(event);
            } catch (RuntimeException exception) {
                subscribers.remove(subscriber);
            }
        }
    }
}