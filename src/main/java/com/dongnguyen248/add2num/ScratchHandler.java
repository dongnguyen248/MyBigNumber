package com.dongnguyen248.add2num;

/**
 * ScratchHandler - Draft Generated via Copilot Chat
 *
 * Prompt used:
 *   Role: Senior Engineer.
 *   Task: Write a POST /api/workorders handler in Java.
 *   Context files: docs/coding-rules.md, docs/api-rules.md.
 *   Constraints:
 *     - Do not invent extra JSON fields not specified in requirements.
 *     - Use standard validation, return 400 on error.
 *     - Match repo style.
 *
 * This file is a scratch/draft to demonstrate Copilot draft generation
 * with context-constrained prompting (LAB 2.1 - Step 3).
 *
 * NOTE: This is intentionally a standalone scratch file (no Spring dependencies).
 * It demonstrates the structure and rules that would be applied in the real
 * WorkOrderController in a Spring Boot project.
 */
public class ScratchHandler {

    // -------------------------------------------------------------------------
    // Simulated Request DTO (follows coding-rules.md Rule 2 - PascalCase,
    // Rule 10 - Enum for fixed value sets)
    // -------------------------------------------------------------------------
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }

    public static class WorkOrderRequest {
        private final String equipmentId;   // required, not blank
        private final String description;   // required, not blank
        private final Priority priority;    // required Enum - no raw String

        public WorkOrderRequest(String equipmentId, String description, Priority priority) {
            this.equipmentId = equipmentId;
            this.description = description;
            this.priority = priority;
        }

        public String getEquipmentId() { return equipmentId; }
        public String getDescription() { return description; }
        public Priority getPriority() { return priority; }
    }

    // -------------------------------------------------------------------------
    // Simulated Response DTO (follows api-rules.md Rule 3 - Strict Schema,
    // only fields defined in requirements)
    // -------------------------------------------------------------------------
    public static class WorkOrderResponse {
        private final long id;
        private final String equipmentId;
        private final String status;

        public WorkOrderResponse(long id, String equipmentId, String status) {
            this.id = id;
            this.equipmentId = equipmentId;
            this.status = status;
        }

        public long getId() { return id; }
        public String getEquipmentId() { return equipmentId; }
        public String getStatus() { return status; }
    }

    // -------------------------------------------------------------------------
    // Simulated Problem Details for error (follows api-rules.md Rule 4 - RFC 7807)
    // -------------------------------------------------------------------------
    public static class ProblemDetail {
        private final String type;
        private final String title;
        private final int status;
        private final String detail;

        public ProblemDetail(String type, String title, int status, String detail) {
            this.type = type;
            this.title = title;
            this.status = status;
            this.detail = detail;
        }
    }

    // -------------------------------------------------------------------------
    // Handler logic (follows coding-rules.md Rule 5 - constructor injection,
    // Rule 3 - specific exceptions, Rule 4 - SLF4J logging, Rule 7 - vars outside loop)
    // -------------------------------------------------------------------------

    /**
     * Validates a WorkOrderRequest according to the rules in coding-rules.md.
     * Returns a ProblemDetail if invalid, null if valid.
     *
     * Per api-rules.md Rule 5: return 400 + ProblemDetail on validation failure.
     */
    public static ProblemDetail validate(WorkOrderRequest request) {
        if (request == null) {
            return new ProblemDetail(
                "about:blank", "Bad Request", 400, "Request body must not be null.");
        }
        if (request.getEquipmentId() == null || request.getEquipmentId().isBlank()) {
            return new ProblemDetail(
                "about:blank", "Validation Failed", 400, "Field 'equipmentId' must not be blank.");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            return new ProblemDetail(
                "about:blank", "Validation Failed", 400, "Field 'description' must not be blank.");
        }
        if (request.getPriority() == null) {
            return new ProblemDetail(
                "about:blank", "Validation Failed", 400, "Field 'priority' must not be null. Valid values: LOW, MEDIUM, HIGH, URGENT.");
        }
        return null; // valid
    }

    /**
     * Simulates the POST /api/work-orders handler.
     *
     * In a real Spring Boot controller this would be:
     *   @PreAuthorize("hasRole('TECHNICIAN')")
     *   @PostMapping
     *   public ResponseEntity<WorkOrderResponse> create(@Valid @RequestBody WorkOrderRequest req)
     */
    public static Object handleCreate(WorkOrderRequest request) {
        // Step 1: Validate input (coding-rules.md Rule 3, api-rules.md Rule 5)
        ProblemDetail validationError = validate(request);
        if (validationError != null) {
            System.out.println("[400 BAD REQUEST] " + validationError.detail);
            return validationError;
        }

        // Step 2: Simulate saving (in real code: workOrderRepository.save(...))
        // Strict schema: only return fields defined in requirements (api-rules.md Rule 3)
        long simulatedId = System.currentTimeMillis();
        WorkOrderResponse response = new WorkOrderResponse(
            simulatedId,
            request.getEquipmentId(),
            "NEW"   // default status per business rules
        );

        // Step 3: Log (coding-rules.md Rule 4 - no PII, structured log)
        // In real code: log.info("Work order created: id={}, equipmentId={}", response.getId(), response.getEquipmentId());
        System.out.printf("[201 CREATED] Work order created: id=%d, equipmentId=%s%n",
            response.getId(), response.getEquipmentId());

        return response;
    }

    // -------------------------------------------------------------------------
    // Quick demo main
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== ScratchHandler Demo (LAB 2.1 - Step 3) ===");
        System.out.println();

        // Case 1: Valid request
        System.out.println("-- Case 1: Valid request --");
        WorkOrderRequest valid = new WorkOrderRequest("EQ-001", "Inspect hydraulic pump", Priority.HIGH);
        handleCreate(valid);

        System.out.println();

        // Case 2: Missing equipmentId (should return 400)
        System.out.println("-- Case 2: Missing equipmentId --");
        WorkOrderRequest missingId = new WorkOrderRequest("", "Some work", Priority.LOW);
        handleCreate(missingId);

        System.out.println();

        // Case 3: Null priority (should return 400)
        System.out.println("-- Case 3: Null priority --");
        WorkOrderRequest nullPriority = new WorkOrderRequest("EQ-002", "Check valve", null);
        handleCreate(nullPriority);
    }
}