package com.restfulbooker.tests.updatebooking;

import com.restfulbooker.base.ApiConfig;
import com.restfulbooker.base.BaseApiTest;
import com.restfulbooker.models.AuthRequest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class UpdateBookingTest extends BaseApiTest {

    private static final int NON_EXISTENT_BOOKING_ID = 999999;
    private static final String INVALID_TOKEN = "invalid-token";

    @Test(
            description = "TC_UB_001 - Verify full update succeeds with valid auth token",
            priority = 1
    )
    public void verifyFullUpdateWithValidAuthToken() {
        int bookingId = createBookingAndReturnId();
        String authToken = generateAuthToken();
        Map<String, Object> updatePayload = buildUpdatePayload();

        Response updateResponse = updateBooking(bookingId, updatePayload, authToken);

        Assert.assertEquals(
                updateResponse.getStatusCode(),
                200,
                "TC_UB_001 failed: expected status code 200 for full update with valid auth token."
        );
        assertBookingPayload(updateResponse.jsonPath(), updatePayload, "TC_UB_001 failed");

        Response getResponse = getBookingById(bookingId);
        Assert.assertEquals(
                getResponse.getStatusCode(),
                200,
                "TC_UB_001 failed: GET after update should return 200."
        );
        assertBookingPayload(getResponse.jsonPath(), updatePayload, "TC_UB_001 failed during GET verification");
    }

    @Test(
            description = "TC_UB_002 - Verify full update fails without auth token",
            priority = 2
    )
    public void verifyFullUpdateWithoutAuthToken() {
        int bookingId = createBookingAndReturnId();
        Map<String, Object> updatePayload = buildUpdatePayload();
        Map<String, Object> originalBooking = getBookingById(bookingId).jsonPath().getMap("$");

        Response updateResponse = given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .body(updatePayload)
                .when()
                .put(ApiConfig.BOOKING_ENDPOINT + "/{id}")
                .then()
                .extract()
                .response();

        Assert.assertEquals(
                updateResponse.getStatusCode(),
                403,
                "TC_UB_002 failed: expected 403 Forbidden without auth token."
        );

        Response getResponse = getBookingById(bookingId);
        assertBookingUnchanged(getResponse.jsonPath().getMap("$"), originalBooking, "TC_UB_002 failed");
    }

    @Test(
            description = "TC_UB_003 - Verify full update fails with invalid token",
            priority = 3
    )
    public void verifyFullUpdateWithInvalidToken() {
        int bookingId = createBookingAndReturnId();
        Map<String, Object> updatePayload = buildUpdatePayload();
        Map<String, Object> originalBooking = getBookingById(bookingId).jsonPath().getMap("$");

        Response updateResponse = updateBooking(bookingId, updatePayload, INVALID_TOKEN);

        Assert.assertEquals(
                updateResponse.getStatusCode(),
                403,
                "TC_UB_003 failed: expected 403 Forbidden with invalid token."
        );

        Response getResponse = getBookingById(bookingId);
        assertBookingUnchanged(getResponse.jsonPath().getMap("$"), originalBooking, "TC_UB_003 failed");
    }

    @Test(
            description = "TC_UB_004 - Verify update on non-existent booking ID returns 405",
            priority = 4
    )
    public void verifyUpdateNonExistentBookingId() {
        String authToken = generateAuthToken();
        Map<String, Object> updatePayload = buildUpdatePayload();

        Response updateResponse = updateBooking(NON_EXISTENT_BOOKING_ID, updatePayload, authToken);

        Assert.assertEquals(
                updateResponse.getStatusCode(),
                405,
                "TC_UB_004 failed: expected 405 Method Not Allowed for non-existent booking ID."
        );
    }

    @Test(
            description = "TC_UB_005 - Verify update with missing required fields returns 400",
            priority = 5
    )
    public void verifyUpdateWithMissingRequiredFields() {
        int bookingId = createBookingAndReturnId();
        String authToken = generateAuthToken();

        Map<String, Object> partialPayload = new LinkedHashMap<>();
        partialPayload.put("firstname", "James");

        Response updateResponse = updateBooking(bookingId, partialPayload, authToken);

        Assert.assertEquals(
                updateResponse.getStatusCode(),
                400,
                "TC_UB_005 failed: expected 400 Bad Request for partial PUT payload."
        );
    }

    private String generateAuthToken() {
        AuthRequest authRequest = new AuthRequest(ApiConfig.VALID_USERNAME, ApiConfig.VALID_PASSWORD);

        Response response = given()
                .spec(requestSpec)
                .body(authRequest)
                .when()
                .post(ApiConfig.AUTH_ENDPOINT)
                .then()
                .extract()
                .response();

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "Unable to generate auth token for update booking tests."
        );

        String token = response.jsonPath().getString("token");
        Assert.assertNotNull(token, "Unable to generate auth token for update booking tests.");
        Assert.assertFalse(token.trim().isEmpty(), "Unable to generate auth token for update booking tests.");
        return token;
    }

    private int createBookingAndReturnId() {
        Response response = given()
                .spec(requestSpec)
                .body(buildCreatePayload())
                .when()
                .post(ApiConfig.BOOKING_ENDPOINT)
                .then()
                .extract()
                .response();

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "Unable to create booking for update booking tests."
        );

        int bookingId = response.jsonPath().getInt("bookingid");
        Assert.assertTrue(bookingId > 0, "Unable to create booking for update booking tests.");
        return bookingId;
    }

    private Response updateBooking(int bookingId, Map<String, Object> payload, String token) {
        return given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .header("Cookie", "token=" + token)
                .body(payload)
                .when()
                .put(ApiConfig.BOOKING_ENDPOINT + "/{id}")
                .then()
                .extract()
                .response();
    }

    private Response getBookingById(int bookingId) {
        return given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .when()
                .get(ApiConfig.BOOKING_ENDPOINT + "/{id}")
                .then()
                .extract()
                .response();
    }

    private Map<String, Object> buildCreatePayload() {
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

    private Map<String, Object> buildUpdatePayload() {
        Map<String, Object> bookingDates = new LinkedHashMap<>();
        bookingDates.put("checkin", "2024-02-01");
        bookingDates.put("checkout", "2024-02-10");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("firstname", "James");
        payload.put("lastname", "Brown");
        payload.put("totalprice", 200);
        payload.put("depositpaid", false);
        payload.put("bookingdates", bookingDates);
        payload.put("additionalneeds", "Lunch");
        return payload;
    }

    private void assertBookingPayload(JsonPath jsonPath, Map<String, Object> expectedPayload, String failurePrefix) {
        Assert.assertEquals(jsonPath.getString("firstname"), expectedPayload.get("firstname"),
                failurePrefix + ": firstname mismatch.");
        Assert.assertEquals(jsonPath.getString("lastname"), expectedPayload.get("lastname"),
                failurePrefix + ": lastname mismatch.");
        Assert.assertEquals(jsonPath.getInt("totalprice"), expectedPayload.get("totalprice"),
                failurePrefix + ": totalprice mismatch.");
        Assert.assertEquals(jsonPath.getBoolean("depositpaid"), expectedPayload.get("depositpaid"),
                failurePrefix + ": depositpaid mismatch.");
        Assert.assertEquals(jsonPath.getString("bookingdates.checkin"),
                ((Map<?, ?>) expectedPayload.get("bookingdates")).get("checkin"),
                failurePrefix + ": checkin mismatch.");
        Assert.assertEquals(jsonPath.getString("bookingdates.checkout"),
                ((Map<?, ?>) expectedPayload.get("bookingdates")).get("checkout"),
                failurePrefix + ": checkout mismatch.");
        Assert.assertEquals(jsonPath.getString("additionalneeds"), expectedPayload.get("additionalneeds"),
                failurePrefix + ": additionalneeds mismatch.");
    }

    private void assertBookingUnchanged(Map<String, Object> actualBooking, Map<String, Object> originalBooking, String failurePrefix) {
        Assert.assertEquals(actualBooking.get("firstname"), originalBooking.get("firstname"),
                failurePrefix + ": firstname should remain unchanged.");
        Assert.assertEquals(actualBooking.get("lastname"), originalBooking.get("lastname"),
                failurePrefix + ": lastname should remain unchanged.");
        Assert.assertEquals(actualBooking.get("totalprice"), originalBooking.get("totalprice"),
                failurePrefix + ": totalprice should remain unchanged.");
        Assert.assertEquals(actualBooking.get("depositpaid"), originalBooking.get("depositpaid"),
                failurePrefix + ": depositpaid should remain unchanged.");
        Assert.assertEquals(actualBooking.get("additionalneeds"), originalBooking.get("additionalneeds"),
                failurePrefix + ": additionalneeds should remain unchanged.");
        Assert.assertEquals(actualBooking.get("bookingdates"), originalBooking.get("bookingdates"),
                failurePrefix + ": bookingdates should remain unchanged.");
    }
}
