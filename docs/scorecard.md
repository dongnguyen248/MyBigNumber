# Scorecard - Danh gia Ma Nguon (MyBigNumber.java)

Prompt su dung de sinh draft code:
> Role: Senior Engineer. Task: Complete method sum(String, String).
> Context files: docs/coding-rules.md.
> Constraints: use Java standard coding rules of Oracle and Google.

File duoc danh gia: src/main/java/com/dongnguyen248/add2num/MyBigNumber.java

---

| # | Tieu chi kiem tra (Tu docs/coding-rules.md) | Ket qua | Ghi chu |
|---|---------------------------------------------|---------|---------|
| 1 | **Naming - PascalCase cho class** | PASS | `MyBigNumber` dung PascalCase |
| 2 | **Naming - camelCase cho method/variable** | PASS | `sum()`, `firstDigit`, `writeIndex`... dung camelCase |
| 3 | **Naming - UPPER_SNAKE_CASE cho constant** | PASS | Khong co constant nao; `log` la static final field theo chuan |
| 4 | **Exception Handling - Khong throw RuntimeException chung** | PASS | Khong co throw nao trong class nay; khong xu ly ngoai le bay ve phia ngoai |
| 5 | **Logging - Khong log PII/sensitive data** | PASS | Log chi chua so va do dai chuoi, khong co du lieu nhay cam |
| 6 | **Logging - Dung SLF4J Logger dung chuan** | PASS | `private static final Logger log = LoggerFactory.getLogger(MyBigNumber.class);` |
| 7 | **Dependency Injection - Constructor injection** | PASS | Class la utility class (final, private constructor), khong can DI |
| 8 | **Variable Declaration - Khai bao bien NGOAI vong lap** | PASS | `firstDigit`, `secondDigit`, `carryIn`, `digitSum`, `resultDigit` khai bao truoc for-loop |
| 9 | **Code Simplicity - Khong over-engineering** | PASS | Logic phang, ro rang, khong co factory hay pattern phuc tap thua |
| 10 | **Khong them truong du lieu ngoai yeu cau (strict schema)** | PASS | Ham chi tra ve String sum, khong tra ve object thua |
| 11 | **Class la final (utility class khong the ke thua)** | PASS | `public final class MyBigNumber` |
| 12 | **Private constructor ngan instantiation** | PASS | `private MyBigNumber() {}` |

---

## Ket qua tong the

| Hang muc | So luong |
|---|---|
| Tieu chi kiem tra | 12 |
| PASS | 12 |
| FAIL | 0 |

**Tong diem: 12/12 - DAT (PASS)**

---

## Ghi chu them

- File hien tai KHONG co @Valid annotation vi day la utility method (String-in / String-out), khong phai REST endpoint -> khong ap dung tieu chi validation annotation cua api-rules.md.
- BenchmarkTest.java co ca 3 phien ban Old/New/CharArray de chung minh hieu qua viec khai bao bien ngoai vong lap (tieu chi #8).