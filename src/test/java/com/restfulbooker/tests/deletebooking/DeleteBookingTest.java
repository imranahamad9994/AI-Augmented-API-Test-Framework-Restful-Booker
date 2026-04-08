package com.restfulbooker.tests.deletebooking;

import com.restfulbooker.base.ApiConfig;
import com.restfulbooker.base.BaseApiTest;
import com.restfulbooker.models.AuthRequest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class DeleteBookingTest extends BaseApiTest {

    private static final int NON_EXISTENT_BOOKING_ID = 999999;

    @Test(
            description = "TC_DEL_001 - Verify booking is deleted with valid auth token",
            priority = 1
    )
    public void verifyDeleteBookingWithValidAuthToken() {
        int bookingId = createBookingAndReturnId();
        String authToken = generateAuthToken();

        Response deleteResponse = deleteBooking(bookingId, authToken);

        Assert.assertEquals(
                deleteResponse.getStatusCode(),
                201,
                "TC_DEL_001 failed: expected status code 201 for successful delete."
                        + " Document actual vs expected if the API returns something else."
        );
        Assert.assertEquals(
                deleteResponse.getBody().asString().trim(),
                "Created",
                "TC_DEL_001 failed: expected response body 'Created' for successful delete."
        );

        Response getResponse = getBookingById(bookingId);
        Assert.assertEquals(
                getResponse.getStatusCode(),
                404,
                "TC_DEL_001 failed: deleted booking should return 404 on GET verification."
        );
    }

    @Test(
            description = "TC_DEL_002 - Verify delete fails without auth token",
            priority = 2
    )
    public void verifyDeleteBookingWithoutAuthToken() {
        int bookingId = createBookingAndReturnId();

        Response deleteResponse = given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .when()
                .delete(ApiConfig.BOOKING_ENDPOINT + "/{id}")
                .then()
                .extract()
                .response();

        Assert.assertEquals(
                deleteResponse.getStatusCode(),
                403,
                "TC_DEL_002 failed: expected 403 Forbidden without auth token."
        );

        Response getResponse = getBookingById(bookingId);
        Assert.assertEquals(
                getResponse.getStatusCode(),
                200,
                "TC_DEL_002 failed: booking should still exist after unauthorized delete attempt."
        );
    }

    @Test(
            description = "TC_DEL_003 - Verify delete fails with invalid auth token",
            priority = 3
    )
    public void verifyDeleteBookingWithInvalidAuthToken() {
        int bookingId = createBookingAndReturnId();

        Response deleteResponse = deleteBooking(bookingId, "invalid-token");

        Assert.assertEquals(
                deleteResponse.getStatusCode(),
                403,
                "TC_DEL_003 failed: expected 403 Forbidden with invalid auth token."
        );
    }

    @Test(
            description = "TC_DEL_004 - Verify delete on non-existent booking ID returns 405",
            priority = 4
    )
    public void verifyDeleteNonExistentBookingId() {
        String authToken = generateAuthToken();

        Response deleteResponse = deleteBooking(NON_EXISTENT_BOOKING_ID, authToken);

        Assert.assertEquals(
                deleteResponse.getStatusCode(),
                405,
                "TC_DEL_004 failed: expected 405 Method Not Allowed for non-existent booking ID."
        );
    }

    @Test(
            description = "TC_DEL_005 - Verify deleted booking returns 404 on GET",
            priority = 5
    )
    public void verifyDeletedBookingReturnsNotFoundOnGet() {
        int bookingId = createBookingAndReturnId();
        String authToken = generateAuthToken();

        Response deleteResponse = deleteBooking(bookingId, authToken);
        Assert.assertEquals(
                deleteResponse.getStatusCode(),
                201,
                "TC_DEL_005 failed: delete prerequisite should return 201."
        );

        Response getResponse = getBookingById(bookingId);
        Assert.assertEquals(
                getResponse.getStatusCode(),
                404,
                "TC_DEL_005 failed: GET should return 404 after successful delete."
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
                "Unable to generate auth token for delete booking tests."
        );

        String token = response.jsonPath().getString("token");
        Assert.assertNotNull(token, "Unable to generate auth token for delete booking tests.");
        Assert.assertFalse(token.trim().isEmpty(), "Unable to generate auth token for delete booking tests.");
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
                "Unable to create booking for delete booking tests."
        );

        int bookingId = response.jsonPath().getInt("bookingid");
        Assert.assertTrue(bookingId > 0, "Unable to create booking for delete booking tests.");
        return bookingId;
    }

    private Response deleteBooking(int bookingId, String token) {
        return given()
                .spec(requestSpec)
                .pathParam("id", bookingId)
                .header("Cookie", "token=" + token)
                .when()
                .delete(ApiConfig.BOOKING_ENDPOINT + "/{id}")
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
}
