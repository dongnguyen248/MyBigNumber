package com.dongnguyen248.add2num.web.controller;

import com.dongnguyen248.add2num.web.model.AdditionJobResponse;
import com.dongnguyen248.add2num.web.model.AdditionRequest;
import com.dongnguyen248.add2num.web.model.AdditionStatusResponse;
import com.dongnguyen248.add2num.web.service.AdditionJobRegistry;
import com.dongnguyen248.add2num.web.service.AdditionService;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/additions")
public class AdditionApiController {

    private final AdditionService additionService;
    private final AdditionJobRegistry jobRegistry;
    private final long sseTimeoutMs;

    public AdditionApiController(
            AdditionService additionService,
            AdditionJobRegistry jobRegistry,
            @Value("${add2num.sse-timeout-ms:60000}") long sseTimeoutMs) {
        this.additionService = additionService;
        this.jobRegistry = jobRegistry;
        this.sseTimeoutMs = sseTimeoutMs;
    }

    @PostMapping
    public ResponseEntity<AdditionJobResponse> create(@Valid @RequestBody AdditionRequest request) {
        UUID jobId = additionService.start(request);
        String baseUrl = "/api/additions/" + jobId;
        return ResponseEntity.accepted().body(new AdditionJobResponse(
                jobId, baseUrl + "/events", baseUrl));
    }

    @GetMapping("/{jobId}")
    public AdditionStatusResponse status(@PathVariable UUID jobId) {
        return jobRegistry.find(jobId).orElseThrow(() -> new AdditionJobRegistry.JobNotFoundException(jobId));
    }

    @GetMapping(value = "/{jobId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable UUID jobId) {
        SseEmitter emitter = new SseEmitter(sseTimeoutMs);
        AdditionJobRegistry.Subscription subscription = jobRegistry.subscribe(jobId, event -> {
            try {
                emitter.send(SseEmitter.event().name(event.name()).data(event.status()));
                if (event.status().status().name().equals("COMPLETED")
                        || event.status().status().name().equals("FAILED")) {
                    emitter.complete();
                }
            } catch (IOException | IllegalStateException exception) {
                emitter.completeWithError(exception);
            }
        }).orElseThrow(() -> new AdditionJobRegistry.JobNotFoundException(jobId));
        emitter.onCompletion(subscription::unsubscribe);
        emitter.onTimeout(() -> {
            subscription.unsubscribe();
            emitter.complete();
        });
        emitter.onError(exception -> subscription.unsubscribe());
        return emitter;
    }
}