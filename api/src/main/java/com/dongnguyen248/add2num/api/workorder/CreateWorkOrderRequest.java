package com.dongnguyen248.add2num.api.workorder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWorkOrderRequest(
        @NotBlank String equipmentId,
        @NotBlank @Size(max = 1000) String description,
        @NotNull Priority priority) {
}
