# Java Coding & Logging Rules (WorkOrder Module)

> Context files: docs/coding-rules.md, docs/api-rules.md, docs/security-rules.md
> Applies to: All Java/Spring Boot code in this project.

---

## Rule 1: Language & Framework

Use **Java 17+** and **Spring Boot 3.3+** for all new code.

- **[GOOD]:** `record WorkOrderRequest(String equipmentId, Priority priority) {}`
- **[BAD]:** Using Java 8 `Date` class instead of `java.time.LocalDateTime`.

---

## Rule 2: Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Class / Interface / Enum | `PascalCase` | `WorkOrderService`, `Priority` |
| Method / Variable | `camelCase` | `createWorkOrder()`, `equipmentId` |
| Constant | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Package | `lowercase` | `com.posco.mci.controller` |

- **[GOOD]:** `private static final int MAX_RETRY_COUNT = 3;`
- **[BAD]:** `private static final int maxRetryCount = 3;` or `private static final int MaxRetryCount = 3;`

---

## Rule 3: Exception Handling

Never throw or catch generic `RuntimeException` or `Exception`. Use specific custom exceptions or standard ones.

- **[GOOD]:**
```java
if (equipmentId == null || equipmentId.isBlank()) {
    throw new IllegalArgumentException("equipmentId must not be blank");
}
throw new EntityNotFoundException("Equipment not found: " + equipmentId);
```

- **[BAD]:**
```java
throw new RuntimeException("something went wrong");
catch (Exception e) { /* silently swallow */ }
```

---

## Rule 4: Logging Standards — No PII

- Use **SLF4J** logger instantiated as: `private static final Logger log = LoggerFactory.getLogger(ClassName.class);`
- **DO NOT** log sensitive data or PII: raw passwords, emails, phone numbers, national IDs, full equipment owner info.
- Use structured placeholders `{}` instead of string concatenation in log statements.

- **[GOOD]:**
```java
private static final Logger log = LoggerFactory.getLogger(WorkOrderService.class);
log.info("Work order created: id={}, equipmentId={}", order.getId(), order.getEquipmentId());
```

- **[BAD]:**
```java
System.out.println("Created for: " + request.getOwnerEmail());
log.info("Password used: " + user.getPassword());
Logger logger = Logger.getLogger("MyLogger"); // java.util.logging - not SLF4J
```

---

## Rule 5: Dependency Injection — Constructor Injection Only

Always use **constructor injection**. Never use field injection (`@Autowired` on fields).

- **[GOOD]:**
```java
public class WorkOrderService {
    private final WorkOrderRepository repository;
    private final EquipmentService equipmentService;

    public WorkOrderService(WorkOrderRepository repository, EquipmentService equipmentService) {
        this.repository = repository;
        this.equipmentService = equipmentService;
    }
}
```

- **[BAD]:**
```java
@Autowired
private WorkOrderRepository repository; // Field injection - avoid

@Autowired
private EquipmentService equipmentService;
```

---

## Rule 6: Code Simplicity — No Over-Engineering

Avoid adding factories, abstract layers, or design patterns unless explicitly requested. Write clean, flat vertical slices.

- **[GOOD]:** One service class per feature. Direct call from controller to service to repository.
- **[BAD]:** Creating `WorkOrderFactory`, `AbstractWorkOrderHandler`, `WorkOrderHandlerRegistry` for a simple CRUD.

---

## Rule 7: Variable Declaration Scope — Declare Outside Loops

**DO NOT** declare variables inside `for`, `while`, or `do-while` loops. Declare all loop-related variables before the loop block.

- **[GOOD]:**
```java
String item = "";
int digitSum = 0;
for (int i = 0; i < list.size(); i++) {
    item = list.get(i);
    digitSum = process(item);
}
```

- **[BAD]:**
```java
for (int i = 0; i < list.size(); i++) {
    String item = list.get(i);   // declared inside - BAD
    int digitSum = process(item); // declared inside - BAD
}
```

---

## Rule 8: Method Length & Single Responsibility

Each method should do **one thing** and be at most **30-40 lines**. Extract sub-logic into private helper methods with descriptive names.

- **[GOOD]:**
```java
public WorkOrderResponse createWorkOrder(WorkOrderRequest req, String createdBy) {
    validateEquipmentActive(req.getEquipmentId());
    WorkOrder order = buildWorkOrder(req, createdBy);
    WorkOrder saved = repository.save(order);
    return toResponse(saved);
}
```

- **[BAD]:** One giant 150-line `createWorkOrder` method doing validation + mapping + persistence + notification.

---

## Rule 9: Immutability — Prefer Final Fields

Prefer `final` for fields that are not reassigned. Use immutable data structures where possible.

- **[GOOD]:**
```java
public final class MyBigNumber {
    private static final Logger log = LoggerFactory.getLogger(MyBigNumber.class);
    private MyBigNumber() {}
}
```

- **[BAD]:**
```java
public class MyBigNumber {
    private Logger log; // non-final, can be reassigned
}
```

---

## Rule 10: Use Enums for Fixed Value Sets

Never use raw String literals for domain values with a fixed set (status, priority, role). Define as `enum`.

- **[GOOD]:**
```java
public enum Priority { LOW, MEDIUM, HIGH, URGENT }
public enum WorkOrderStatus { NEW, IN_PROGRESS, DONE, CANCELLED }
```

- **[BAD]:**
```java
String priority = "urgent"; // typo-prone, no type safety
if (priority.equals("high")) { ... }
```

---

## Rule 11: Null Safety — No Returning Null from Public Methods

Public methods must never return `null`. Use `Optional<T>` for nullable lookups, or throw a specific exception.

- **[GOOD]:**
```java
public Optional<WorkOrder> findById(Long id) {
    return repository.findById(id);
}
// Caller:
workOrderService.findById(id)
    .orElseThrow(() -> new EntityNotFoundException("WorkOrder not found: " + id));
```

- **[BAD]:**
```java
public WorkOrder findById(Long id) {
    return repository.findById(id).orElse(null); // null returned to caller
}
```

---

## Rule 12: Test Coverage — Every Public Method Must Have Unit Tests

Every public method must have at least one positive test and one negative/edge test.

- **[GOOD]:** `createWorkOrder_shouldReturn201_whenValidRequest()` and `createWorkOrder_shouldReturn400_whenEquipmentIdBlank()`
- **[BAD]:** No test class exists for `WorkOrderService`.