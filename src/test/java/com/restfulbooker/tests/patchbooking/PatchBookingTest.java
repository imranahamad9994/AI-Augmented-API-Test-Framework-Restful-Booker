package com.restfulbooker.tests.patchbooking;

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

public class PatchBookingTest extends BaseApiTest {

    private static final int NON_EXISTENT_BOOKING_ID = 999999;

    @Test(
            description = "TC_PU_001 - Verify partial update of firstname only",
            priority = 1
    )
    public void verifyPartialUpdateFirstnameOnly() {
        int bookingId = createBookingAndReturnId();
        String authToken = generateAuthToken();
        Map<String, Object> originalBooking = getBookingById(bookingId).jsonPath().getMap("$");

        Map<String, Object> patchPayload = new LinkedHashMap<>();
        patchPayload.put("firstname", "James");

        Response patchResponse = patchBooking(bookingId, patchPayload, authToken);

        Assert.assertEquals(
                patchResponse.getStatusCode(),
                200,
                "TC_PU_001 failed: expected status code 200 for firstname patch."
        );
        Assert.assertEquals(
                patchResponse.jsonPath().getString("firstname"),
                "James",
                "TC_PU_001 failed: firstname should be updated."
        );

        Response getResponse = getBookingById(bookingId);
        Assert.assertEquals(getResponse.getStatusCode(), 200,
                "TC_PU_001 failed: GET after PATCH should return 200.");

        Map<String, Object> updatedBooking = getResponse.jsonPath().getMap("$");
        Assert.assertEquals(updatedBooking.get("firstname"), "James",
                "TC_PU_001 failed: firstname should be updated after GET verification.");
        assertFieldUnchanged(updatedBooking, originalBooking, "lastname", "TC_PU_001 failed");
        assertFieldUnchanged(updatedBooking, originalBooking, "totalprice", "TC_PU_001 failed");
        assertFieldUnchanged(updatedBooking, originalBooking, "depositpaid", "TC_PU_001 failed");
        assertFieldUnchanged(updatedBooking, originalBooking, "additionalneeds", "TC_PU_001 failed");
        assertFieldUnchanged(updatedBooking, originalBooking, "bookingdates", "TC_PU_001 failed");
    }

    @Test(
            description = "TC_PU_002 - Verify partial update of totalprice only",
            priority = 2
    )
    public void verifyPartialUpdateTotalpriceOnly() {
        int bookingId = createBookingAndReturnId();
        String authToken = generateAuthToken();
        Map<String, Object> originalBooking = getBookingById(bookingId).jsonPath().getMap("$");

        Map<String, Object> patchPayload = new LinkedHashMap<>();
        patchPayload.put("totalprice", 999);

        Response patchResponse = patchBooking(bookingId, patchPayload, authToken);

        Assert.assertEquals(
                patchResponse.getStatusCode(),
                200,
                "TC_PU_002 failed: expected status code 200 for totalprice patch."
        );
        Assert.assertEquals(
                patchResponse.jsonPath().getInt("totalprice"),
                999,
                "TC_PU_002 failed: totalprice should be updated."
        );

        Response getResponse = getBookingById(bookingId);
        Map<String, Object> updatedBooking = getResponse.jsonPath().getMap("$");

        Assert.assertEquals(updatedBooking.get("totalprice"), 999,
                "TC_PU_002 failed: totalprice should be updated after GET verification.");
        assertFieldUnchanged(updatedBooking, originalBooking, "firstname", "TC_PU_002 failed");
        assertFieldUnchanged(updatedBooking, originalBooking, "lastname", "TC_PU_002 failed");
        assertFieldUnchanged(updatedBooking, originalBooking, "depositpaid", "TC_PU_002 failed");
        assertFieldUnchanged(updatedBooking, originalBooking, "additionalneeds", "TC_PU_002 failed");
        assertFieldUnchanged(updatedBooking, originalBooking, "bookingdates", "TC_PU_002 failed");
    }

    @Test(
            description = "TC_PU_003 - Verify partial update fails without auth token",
            priority = 3
    )
    public void verifyPartialUpdateWithoutAuthToken() {
        int bookingId = createBookingAndReturnId();

        Map<String, Object> patchPayload = new LinkedHashMap<>();
        patchPayload.put("firstname", "James");

        Response patchResponse = given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .body(patchPayload)
                .when()
                .patch(ApiConfig.BOOKING_ENDPOINT + "/{id}")
                .then()
                .extract()
                .response();

        Assert.assertEquals(
                patchResponse.getStatusCode(),
                403,
                "TC_PU_003 failed: expected 403 Forbidden without auth token."
        );
    }

    @Test(
            description = "TC_PU_004 - Verify partial update on non-existent booking ID returns 405",
            priority = 4
    )
    public void verifyPartialUpdateNonExistentBookingId() {
        String authToken = generateAuthToken();

        Map<String, Object> patchPayload = new LinkedHashMap<>();
        patchPayload.put("firstname", "James");

        Response patchResponse = patchBooking(NON_EXISTENT_BOOKING_ID, patchPayload, authToken);

        Assert.assertEquals(
                patchResponse.getStatusCode(),
                405,
                "TC_PU_004 failed: expected 405 Method Not Allowed for non-existent booking ID."
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

        Assert.assertEquals(response.getStatusCode(), 200,
                "Unable to generate auth token for patch booking tests.");

        String token = response.jsonPath().getString("token");
        Assert.assertNotNull(token, "Unable to generate auth token for patch booking tests.");
        Assert.assertFalse(token.trim().isEmpty(), "Unable to generate auth token for patch booking tests.");
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

        Assert.assertEquals(response.getStatusCode(), 200,
                "Unable to create booking for patch booking tests.");
        int bookingId = response.jsonPath().getInt("bookingid");
        Assert.assertTrue(bookingId > 0, "Unable to create booking for patch booking tests.");
        return bookingId;
    }

    private Response patchBooking(int bookingId, Map<String, Object> payload, String token) {
        return given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .header("Cookie", "token=" + token)
                .body(payload)
                .when()
                .patch(ApiConfig.BOOKING_ENDPOINT + "/{id}")
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

    private void assertFieldUnchanged(
            Map<String, Object> updatedBooking,
            Map<String, Object> originalBooking,
            String fieldName,
            String failurePrefix
    ) {
        Assert.assertEquals(
                updatedBooking.get(fieldName),
                originalBooking.get(fieldName),
                failurePrefix + ": field '" + fieldName + "' should remain unchanged."
        );
    }
}
