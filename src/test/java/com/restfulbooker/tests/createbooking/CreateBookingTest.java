package com.restfulbooker.tests.createbooking;

import com.restfulbooker.base.ApiConfig;
import com.restfulbooker.base.BaseApiTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class CreateBookingTest extends BaseApiTest {

    private static Integer createdBookingId;

    @Test(
            description = "TC_CB_001 - Verify booking is created with all valid fields",
            priority = 1
    )
    public void verifyCreateBookingWithAllValidFields() {
        Map<String, Object> payload = buildValidBookingPayload();

        Response response = createBooking(payload);

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "TC_CB_001 failed: expected status code 200 for valid booking creation."
        );

        JsonPath jsonPath = response.jsonPath();
        createdBookingId = jsonPath.getInt("bookingid");

        Assert.assertTrue(
                createdBookingId > 0,
                "TC_CB_001 failed: response should contain a positive integer bookingid."
        );
        assertBookingMatchesPayload(jsonPath, payload, "TC_CB_001 failed");
    }

    @Test(
            description = "TC_CB_002 - Verify missing firstname returns server error",
            priority = 2
    )
    public void verifyCreateBookingMissingFirstname() {
        Map<String, Object> payload = buildValidBookingPayload();
        payload.remove("firstname");

        Response response = createBooking(payload);

        assertServerError(response, "TC_CB_002 failed");
    }

    @Test(
            description = "TC_CB_003 - Verify missing totalprice returns server error",
            priority = 3
    )
    public void verifyCreateBookingMissingTotalprice() {
        Map<String, Object> payload = buildValidBookingPayload();
        payload.remove("totalprice");

        Response response = createBooking(payload);

        assertServerError(response, "TC_CB_003 failed");
    }

    @Test(
            description = "TC_CB_004 - Verify missing depositpaid returns server error",
            priority = 4
    )
    public void verifyCreateBookingMissingDepositpaid() {
        Map<String, Object> payload = buildValidBookingPayload();
        payload.remove("depositpaid");

        Response response = createBooking(payload);

        assertServerError(response, "TC_CB_004 failed");
    }

    @Test(
            description = "TC_CB_005 - Verify missing bookingdates returns server error",
            priority = 5
    )
    public void verifyCreateBookingMissingBookingdates() {
        Map<String, Object> payload = buildValidBookingPayload();
        payload.remove("bookingdates");

        Response response = createBooking(payload);

        assertServerError(response, "TC_CB_005 failed");
    }

    @Test(
            description = "TC_CB_006 - Verify actual behavior for invalid date format during booking creation",
            priority = 6
    )
    public void verifyCreateBookingWithInvalidDateFormat() {
        Map<String, Object> payload = buildValidBookingPayload();
        Map<String, Object> invalidDates = new LinkedHashMap<>();
        invalidDates.put("checkin", "01-13-2024");
        invalidDates.put("checkout", "01-20-2024");
        payload.put("bookingdates", invalidDates);

        Response response = createBooking(payload);

        int actualStatus = response.getStatusCode();
        String actualBody = response.getBody().asString();

        Assert.assertEquals(
                actualStatus,
                200,
                "TC_CB_006 failed: expected status 200 per test specification."
                        + " Expected behavior: API accepts or at least responds 200 for malformed dates and we document actual behavior."
                        + " Actual status: " + actualStatus
                        + ", actual body: " + actualBody
        );
    }

    @Test(
            description = "TC_CB_007 - Verify create booking response schema",
            priority = 7
    )
    public void verifyCreateBookingResponseSchema() {
        Map<String, Object> payload = buildValidBookingPayload();

        Response response = createBooking(payload);

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "TC_CB_007 failed: expected status code 200 for valid booking creation."
        );

        JsonPath jsonPath = response.jsonPath();
        Assert.assertTrue(
                jsonPath.get("bookingid") instanceof Integer,
                "TC_CB_007 failed: bookingid should be an integer."
        );
        Assert.assertTrue(
                jsonPath.get("booking.totalprice") instanceof Integer,
                "TC_CB_007 failed: booking.totalprice should be a number."
        );
        Assert.assertTrue(
                jsonPath.get("booking.depositpaid") instanceof Boolean,
                "TC_CB_007 failed: booking.depositpaid should be a boolean."
        );

        String checkin = jsonPath.getString("booking.bookingdates.checkin");
        String checkout = jsonPath.getString("booking.bookingdates.checkout");

        Assert.assertTrue(
                checkin.matches("\\d{4}-\\d{2}-\\d{2}"),
                "TC_CB_007 failed: checkin should be in YYYY-MM-DD format."
        );
        Assert.assertTrue(
                checkout.matches("\\d{4}-\\d{2}-\\d{2}"),
                "TC_CB_007 failed: checkout should be in YYYY-MM-DD format."
        );
    }

    public static Integer getCreatedBookingId() {
        return createdBookingId;
    }

    private Response createBooking(Map<String, Object> payload) {
        return given()
                .spec(requestSpec)
                .body(payload)
                .when()
                .post(ApiConfig.BOOKING_ENDPOINT)
                .then()
                .extract()
                .response();
    }

    private Map<String, Object> buildValidBookingPayload() {
        Map<String, Object> bookingDates = new LinkedHashMap<>();
        bookingDates.put("checkin", "2024-01-01");
        bookingDates.put("checkout", "2024-01-10");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("firstname", "Jim");
        payload.put("lastname", "Brown");
        payload.put("totalprice", 111);
        payload.put("depositpaid", true);
        payload.put("bookingdates", bookingDates);
        payload.put("additionalneeds", "Breakfast");
        return payload;
    }

    private void assertBookingMatchesPayload(JsonPath jsonPath, Map<String, Object> payload, String failurePrefix) {
        Assert.assertEquals(jsonPath.getString("booking.firstname"), payload.get("firstname"),
                failurePrefix + ": firstname mismatch.");
        Assert.assertEquals(jsonPath.getString("booking.lastname"), payload.get("lastname"),
                failurePrefix + ": lastname mismatch.");
        Assert.assertEquals(jsonPath.getInt("booking.totalprice"), payload.get("totalprice"),
                failurePrefix + ": totalprice mismatch.");
        Assert.assertEquals(jsonPath.getBoolean("booking.depositpaid"), payload.get("depositpaid"),
                failurePrefix + ": depositpaid mismatch.");
        Assert.assertEquals(jsonPath.getString("booking.bookingdates.checkin"),
                ((Map<?, ?>) payload.get("bookingdates")).get("checkin"),
                failurePrefix + ": checkin mismatch.");
        Assert.assertEquals(jsonPath.getString("booking.bookingdates.checkout"),
                ((Map<?, ?>) payload.get("bookingdates")).get("checkout"),
                failurePrefix + ": checkout mismatch.");
        Assert.assertEquals(jsonPath.getString("booking.additionalneeds"), payload.get("additionalneeds"),
                failurePrefix + ": additionalneeds mismatch.");
    }

    private void assertServerError(Response response, String failurePrefix) {
        Assert.assertEquals(
                response.getStatusCode(),
                500,
                failurePrefix + ": expected status code 500."
        );
    }
}
