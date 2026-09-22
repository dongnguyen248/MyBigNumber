# AI-Assisted Code Review - WorkOrderController.java

**Reviewer role:** Senior Security Code Reviewer
**File reviewed:** WorkOrderController.java (POSCO MCI - du lieu mau tu LAB 2.2)
**Review date:** 2026-09-22

---

## Doan code duoc review

```java
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    @Autowired
    private DataSource dataSource;

    private static final String JWT_SECRET = "posco_mci_secret_key_2026_xyz";

    @PostMapping("/create-wo")
    public ResponseEntity<?> createWorkOrder(@RequestBody WorkOrderRequest request) {
        // Bo qua buoc kiem tra xac thuc (Authentication/Authorization)

        // Ghep noi chuoi truc tiep vao cau lenh SQL (Loi bao mat nghiem trong)
        String query = "INSERT INTO work_orders (equipment_id, description, priority, status) VALUES ('"
                + request.getEquipmentId() + "', '"
                + request.getDescription() + "', '"
                + request.getPriority() + "', 'NEW')";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(query);
            System.out.println("Da tao phieu thanh cong cho thiet bi: " + request.getEquipmentId());

            return ResponseEntity.ok().body(new ApiResponse(true, "Tao phieu thanh cong!", null));

        } catch (Exception e) {
            // Lo thong tin nhay cam cua he thong va cau truy van loi ra ngoai
            return ResponseEntity.status(500).body(new ErrorResponse(e.getMessage(), query));
        }
    }
}
```

---

## Review Comments (Theo thu tu uu tien)

---

### [P1 - SPEC DELTA] RC-01: Sai ten endpoint so voi API Rules

**Vi tri:** `@PostMapping("/create-wo")`
**Van de:** Endpoint dung dong tu "create" trong URI, vi pham `docs/api-rules.md` Rule #1 (REST Resource Naming - khong dung dong tu trong URI).
**Muc do:** SPEC DELTA - Lech dac ta
**Sua lai:**
```java
// GOOD
@PostMapping   // POST /api/work-orders la du, khong can /create-wo
```

---

### [P1 - SPEC DELTA] RC-02: Thieu truong "created_by" - Khong biet ai tao phieu

**Vi tri:** Cau SQL INSERT khong co truong `created_by`
**Van de:** Theo phan tich nghiep vu (br-analysis-wo.md), moi Work Order phai luu thong tin ai tao ra. SQL hien tai khong co `created_by`, dan den mat traceability.
**Muc do:** SPEC DELTA - Thieu nghiep vu
**Sua lai:** Lay username tu Security Context, them vao INSERT.

---

### [P2 - SECURITY] RC-03: Hardcoded JWT Secret - Loi bao mat NGHIEM TRONG

**Vi tri:** `private static final String JWT_SECRET = "posco_mci_secret_key_2026_xyz";`
**Van de:** JWT secret bi hardcode truc tiep trong source code. Bat ky ai co the doc duoc source code (GitHub, log, IDE) deu co the giai ma hoac gia mao token. Vi pham `docs/security-rules.md` Rule #1.
**Muc do:** CRITICAL SECURITY
**Sua lai:**
```java
// GOOD - Doc tu environment variable
@Value("${jwt.secret}")
private String jwtSecret;
```

---

### [P2 - SECURITY] RC-04: SQL Injection - Loi bao mat NGHIEM TRONG

**Vi tri:** String concatenation khi xay dung cau SQL
**Van de:** `request.getEquipmentId()`, `request.getDescription()`, `request.getPriority()` duoc ghep noi truc tiep vao chuoi SQL. Attacker co the inject lenh SQL tuy y.
**Vi du tan cong:**
```
equipmentId = "'; DROP TABLE work_orders; --"
```
**Muc do:** CRITICAL SECURITY
**Sua lai:**
```java
// GOOD - Dung Spring Data JPA / PreparedStatement
workOrderRepository.save(new WorkOrder(request));

// Hoac neu bat buoc dung JDBC:
String sql = "INSERT INTO work_orders (equipment_id, description, priority, status) VALUES (?, ?, ?, 'NEW')";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, request.getEquipmentId());
    ps.setString(2, request.getDescription());
    ps.setString(3, request.getPriority());
    ps.executeUpdate();
}
```

---

### [P2 - SECURITY] RC-05: Khong co Authentication / Authorization

**Vi tri:** Toan bo method `createWorkOrder` - khong co annotation bao ve
**Van de:** Bat ky request HTTP nao cung co the tao Work Order ma khong can xac thuc. Khong co `@PreAuthorize`, khong co Spring Security filter, khong co session check. Vi pham `docs/security-rules.md` Rule #4.
**Muc do:** CRITICAL SECURITY
**Sua lai:**
```java
// GOOD
@PreAuthorize("hasRole('TECHNICIAN')")
@PostMapping
public ResponseEntity<WorkOrderResponse> createWorkOrder(
        @Valid @RequestBody WorkOrderRequest request,
        @AuthenticationPrincipal UserDetails currentUser) {
    ...
}
```

---

### [P2 - SECURITY] RC-06: Lo thong tin nhay cam trong Error Response

