package com.restfulbooker.tests.booking;

import com.restfulbooker.base.ApiConfig;
import com.restfulbooker.base.BaseApiTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class GetBookingTest extends BaseApiTest {

    private static final int NON_EXISTENT_BOOKING_ID = 999999999;

    @Test(
            description = "TC_GB_001 - Verify booking details are returned for a valid booking ID",
            priority = 1
    )
    public void verifyBookingByValidId() {
        int bookingId = fetchValidBookingIdWithCompleteData();

        Response response = getBookingById(String.valueOf(bookingId));

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "TC_GB_001 failed: expected status code 200 for a valid booking ID."
        );

        JsonPath jsonPath = response.jsonPath();
        Assert.assertNotNull(jsonPath.getString("firstname"),
                "TC_GB_001 failed: 'firstname' should be present.");
        Assert.assertNotNull(jsonPath.getString("lastname"),
                "TC_GB_001 failed: 'lastname' should be present.");
        Assert.assertTrue(jsonPath.get("totalprice") instanceof Integer,
                "TC_GB_001 failed: 'totalprice' should be an integer.");
        Assert.assertTrue(jsonPath.get("depositpaid") instanceof Boolean,
                "TC_GB_001 failed: 'depositpaid' should be a boolean.");
        Assert.assertNotNull(jsonPath.getString("bookingdates.checkin"),
                "TC_GB_001 failed: 'bookingdates.checkin' should be present.");
        Assert.assertNotNull(jsonPath.getString("bookingdates.checkout"),
                "TC_GB_001 failed: 'bookingdates.checkout' should be present.");
        Assert.assertNotNull(jsonPath.get("additionalneeds"),
                "TC_GB_001 failed: 'additionalneeds' field should be present.");
    }

    @Test(
            description = "TC_GB_002 - Verify non-existent booking ID returns 404",
            priority = 2
    )
    public void verifyNonExistentBookingIdReturnsNotFound() {
        Response response = getBookingById(String.valueOf(NON_EXISTENT_BOOKING_ID));

        assertNotFoundResponse(
                response,
                "TC_GB_002 failed: expected 404 Not Found for a non-existent booking ID."
        );
    }

    @Test(
            description = "TC_GB_003 - Verify booking ID 0 returns 404",
            priority = 3
    )
    public void verifyZeroBookingIdReturnsNotFound() {
        Response response = getBookingById("0");

        assertNotFoundResponse(
                response,
                "TC_GB_003 failed: expected 404 Not Found for booking ID 0."
        );
    }

    @Test(
            description = "TC_GB_004 - Verify string booking ID returns 404",
            priority = 4
    )
    public void verifyStringBookingIdReturnsNotFound() {
        Response response = getBookingById("abc");

        assertNotFoundResponse(
                response,
                "TC_GB_004 failed: expected 404 Not Found for a string booking ID."
        );
    }

    private int fetchValidBookingIdWithCompleteData() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(ApiConfig.BOOKING_ENDPOINT)
                .then()
                .extract()
                .response();

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "Unable to fetch a valid booking ID: /booking should return 200."
        );

        List<Map<String, Object>> bookings = response.jsonPath().getList("$");
        Assert.assertNotNull(
                bookings,
                "Unable to fetch a valid booking ID: response should be a JSON array."
        );
        Assert.assertFalse(
                bookings.isEmpty(),
                "Unable to fetch a valid booking ID: booking list should not be empty."
        );

        int candidatesToCheck = Math.min(bookings.size(), 20);
        for (int index = 0; index < candidatesToCheck; index++) {
            Object bookingId = bookings.get(index).get("bookingid");
            if (!(bookingId instanceof Integer)) {
                continue;
            }

            Response bookingResponse = getBookingById(String.valueOf(bookingId));
            if (bookingResponse.getStatusCode() != 200) {
                continue;
            }

            JsonPath bookingJson = bookingResponse.jsonPath();
            boolean hasCompleteData =
                    bookingJson.getString("firstname") != null
                            && bookingJson.getString("lastname") != null
                            && bookingJson.get("totalprice") instanceof Integer
                            && bookingJson.get("depositpaid") instanceof Boolean
                            && bookingJson.getString("bookingdates.checkin") != null
                            && bookingJson.getString("bookingdates.checkout") != null
                            && bookingJson.get("additionalneeds") != null;

            if (hasCompleteData) {
                return (Integer) bookingId;
            }
        }

        Assert.fail(
                "Unable to fetch a valid booking ID with complete data."
                        + " Checked the first "
                        + candidatesToCheck
                        + " booking IDs, but none contained all expected fields including 'additionalneeds'."
        );
        return -1;
    }

    private Response getBookingById(String bookingId) {
        return given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .when()
                .get(ApiConfig.BOOKING_ENDPOINT + "/{id}")
                .then()
                .extract()
                .response();
    }

    private void assertNotFoundResponse(Response response, String failurePrefix) {
        Assert.assertEquals(
                response.getStatusCode(),
                404,
                failurePrefix + " Expected status code 404."
        );
        Assert.assertEquals(
                response.getBody().asString().trim(),
                "Not Found",
                failurePrefix + " Expected response body to be 'Not Found'."
        );
    }
}
