package com.dongnguyen248.add2num;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * C0 coverage tests for ScratchHandler and all its inner classes.
 * Covers: ScratchHandler, WorkOrderRequest, WorkOrderResponse, ProblemDetail, Priority
 */
class ScratchHandlerTest {

    // --- validate() ---

    @Test
    void validate_returnsNull_whenRequestIsValid() {
        var req = new ScratchHandler.WorkOrderRequest("EQ-001", "Fix pump", ScratchHandler.Priority.HIGH);
        assertNull(ScratchHandler.validate(req));
    }

    @Test
    void validate_returnsProblemDetail_whenRequestIsNull() {
        ScratchHandler.ProblemDetail pd = ScratchHandler.validate(null);
        assertNotNull(pd);
    }

    @Test
    void validate_returnsProblemDetail_whenEquipmentIdIsNull() {
        var req = new ScratchHandler.WorkOrderRequest(null, "desc", ScratchHandler.Priority.LOW);
        assertNotNull(ScratchHandler.validate(req));
    }

    @Test
    void validate_returnsProblemDetail_whenEquipmentIdIsBlank() {
        var req = new ScratchHandler.WorkOrderRequest("   ", "desc", ScratchHandler.Priority.LOW);
        assertNotNull(ScratchHandler.validate(req));
    }

    @Test
    void validate_returnsProblemDetail_whenDescriptionIsNull() {
        var req = new ScratchHandler.WorkOrderRequest("EQ-001", null, ScratchHandler.Priority.LOW);
        assertNotNull(ScratchHandler.validate(req));
    }

    @Test
    void validate_returnsProblemDetail_whenDescriptionIsBlank() {
        var req = new ScratchHandler.WorkOrderRequest("EQ-001", "", ScratchHandler.Priority.LOW);
        assertNotNull(ScratchHandler.validate(req));
    }

    @Test
    void validate_returnsProblemDetail_whenPriorityIsNull() {
        var req = new ScratchHandler.WorkOrderRequest("EQ-001", "desc", null);
        assertNotNull(ScratchHandler.validate(req));
    }

    // --- handleCreate() ---

    @Test
    void handleCreate_returnsWorkOrderResponse_whenRequestIsValid() {
        var req = new ScratchHandler.WorkOrderRequest("EQ-001", "Inspect valve", ScratchHandler.Priority.URGENT);
        Object result = ScratchHandler.handleCreate(req);
        assertInstanceOf(ScratchHandler.WorkOrderResponse.class, result);
        ScratchHandler.WorkOrderResponse resp = (ScratchHandler.WorkOrderResponse) result;
        assertEquals("EQ-001", resp.getEquipmentId());
        assertEquals("NEW", resp.getStatus());
        assertTrue(resp.getId() > 0);
    }

    @Test
    void handleCreate_returnsProblemDetail_whenRequestIsInvalid() {
        Object result = ScratchHandler.handleCreate(null);
        assertInstanceOf(ScratchHandler.ProblemDetail.class, result);
    }

    // --- WorkOrderRequest getters ---

    @Test
    void workOrderRequest_getters_returnCorrectValues() {
        var req = new ScratchHandler.WorkOrderRequest("EQ-999", "Check motor", ScratchHandler.Priority.MEDIUM);
        assertEquals("EQ-999", req.getEquipmentId());
        assertEquals("Check motor", req.getDescription());
        assertEquals(ScratchHandler.Priority.MEDIUM, req.getPriority());
    }

    // --- WorkOrderResponse getters ---

    @Test
    void workOrderResponse_getters_returnCorrectValues() {
        var resp = new ScratchHandler.WorkOrderResponse(42L, "EQ-007", "IN_PROGRESS");
        assertEquals(42L, resp.getId());
        assertEquals("EQ-007", resp.getEquipmentId());
        assertEquals("IN_PROGRESS", resp.getStatus());
    }

    // --- Priority enum values (covers all 4 to hit enum class instructions) ---

    @Test
    void priority_allValuesAccessible() {
        assertNotNull(ScratchHandler.Priority.LOW);
        assertNotNull(ScratchHandler.Priority.MEDIUM);
        assertNotNull(ScratchHandler.Priority.HIGH);
        assertNotNull(ScratchHandler.Priority.URGENT);
        // valueOf covers the static methods of the enum
        assertEquals(ScratchHandler.Priority.LOW, Enum.valueOf(ScratchHandler.Priority.class, "LOW"));
    }
}