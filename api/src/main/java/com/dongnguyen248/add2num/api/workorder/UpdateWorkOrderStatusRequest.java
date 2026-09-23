package com.dongnguyen248.add2num.api.workorder;

import jakarta.validation.constraints.NotNull;

public record UpdateWorkOrderStatusRequest(@NotNull WorkOrderStatus status) {
}
