# Restful Booker API Automation Portfolio

This project is an API automation portfolio for [Restful Booker](https://restful-booker.herokuapp.com) built with Java, Maven, RestAssured, and TestNG.

## Tech Stack

- Java 17
- Maven
- RestAssured
- TestNG
- Extent Reports

## Project Goal

Automate the Restful Booker API flow in this order:

`Ping -> Auth -> Get Booking IDs -> Get Booking -> Create -> Update -> Patch -> Delete`

Known bug-focus areas to document during execution:

- `TC_CB_006`
- `TC_DEL_001`
- `TC_GBI_006`

## Implemented So Far

### Ping

- `TC_PING_001` - API health check, server is up

### Auth

- `TC_AUTH_001` - Valid credentials, token generated
- `TC_AUTH_002` - Invalid username, bad credentials
- `TC_AUTH_003` - Invalid password, bad credentials
- `TC_AUTH_004` - Empty username and password
- `TC_AUTH_005` - Missing request body entirely

### Get Booking IDs

- `TC_GBI_001` - Get all booking IDs without filters
- `TC_GBI_002` - Filter by firstname query param
- `TC_GBI_003` - Filter by lastname query param
- `TC_GBI_004` - Filter by checkin date
- `TC_GBI_005` - Filter by checkout date
- `TC_GBI_006` - Non-matching firstname returns empty result

## Framework Structure

```text
src/test/java/com/restfulbooker
|-- base
|   |-- ApiConfig.java
|   `-- BaseApiTest.java
|-- listeners
|   `-- ExtentReportListener.java
|-- models
|   `-- AuthRequest.java
|-- reports
|   `-- ExtentReportManager.java
`-- tests
    |-- auth
    |   `-- AuthTest.java
    |-- bookingids
    |   `-- GetBookingIdsTest.java
    `-- ping
        `-- PingTest.java
```

## Reporting

- Extent Reports are integrated through a TestNG listener.
- Report files are generated under `test-output/extent-report`.
- The framework attempts to open the generated HTML report automatically after suite execution.

## How To Run

Run full suite:

```powershell
mvn test
```

Run ping tests only:

```powershell
mvn "-Dtest=com.restfulbooker.tests.ping.PingTest" test
```

Run auth tests only:

```powershell
mvn "-Dtest=com.restfulbooker.tests.auth.AuthTest" test
```

Run get booking IDs tests only:

```powershell
mvn "-Dtest=com.restfulbooker.tests.bookingids.GetBookingIdsTest" test
```

## Current Execution Notes

- `TC_AUTH_001` is implemented correctly, but in recent runs the response-time assertion has exceeded the expected `< 3000 ms` threshold.
- Functional validation for `TC_AUTH_001` is passing:
  - Status code `200`
  - Token field present
  - Token is non-empty
- `TC_AUTH_002` to `TC_AUTH_005` were executed and passed.
- `TC_PING_001` was executed and passed.
- `TC_GBI_001` to `TC_GBI_006` were executed and passed.
- For `TC_GBI_006`, expected behavior and actual behavior matched in the latest execution:
  - Expected: `200` with empty JSON array `[]`
  - Actual: `200` with empty JSON array `[]`

## Test Case Source

Test cases are maintained in:

`Restful_Booker_TestCases.xlsx`

## Next Scope

- `GET_BOOKING` module
- `CREATE_BOOKING` module
- `UPDATE_BOOKING` module
- `PATCH_BOOKING` module
- `DELETE_BOOKING` module
