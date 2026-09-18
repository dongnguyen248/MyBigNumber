# Add2Num Core Library

A Java library that adds non-negative decimal numbers represented as strings. The algorithm processes digits from right to left, like elementary-school long addition.

## Definition of Done

A `0.0.1` release is complete only when all of the following are true:

- A clean build passes.
- Normal tests and the performance-test profile pass.
- Annotated Git tag `0.0.1` exists on the release commit.
- The repository is available at `https://github.com/dongnguyen248/MyBigNumber`.
- A fresh clone can check out tag `0.0.1` and successfully build and test the release.

## Maven Coordinates

Task 2 imports this library with the following dependency after the artifact is published to an accessible Maven repository:

```xml
<dependency>
    <groupId>com.dongnguyen248</groupId>
    <artifactId>add2num</artifactId>
    <version>0.0.1</version>
</dependency>
```

`mvn install` publishes the main JAR, sources JAR, and Javadoc JAR to the local Maven repository. The Java package is `com.dongnguyen248.add2num`.

## API Contract

`MyBigNumber` is a final utility class with a private constructor. Its public API is static:

```java
String sum(String stn1, String stn2)
String sum(String stn1, String stn2, ProgressListener listener)
```

Inputs are assumed to be non-negative strings containing only valid decimal digits. The library deliberately does not validate inputs. It does not use `BigInteger` to perform addition; instead, it processes each input column using `StringBuilder` and one `reverse()` operation. Runtime and additional memory use are both O(n), where n is the longer input length.

## Progress Contract

`ProgressListener` receives this synchronous callback:

```java
void onProgress(int completedSteps, int totalSteps, int resultDigit, int carry)
```

The callback runs synchronously on the thread that calls `sum`; no asynchronous worker or separate thread is created. A listener implementation must be lightweight and non-blocking. For long inputs, a consumer such as Task 2 should throttle or batch UI rendering because a 100,000-digit input creates 100,000 callbacks.

`totalSteps` is `max(stn1.length(), stn2.length())`. The listener is called exactly once for each input column, with `completedSteps` running from 1 through `totalSteps`. The final carry is appended to the result but does not trigger an additional callback. For example, `sum("999", "1", listener)` returns `"1000"` and calls the listener exactly three times.

## Result Formatting

Results are canonical decimal strings. Leading zeroes are removed, with one zero retained for a zero result:

```java
MyBigNumber.sum("000", "1");   // "1"
MyBigNumber.sum("000", "000"); // "0"
```

This prevents values such as `"001"` from being treated as a formatting defect or being displayed directly in Task 2.

## Logging

The library exposes logging through the SLF4J API. It has no production logging backend; the consuming application chooses and configures one.

- INFO logs the start and completion of an addition.
- DEBUG logs each processed column, including digits, carry in/out, total, and stored result digit.
- DEBUG column numbers start at 1 and count from right to left, matching the addition algorithm and progress callbacks.

Per-column DEBUG logs use SLF4J parameterized messages such as:

```java
logger.debug("col={} d1={} d2={} carryIn={} sum={} digit={} carryOut={}", ...);
```

Do not concatenate or pre-format log strings inside the addition loop. The loop can process 100,000 columns, so unnecessary string allocation can affect performance even when DEBUG logging is disabled.

The Javadoc plugin configures `doclint` as `none` to prevent Java 17 doclint differences from blocking `package` or `install`. This is only a build-safety fallback; public API documentation still includes complete `@param` and `@return` tags.

## Build and Test

Prerequisites:

- Java 17
- Maven 3.9 or later

Run the normal test suite. It excludes tests tagged `performance`:

```powershell
mvn clean test
```

Run the 100,000-digit performance test. The `performance` profile selects only JUnit tests tagged `@Tag("performance")`:

```powershell
mvn -Pperformance test
```

Build all artifacts:

```powershell
mvn package
```

Install the main, sources, and Javadoc artifacts locally:

```powershell
mvn install
```

Run the Maven demo. It prints `2131` for `1234 + 897`:

```powershell
mvn -q compile exec:java
```

The produced `target/add2num-0.0.1.jar` is a thin library JAR, not an executable application. Do not run it with `java -jar`; use the Maven demo command above or supply the required classpath in a consuming application.

## Clone the Core Release

```powershell
git clone https://github.com/dongnguyen248/MyBigNumber.git
cd MyBigNumber
git switch core
mvn clean test
mvn -Pperformance test
```

To build the exact Task 1 release instead of the latest `core` branch, use `git switch --detach 0.0.1`.
