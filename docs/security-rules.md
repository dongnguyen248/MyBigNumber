# Security Rules

> **Applies to:** All source code, configuration files, and infrastructure scripts in this project.

---

## 1. No Hardcoded Secrets

Never hardcode API keys, passwords, connection strings, JWT secrets, or tokens in:
- Source code (.java, .py, .js, etc.)
- Configuration files (application.properties, application.yml)
- Comments or documentation

Use environment variables or a secrets manager (e.g., HashiCorp Vault, AWS Secrets Manager).

Example BAD:
  String dbPassword = "mySecret123!";

Example GOOD:
  String dbPassword = System.getenv("DB_PASSWORD");

---

## 2. Input Sanitization and Validation

- Always validate and sanitize inputs at the controller boundary.
- Never trust client payloads.
- Reject unexpected or malformed input early (fail fast).
- Strip or escape special characters where needed.

Example GOOD (Spring):
  @PostMapping("/api/work-orders")
  public ResponseEntity<?> create(@Valid @RequestBody WorkOrderRequest req) { ... }

---

## 3. SQL / Injection Protection

- Use Spring Data JPA / Hibernate parameterized queries or ORM methods.
- Never concatenate strings to build native SQL or JPQL queries.
- Never use user-supplied values directly in query strings.

Example BAD:
  String query = "SELECT * FROM users WHERE name = '" + username + "'";

Example GOOD:
  userRepository.findByName(username); // ORM method

---

## 4. Authorization Checks (RBAC)

- Ensure proper role-based access control is declared on all sensitive endpoints.
- Use @PreAuthorize or equivalent security annotations.
- Do not rely solely on UI to hide functionality; enforce server-side.

Example GOOD:
  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/api/work-orders/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) { ... }

---

## 5. Sensitive Data - No Logging of PII

- Do NOT log passwords, tokens, credit card numbers, national IDs, emails, or phone numbers.
- When logging request parameters, mask or omit sensitive fields.

Example BAD:
  log.info("Login attempt for user: {} with password: {}", username, password);

Example GOOD:
  log.info("Login attempt for user: {}", username);

---

## 6. Dependency Security

- Keep all dependencies up to date.
- Regularly run security audits (e.g., `mvn dependency-check:check` or OWASP Dependency-Check).
- Do not import libraries from untrusted or unmaintained sources.

---

## 7. Error Message Disclosure

- Do NOT expose internal stack traces, database error messages, or system internals to the client.
- Return generic error messages to the client; log full details server-side only.

Example BAD (response to client):
  java.sql.SQLException: Table 'users' doesn't exist at ...

Example GOOD (response to client):
  { "status": 500, "title": "Internal Server Error", "detail": "An unexpected error occurred." }