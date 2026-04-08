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

### Get Booking

- `TC_GB_001` - Get booking by valid ID
- `TC_GB_002` - Get booking by non-existent ID
- `TC_GB_003` - Get booking with ID = 0
- `TC_GB_004` - Get booking with string as ID

### Create Booking

- `TC_CB_001` - Create booking with all valid fields
- `TC_CB_002` - Create booking with missing firstname
- `TC_CB_003` - Create booking with missing totalprice
- `TC_CB_004` - Create booking with missing depositpaid
- `TC_CB_005` - Create booking with missing bookingdates
- `TC_CB_006` - Create booking with invalid date format
- `TC_CB_007` - Verify create booking response schema

### Update Booking

- `TC_UB_001` - Full update with valid auth token
- `TC_UB_002` - Full update without auth token
- `TC_UB_003` - Full update with invalid token
- `TC_UB_004` - Update non-existent booking ID
- `TC_UB_005` - Update with missing required fields

### Patch Booking

- `TC_PU_001` - Partial update firstname only
- `TC_PU_002` - Partial update totalprice only
- `TC_PU_003` - Partial update without auth token
- `TC_PU_004` - Partial update non-existent booking ID

### Delete Booking

- `TC_DEL_001` - Delete booking with valid auth token
- `TC_DEL_002` - Delete booking without auth token
- `TC_DEL_003` - Delete booking with invalid auth token
- `TC_DEL_004` - Delete non-existent booking ID
- `TC_DEL_005` - Verify deleted booking returns 404

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
    |-- booking
    |   `-- GetBookingTest.java
    |-- bookingids
    |   `-- GetBookingIdsTest.java
    |-- createbooking
    |   `-- CreateBookingTest.java
    |-- deletebooking
    |   `-- DeleteBookingTest.java
    `-- ping
        `-- PingTest.java
    |-- patchbooking
    |   `-- PatchBookingTest.java
    `-- updatebooking
        `-- UpdateBookingTest.java
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

Run get booking tests only:

```powershell
mvn "-Dtest=com.restfulbooker.tests.booking.GetBookingTest" test
```

Run create booking tests only:

```powershell
mvn "-Dtest=com.restfulbooker.tests.createbooking.CreateBookingTest" test
```

Run update booking tests only:

```powershell
mvn "-Dtest=com.restfulbooker.tests.updatebooking.UpdateBookingTest" test
```

Run patch booking tests only:

```powershell
mvn "-Dtest=com.restfulbooker.tests.patchbooking.PatchBookingTest" test
```

Run delete booking tests only:

```powershell
mvn "-Dtest=com.restfulbooker.tests.deletebooking.DeleteBookingTest" test
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
- `TC_GB_001` to `TC_GB_004` were executed and passed.
- `TC_CB_001` to `TC_CB_007` were executed and passed.
- For `TC_CB_006`, expected behavior and actual behavior matched in the latest execution:
  - Expected: `200`
  - Actual: `200`
- `TC_UB_001` to `TC_UB_005` were executed and passed.
- `TC_PU_001` to `TC_PU_004` were executed and passed.
- `TC_DEL_001` to `TC_DEL_005` were executed and passed.
- For `TC_DEL_001`, expected behavior and actual behavior matched in the latest execution:
  - Expected: `201` with body `Created`, then `GET /booking/{id}` -> `404`
  - Actual: `201` with body `Created`, then `GET /booking/{id}` -> `404`

## Test Case Source

Test cases are maintained in:

`Restful_Booker_TestCases.xlsx`

## Status

Initial end-to-end framework setup and all planned modules from the provided workbook are now implemented in the automation suite.