**Vi tri:** `return ResponseEntity.status(500).body(new ErrorResponse(e.getMessage(), query));`
**Van de:** Tra ve ca `e.getMessage()` (co the lo ten bang, ten cot, cau truc DB) lan `query` (chuoi SQL day du) ra client. Attacker co the doc duoc cau SQL de phan tich cau truc DB. Vi pham `docs/security-rules.md` Rule #7.
**Muc do:** HIGH SECURITY
**Sua lai:**
```java
// GOOD - Chi log server-side, tra ve message chung cho client
log.error("Failed to create work order: {}", e.getMessage(), e);
return ResponseEntity.status(500).body(ProblemDetail.forStatusAndDetail(
    HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred."));
```

---

### [P3 - VALIDATION] RC-07: Thieu @Valid va rang buoc du lieu tren Request DTO

**Vi tri:** `@RequestBody WorkOrderRequest request`
**Van de:** Khong co `@Valid` annotation. Neu `WorkOrderRequest` cung khong co `@NotNull`, `@NotBlank`, he thong se cho phep tao Work Order voi `equipmentId = null`, `description = ""`, `priority` la gia tri bat ky (ke ca gia tri la "'; DROP TABLE..."). Vi pham `docs/api-rules.md` Rule #5.
**Muc do:** HIGH VALIDATION
**Sua lai:**
```java
// Them @Valid vao parameter
public ResponseEntity<?> createWorkOrder(@Valid @RequestBody WorkOrderRequest request)

// Them constraint len DTO
public class WorkOrderRequest {
    @NotBlank private String equipmentId;
    @NotBlank @Size(max = 1000) private String description;
    @NotNull private Priority priority; // dung Enum, khong phai String
}
```

---

### [P3 - VALIDATION] RC-08: Khong validate equipment_id voi danh sach thiet bi hoat dong

**Vi tri:** Truc tiep INSERT ma khong co buoc kiem tra
**Van de:** Theo nghiep vu, chi duoc tao Work Order cho thiet bi dang `ACTIVE`. Hien tai code khong co buoc validate equipment_id hop le, dan den co the tao phieu cho thiet bi da ngung hoat dong hoac ma thiet bi khong ton tai.
**Muc do:** HIGH SPEC / VALIDATION
**Sua lai:**
```java
// Kiem tra truoc khi tao
if (!equipmentRepository.existsByEquipmentIdAndStatus(request.getEquipmentId(), EquipmentStatus.ACTIVE)) {
    return ResponseEntity.badRequest().body(ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Equipment ID khong ton tai hoac khong hoat dong."));
}
```

---

### [P4 - COMPLEXITY] RC-09: Su dung JDBC DataSource thay vi Spring Data JPA

**Vi tri:** `@Autowired private DataSource dataSource;`
**Van de:** Inject raw `DataSource` va viet JDBC thu cong tao ra boilerplate code nhieu, kho maintain, de mac loi (khong dong Connection, khong dung PreparedStatement...). Spring Data JPA da co san trong stack.
**Muc do:** MEDIUM COMPLEXITY
**Sua lai:** Dung `WorkOrderRepository extends JpaRepository<WorkOrder, Long>` thay the.

---

### [P5 - STYLE] RC-10: Dung System.out.println thay vi Logger

**Vi tri:** `System.out.println("Da tao phieu thanh cong cho thiet bi: " + request.getEquipmentId());`
**Van de:** Dung `System.out.println` trong production code - khong co log level, khong co structured logging, khong co log rotation. Vi pham `docs/coding-rules.md` Rule #4 (phai dung SLF4J).
**Muc do:** LOW STYLE
**Sua lai:**
```java
private static final Logger log = LoggerFactory.getLogger(WorkOrderController.class);
// ...
log.info("Work order created for equipment: {}", request.getEquipmentId());
```

---

### [P5 - STYLE] RC-11: Response body khong tuan theo RFC 7807 Problem Details

**Vi tri:** `return ResponseEntity.ok().body(new ApiResponse(true, "Tao phieu thanh cong!", null));`
**Van de:** Class `ApiResponse` tu dinh nghia thay vi dung chuan RFC 7807. Khong nhat quan voi error response format. Vi pham `docs/api-rules.md` Rule #4.
**Muc do:** LOW STYLE
**Sua lai:** Dung `ResponseEntity.status(HttpStatus.CREATED).build()` cho 201 Created, error dung `ProblemDetail` cua Spring 6+.

---

## Tong ket

| Loai | So luong | Muc do |
|---|---|---|
| Spec Delta | 2 | HIGH |
| Security | 4 | CRITICAL / HIGH |
| Validation | 2 | HIGH |
| Complexity | 1 | MEDIUM |
| Style | 2 | LOW |
| **Tong** | **11** | |

**Ket luan:** CODE NAY KHONG THE MERGE. Can sua tat ca cac diem CRITICAL / HIGH truoc khi review lai.

---

## Goi y cau truc code sau khi sua loi

```java
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderController.class);
    private final WorkOrderService workOrderService;

    // Constructor injection (tuan theo coding-rules.md)
    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PreAuthorize("hasRole('TECHNICIAN')")
    @PostMapping
    public ResponseEntity<WorkOrderResponse> createWorkOrder(
            @Valid @RequestBody WorkOrderRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        WorkOrderResponse response = workOrderService.create(request, currentUser.getUsername());
        log.info("Work order created for equipment: {}", request.getEquipmentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```