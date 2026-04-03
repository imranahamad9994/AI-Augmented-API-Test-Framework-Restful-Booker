package com.restfulbooker.tests.auth;

import com.restfulbooker.base.ApiConfig;
import com.restfulbooker.base.BaseApiTest;
import com.restfulbooker.models.AuthRequest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class AuthTest extends BaseApiTest {

    private static String authToken;

    @Test(
            description = "TC_AUTH_001 - Verify auth token is generated for valid credentials",
            priority = 1
    )
    public void verifyTokenGeneratedWithValidCredentials() {
        AuthRequest authRequest = new AuthRequest(
                ApiConfig.VALID_USERNAME,
                ApiConfig.VALID_PASSWORD
        );

        Response response = given()
                .spec(requestSpec)
                .body(authRequest)
                .when()
                .post(ApiConfig.AUTH_ENDPOINT)
                .then()
                .extract()
                .response();

        authToken = response.jsonPath().getString("token");
        long actualResponseTime = response.time();

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "TC_AUTH_001 failed: expected status code 200 for valid credentials."
        );
        Assert.assertNotNull(
                authToken,
                "TC_AUTH_001 failed: response body should contain the 'token' field."
        );
        Assert.assertFalse(
                authToken.trim().isEmpty(),
                "TC_AUTH_001 failed: token should be a non-empty string."
        );
        Assert.assertTrue(
                actualResponseTime < ApiConfig.MAX_RESPONSE_TIME_MS,
                "TC_AUTH_001 failed: expected response time under "
                        + ApiConfig.MAX_RESPONSE_TIME_MS
                        + " ms, but was "
                        + actualResponseTime
                        + " ms."
        );
    }

    @Test(
            description = "TC_AUTH_002 - Verify bad credentials response for invalid username",
            priority = 2
    )
    public void verifyBadCredentialsForInvalidUsername() {
        AuthRequest authRequest = new AuthRequest(
                ApiConfig.INVALID_USERNAME,
                ApiConfig.VALID_PASSWORD
        );

        Response response = sendAuthRequest(authRequest);

        assertBadCredentialsResponse(
                response,
                "TC_AUTH_002 failed: expected bad credentials response for invalid username."
        );
    }

    @Test(
            description = "TC_AUTH_003 - Verify bad credentials response for invalid password",
            priority = 3
    )
    public void verifyBadCredentialsForInvalidPassword() {
        AuthRequest authRequest = new AuthRequest(
                ApiConfig.VALID_USERNAME,
                ApiConfig.INVALID_PASSWORD
        );

        Response response = sendAuthRequest(authRequest);

        assertBadCredentialsResponse(
                response,
                "TC_AUTH_003 failed: expected bad credentials response for invalid password."
        );
    }

    @Test(
            description = "TC_AUTH_004 - Verify bad credentials response for empty username and password",
            priority = 4
    )
    public void verifyBadCredentialsForEmptyUsernameAndPassword() {
        AuthRequest authRequest = new AuthRequest("", "");

        Response response = sendAuthRequest(authRequest);

        assertBadCredentialsResponse(
                response,
                "TC_AUTH_004 failed: expected bad credentials response for empty username and password."
        );
    }

    @Test(
            description = "TC_AUTH_005 - Verify error response when request body is missing",
            priority = 5
    )
    public void verifyErrorResponseForMissingRequestBody() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .post(ApiConfig.AUTH_ENDPOINT)
                .then()
                .extract()
                .response();

        Assert.assertEquals(
                response.getStatusCode(),
                200,
                "TC_AUTH_005 failed: expected status code 200 when request body is missing."
        );

        String token = response.jsonPath().getString("token");
        String reason = response.jsonPath().getString("reason");

        Assert.assertTrue(
                token == null || token.trim().isEmpty(),
                "TC_AUTH_005 failed: token should not be generated when request body is missing."
        );
        Assert.assertNotNull(
                reason,
                "TC_AUTH_005 failed: response should contain an error reason when request body is missing."
        );
        Assert.assertFalse(
                reason.trim().isEmpty(),
                "TC_AUTH_005 failed: error reason should not be empty."
        );
    }

    public static String getAuthToken() {
        return authToken;
    }

    private Response sendAuthRequest(AuthRequest authRequest) {
        return given()
                .spec(requestSpec)
                .body(authRequest)
                .when()
                .post(ApiConfig.AUTH_ENDPOINT)
                .then()
                .extract()
                .response();
    }

    private void assertBadCredentialsResponse(Response response, String failureMessagePrefix) {
        Assert.assertEquals(
                response.getStatusCode(),
                200,
                failureMessagePrefix + " Expected status code 200."
        );

        String token = response.jsonPath().getString("token");
        String reason = response.jsonPath().getString("reason");

        Assert.assertTrue(
                token == null || token.trim().isEmpty(),
                failureMessagePrefix + " Token should not be present."
        );
        Assert.assertEquals(
                reason,
                ApiConfig.BAD_CREDENTIALS_REASON,
                failureMessagePrefix + " Reason should match expected error message."
        );
    }
}
