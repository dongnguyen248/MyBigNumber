package com.dongnguyen248.add2num.web.service;

import com.dongnguyen248.add2num.MyBigNumber;
import com.dongnguyen248.add2num.web.model.AdditionRequest;
import com.dongnguyen248.add2num.web.model.AdditionResult;
import com.dongnguyen248.add2num.web.model.ProgressUpdate;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

@Service
public class AdditionService {

    private final AdditionJobRegistry jobRegistry;
    private final TaskExecutor additionExecutor;

    public AdditionService(AdditionJobRegistry jobRegistry, @Qualifier("additionExecutor") TaskExecutor additionExecutor) {
        this.jobRegistry = jobRegistry;
        this.additionExecutor = additionExecutor;
    }

    public UUID start(AdditionRequest request) {
        UUID jobId = jobRegistry.create();
        try {
            additionExecutor.execute(() -> calculate(jobId, request));
        } catch (TaskRejectedException exception) {
            jobRegistry.fail(jobId, "The server is busy. Please try again shortly.");
        }
        return jobId;
    }

    void calculate(UUID jobId, AdditionRequest request) {
        jobRegistry.markRunning(jobId);
        int[] lastPublishedPercentage = {0};
        long startedAt = System.nanoTime();
        try {
            String sum = MyBigNumber.sum(request.firstNumber(), request.secondNumber(),
                    (completedSteps, totalSteps, resultDigit, carry) -> {
                        int percentage = (int) ((long) completedSteps * 100 / totalSteps);
                        boolean publishUpdate = percentage > lastPublishedPercentage[0];
                        if (publishUpdate) {
                            lastPublishedPercentage[0] = percentage;
                        }
                        jobRegistry.updateProgress(jobId, new ProgressUpdate(
                                jobId, completedSteps, totalSteps, percentage, resultDigit, carry), publishUpdate);
                    });
            int totalSteps = Math.max(request.firstNumber().length(), request.secondNumber().length());
            jobRegistry.complete(jobId, new AdditionResult(sum, totalSteps, System.nanoTime() - startedAt));
        } catch (RuntimeException exception) {
            jobRegistry.fail(jobId, "The addition could not be completed.");
        }
    }
}