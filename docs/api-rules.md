# API Design Rules

> **Scope:** All REST endpoints exposed by this project.

---

## 1. REST Resource Naming

- Use plural nouns for resource paths (e.g., `/api/work-orders`, not `/api/workOrder`).
- Use kebab-case for multi-word resources.
- Do NOT use verbs in URIs.

Example - GOOD: `POST /api/work-orders`
Example - BAD:  `POST /api/createWorkOrder`

---

## 2. HTTP Verbs

| Method | Use for |
|--------|---------|
| GET    | Retrieve a resource or collection |
| POST   | Create a new resource |
| PUT    | Full replacement of an existing resource |
| PATCH  | Partial update of an existing resource |
| DELETE | Remove a resource |

---

## 3. Strict Schema Conformance

- DO NOT invent extra JSON fields not defined in requirements or API spec.
- Response bodies must contain only the fields listed in the agreed contract.

---

## 4. Error Responses - RFC 7807 Problem Details

All error responses must follow RFC 7807 Problem Details format.

Required fields: type (URI), title (string), status (int), detail (string).

Example GOOD response body:
  { "type": "...", "title": "Validation Failed", "status": 400, "detail": "Field X is blank" }

Example BAD:
  "Validation error: field cannot be null at line 42..."

---

## 5. Input Validation

- All incoming request bodies must be annotated with @Valid.
- Use Bean Validation: @NotNull, @NotBlank, @Size, @Pattern on DTO fields.
- Return 400 Bad Request with Problem Details on validation failure.

---

## 6. HTTP Status Codes

| Scenario | Code |
|---|---|
| Resource created | 201 Created |
| Successful retrieval/update | 200 OK |
| Validation / bad input | 400 Bad Request |
| Not authenticated | 401 Unauthorized |
| Insufficient permission | 403 Forbidden |
| Resource not found | 404 Not Found |
| Internal server error | 500 Internal Server Error |

---

## 7. Response Envelope Consistency

- Do not mix response structures between endpoints.
- Do not return raw primitives or raw lists at the top level. Wrap in a typed response object.

Example GOOD: { "data": [...], "total": 42 }
Example BAD:  [{...}, {...}]