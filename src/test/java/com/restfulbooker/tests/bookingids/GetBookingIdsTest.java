package com.restfulbooker.tests.bookingids;

import com.restfulbooker.base.ApiConfig;
import com.restfulbooker.base.BaseApiTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class GetBookingIdsTest extends BaseApiTest {

    @Test(
            description = "TC_GBI_001 - Verify all booking IDs are returned without filters",
            priority = 1
    )
    public void verifyAllBookingIdsWithoutFilters() {
        Response response = sendBookingIdsRequest(null, null);

        assertStatusCode(response, 200, "TC_GBI_001 failed");
        assertBookingIdsArray(response, true, "TC_GBI_001 failed");
    }

    @Test(
            description = "TC_GBI_002 - Verify booking IDs are returned for firstname filter",
            priority = 2
    )
    public void verifyBookingIdsFilteredByFirstname() {
        Response response = sendBookingIdsRequest("firstname", "Jim");

        assertStatusCode(response, 200, "TC_GBI_002 failed");
        assertBookingIdsArray(response, true, "TC_GBI_002 failed");
    }

    @Test(
            description = "TC_GBI_003 - Verify booking IDs are returned for lastname filter",
            priority = 3
    )
    public void verifyBookingIdsFilteredByLastname() {
        Response response = sendBookingIdsRequest("lastname", "Brown");

        assertStatusCode(response, 200, "TC_GBI_003 failed");
        assertBookingIdsArray(response, true, "TC_GBI_003 failed");
    }

    @Test(
            description = "TC_GBI_004 - Verify booking IDs are returned for checkin filter",
            priority = 4
    )
    public void verifyBookingIdsFilteredByCheckinDate() {
        Response response = sendBookingIdsRequest("checkin", "2024-01-01");

        assertStatusCode(response, 200, "TC_GBI_004 failed");
        assertBookingIdsArray(response, true, "TC_GBI_004 failed");
    }

    @Test(
            description = "TC_GBI_005 - Verify booking IDs are returned for checkout filter",
            priority = 5
    )
    public void verifyBookingIdsFilteredByCheckoutDate() {
        Response response = sendBookingIdsRequest("checkout", "2024-12-31");

        assertStatusCode(response, 200, "TC_GBI_005 failed");
        assertBookingIdsArray(response, true, "TC_GBI_005 failed");
    }

    @Test(
            description = "TC_GBI_006 - Verify empty array is returned for non-matching firstname filter",
            priority = 6
    )
    public void verifyEmptyResultForNonMatchingFirstname() {
        Response response = sendBookingIdsRequest("firstname", "ZZZZZZ");

        assertStatusCode(response, 200, "TC_GBI_006 failed");

        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> bookings = jsonPath.getList("$");

        Assert.assertNotNull(
                bookings,
                "TC_GBI_006 failed: expected an empty JSON array, but response body was null."
        );
        Assert.assertEquals(
                bookings.size(),
                0,
                "TC_GBI_006 failed: expected empty JSON array [] for a non-matching firstname filter."
                        + " Document actual vs expected if this endpoint returns records instead."
                        + " Actual response body: " + response.getBody().asString()
        );
    }

    private Response sendBookingIdsRequest(String queryParamName, String queryParamValue) {
        if (queryParamName == null || queryParamValue == null) {
            return given()
                    .spec(requestSpec)
                    .when()
                    .get(ApiConfig.BOOKING_ENDPOINT)
                    .then()
                    .extract()
                    .response();
        }

        return given()
                .spec(requestSpec)
                .queryParam(queryParamName, queryParamValue)
                .when()
                .get(ApiConfig.BOOKING_ENDPOINT)
                .then()
                .extract()
                .response();
    }

    private void assertStatusCode(Response response, int expectedStatusCode, String failurePrefix) {
        Assert.assertEquals(
                response.getStatusCode(),
                expectedStatusCode,
                failurePrefix + ": expected status code " + expectedStatusCode + "."
        );
    }

    private void assertBookingIdsArray(Response response, boolean expectAtLeastOneRecord, String failurePrefix) {
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> bookings = jsonPath.getList("$");

        Assert.assertNotNull(
                bookings,
                failurePrefix + ": response should be a JSON array."
        );
        if (expectAtLeastOneRecord) {
            Assert.assertTrue(
                    bookings.size() >= 1,
                    failurePrefix + ": expected at least one booking record."
            );
        }

        for (Map<String, Object> booking : bookings) {
            Assert.assertTrue(
                    booking.containsKey("bookingid"),
                    failurePrefix + ": each array element should contain 'bookingid'."
            );
            Assert.assertTrue(
                    booking.get("bookingid") instanceof Integer,
                    failurePrefix + ": 'bookingid' should be an integer."
            );
        }
    }
}
