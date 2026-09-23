package com.dongnguyen248.add2num.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import com.dongnguyen248.add2num.web.model.AdditionRequest;
import com.dongnguyen248.add2num.web.model.JobStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdditionServiceTest {

    @Test
    void completesAdditionUsingTheCoreLibrary() {
        AdditionJobRegistry registry = new AdditionJobRegistry(Duration.ofMinutes(5));
        AdditionService service = new AdditionService(registry, Runnable::run);

        UUID jobId = service.start(new AdditionRequest("999", "1"));

        assertThat(registry.find(jobId).orElseThrow())
                .satisfies(status -> {
                    assertThat(status.status()).isEqualTo(JobStatus.COMPLETED);
                    assertThat(status.result().sum()).isEqualTo("1000");
                    assertThat(status.progress().completedSteps()).isEqualTo(3);
                });
    }

    @Test
    void coalescesLongAdditionProgressToPercentageChanges() {
        AdditionJobRegistry registry = new AdditionJobRegistry(Duration.ofMinutes(5));
        List<Runnable> pendingTasks = new ArrayList<>();
        AdditionService service = new AdditionService(registry, pendingTasks::add);
        String firstNumber = "9".repeat(100_000);
        UUID jobId = service.start(new AdditionRequest(firstNumber, "1"));
        List<AdditionJobRegistry.JobEvent> events = new ArrayList<>();
        registry.subscribe(jobId, events::add);

        assertTimeout(Duration.ofSeconds(5), () -> pendingTasks.remove(0).run());

        assertThat(events).filteredOn(event -> event.name().equals("progress")).hasSize(100);
        assertThat(registry.find(jobId).orElseThrow().progress().completedSteps()).isEqualTo(100_000);
    }
}