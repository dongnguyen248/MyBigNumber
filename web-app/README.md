# Add2Num Web

Spring Boot MVC interface for adding two non-negative decimal numbers of arbitrary length. It uses the released Task 1 library as a Maven binary dependency; the web module does not copy or link to the core source tree.

## Prerequisites

- Java 17 or newer
- Maven 3.9 or newer

Install the core release in the local Maven repository before building the web application:

```powershell
mvn clean install
mvn -f web-app/pom.xml clean test
```

Start the application with:

```powershell
mvn -f web-app/pom.xml spring-boot:run
```

Open `http://localhost:8080`.

## Architecture

`AdditionService` creates a distinct UUID job, schedules it on a bounded Spring executor, and calls only `MyBigNumber.sum(firstNumber, secondNumber, listener)` from `com.dongnguyen248:add2num:0.0.1`.

The core listener updates each job's real completed-step snapshot. The application publishes SSE progress only when integer percentage changes, limiting a long calculation to at most 100 browser updates while preserving exact counts in the status response. Completed and failed snapshots are retained for five minutes by default, which handles calculations that finish before an EventSource connection opens.

## HTTP API

`POST /api/additions` accepts JSON such as:

```json
{
  "firstNumber": "1234",
  "secondNumber": "897"
}
```

It returns `202 Accepted` with a job ID plus `eventsUrl` and `statusUrl`. Both number fields are required and must contain decimal digits only.

`GET /api/additions/{jobId}` returns the current job snapshot. Unknown or expired IDs return `404`.

`GET /api/additions/{jobId}/events` is an SSE stream. Events are named `status`, `progress`, `completed`, or `failed`; each event contains the same status snapshot returned by the status endpoint.

## Configuration

`src/main/resources/application.yml` configures port `8080`, a 60-second SSE timeout, five-minute completed-job retention, and executor limits of 2 core threads, 8 maximum threads, and 100 queued jobs.

Package an executable Boot JAR with:

```powershell
mvn -f web-app/pom.xml package
```