# BR Analysis - Work Order Management (POSCO MCI)

## Mo ta yeu cau tho tu Product Owner

> "Du an POSCO MCI: Can lam gap tinh nang Quan ly Phieu cong viec (Work Order) tren ung dung di dong cho thu ky thuat o cong truong. Thu vao app nhap ma thiet bi (equipment_id), chon muc do uu tien (low, medium, high, urgent) va viet mo ta cong viec (description). He thong luu lai va hien thi danh sach cho quan doc xem. Yeu cau lam nhanh trong tuan nay, khong can phan quyen phuc tap vi ai dang nhap app cong truong cung la thu, mien la nhap dung ma thiet bi dang hoat dong."

---

## 1. Danh sach Thuc the (Entities) va Thuoc tinh

### 1.1 WorkOrder (Phieu cong viec)

| Thuoc tinh | Kieu du lieu | Rang buoc | Ghi chu |
|---|---|---|---|
| `id` | Long / UUID | Auto-generated, PK | Khoa chinh |
| `equipment_id` | String | NOT NULL, phai la ma thiet bi hop le dang hoat dong | Can validate voi danh sach thiet bi |
| `description` | String | NOT NULL, NOT BLANK | Mo ta cong viec |
| `priority` | Enum (LOW, MEDIUM, HIGH, URGENT) | NOT NULL | Muc do uu tien |
| `status` | Enum (NEW, IN_PROGRESS, DONE) | NOT NULL, default = NEW | Trang thai phieu |
| `created_by` | String / FK -> User | NOT NULL | Thu ky thuat tao phieu |
| `created_at` | LocalDateTime | Auto-set | Thoi gian tao |
| `updated_at` | LocalDateTime | Auto-set | Thoi gian cap nhat cuoi |

### 1.2 Equipment (Thiet bi) - Tham chieu

| Thuoc tinh | Kieu du lieu | Ghi chu |
|---|---|---|
| `equipment_id` | String | Ma thiet bi duy nhat |
| `status` | Enum (ACTIVE, INACTIVE) | Chi cho phep tao WorkOrder cho thiet bi ACTIVE |
| `name` | String | Ten thiet bi |

### 1.3 User (Nguoi dung - Thu ky thuat / Quan doc)

| Thuoc tinh | Kieu du lieu | Ghi chu |
|---|---|---|
| `id` | Long | PK |
| `username` | String | Ten dang nhap |
| `role` | Enum (TECHNICIAN, SUPERVISOR) | Phan quyen co ban |

---

## 2. Cau hoi con bo ngo / Rui ro nghiep vu (Open Questions)

| # | Cau hoi | Muc do uu tien | Nguoi can tra loi |
|---|---|---|---|
| OQ-01 | Xac thuc equipment_id duoc thuc hien nhu the nao? He thong co API danh sach thiet bi dang hoat dong de validate? | HIGH | Product Owner + Tech Lead |
| OQ-02 | "Ai dang nhap cung la thu" - co nghia la ung dung khong co Authentication? Hay co SSO / login co san cua cong truong? | CRITICAL | Product Owner + Security Team |
| OQ-03 | Quan doc xem danh sach - co the xem tat ca cong viec hay chi xem cong viec thuoc khu vuc/ca minh quan ly? | MEDIUM | Product Owner |
| OQ-04 | Thu ky thuat sau khi tao phieu co the sua / huy phieu khong? Hay chi quan doc moi duoc? | MEDIUM | Product Owner |
| OQ-05 | Truong "status" co flow chuyen trang thai ro rang khong? Ai duoc phep chuyen tu NEW -> IN_PROGRESS -> DONE? | HIGH | Product Owner |
| OQ-06 | Ung dung co can hoat dong offline (mat mang tai cong truong) roi dong bo sau? | MEDIUM | Product Owner + Mobile Dev |
| OQ-07 | Du lieu Work Order can luu bao lau? Co can chinh sach xoa du lieu cu khong? | LOW | Product Owner |
| OQ-08 | Can thong bao (push notification) cho quan doc khi co phieu moi khong? | LOW | Product Owner |

---

## 3. Bang phan ru chi tiet theo mo hinh 3 tang (UI / Data / API)

### 3.1 Tang UI (Giao dien nguoi dung - Mobile App)

| Tinh nang | Man hinh / Component | Mo ta |
|---|---|---|
| Tao phieu cong viec | CreateWorkOrderScreen | Form nhap: equipment_id (text field), priority (dropdown: LOW/MEDIUM/HIGH/URGENT), description (textarea) |
| Xem danh sach phieu | WorkOrderListScreen | Danh sach co loc theo priority, status; hien thi cho quan doc |
| Validate equipment_id | Inline trong form | Kiem tra ma thiet bi truoc khi cho submit |
| Thong bao loi | Toast / Error message | Hien thi loi theo RFC 7807 Problem Details |

### 3.2 Tang Data (Database / Entity)

| Bang | Columns chinh | Index / Constraint |
|---|---|---|
| `work_orders` | id, equipment_id, description, priority, status, created_by, created_at, updated_at | INDEX tren equipment_id, priority, status, created_at |
| `equipments` | equipment_id, name, status | PK = equipment_id |
| `users` | id, username, role, password_hash | UNIQUE tren username |

### 3.3 Tang API (REST Endpoints)

| Method | Endpoint | Mo ta | Auth can thiet |
|---|---|---|---|
| POST | /api/work-orders | Tao phieu cong viec moi | TECHNICIAN role |
| GET | /api/work-orders | Lay danh sach phieu (co loc, phan trang) | TECHNICIAN / SUPERVISOR |
| GET | /api/work-orders/{id} | Lay chi tiet mot phieu | TECHNICIAN / SUPERVISOR |
| PATCH | /api/work-orders/{id}/status | Cap nhat trang thai phieu | SUPERVISOR role |
| GET | /api/equipments/active | Lay danh sach thiet bi dang hoat dong | TECHNICIAN (de validate) |

---

## 4. Rui ro ky thuat can xu ly

| Rui ro | Muc do | Giai phap de xuat |
|---|---|---|
| Phat bieu "khong can phan quyen phuc tap" co the dan den bo qua Auth hoan toan | CRITICAL | Toi thieu phai xac thuc nguoi dung (JWT / Session). Phan quyen co ban TECHNICIAN/SUPERVISOR la bat buoc |
| SQL Injection neu dung string concatenation | CRITICAL | Bat buoc dung Spring Data JPA / PreparedStatement |
| equipment_id khong duoc validate voi he thong thiet bi thuc te | HIGH | Goi API hoac query DB kiem tra equipment ton tai va ACTIVE truoc khi insert |
| Khong co created_by -> khong biet ai tao phieu | HIGH | Lay user hien tai tu Security Context, khong de client tu khai bao |