package com.dongnguyen248.add2num.web.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.dongnguyen248.add2num.web.Add2NumWebApplication;
import com.dongnguyen248.add2num.web.model.AdditionRequest;
import com.dongnguyen248.add2num.web.model.JobStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Add2NumWebApplication.class)
class ConcurrentAdditionIntegrationTest {

    @Autowired
    private AdditionService additionService;

    @Autowired
    private AdditionJobRegistry jobRegistry;

    @Test
    void keepsConcurrentCalculationResultsIsolated() {
        List<AdditionRequest> requests = List.of(
                new AdditionRequest("999", "1"),
                new AdditionRequest("1234", "897"),
                new AdditionRequest("0007", "0008"),
                new AdditionRequest("99999999999999999999", "1"));
        List<UUID> jobIds = requests.stream().map(additionService::start).toList();

        Instant deadline = Instant.now().plus(Duration.ofSeconds(5));
        while (Instant.now().isBefore(deadline) && jobIds.stream()
                .anyMatch(jobId -> jobRegistry.find(jobId).orElseThrow().status() != JobStatus.COMPLETED)) {
            Thread.onSpinWait();
        }

        assertThat(jobIds).allSatisfy(jobId -> assertThat(jobRegistry.find(jobId).orElseThrow().status())
                .isEqualTo(JobStatus.COMPLETED));
        assertThat(jobRegistry.find(jobIds.get(0)).orElseThrow().result().sum()).isEqualTo("1000");
        assertThat(jobRegistry.find(jobIds.get(1)).orElseThrow().result().sum()).isEqualTo("2131");
        assertThat(jobRegistry.find(jobIds.get(2)).orElseThrow().result().sum()).isEqualTo("15");
        assertThat(jobRegistry.find(jobIds.get(3)).orElseThrow().result().sum()).isEqualTo("100000000000000000000");
    }
}