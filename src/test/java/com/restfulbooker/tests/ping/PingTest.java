package com.restfulbooker.tests.ping;

import com.restfulbooker.base.ApiConfig;
import com.restfulbooker.base.BaseApiTest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class PingTest extends BaseApiTest {

    @Test(
            description = "TC_PING_001 - Verify server is reachable through ping endpoint",
            priority = 1
    )
    public void verifyServerIsUpUsingPingEndpoint() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(ApiConfig.PING_ENDPOINT)
                .then()
                .extract()
                .response();

        Assert.assertEquals(
                response.getStatusCode(),
                201,
                "TC_PING_001 failed: expected status code 201 from the ping endpoint."
        );
        Assert.assertEquals(
                response.getBody().asString().trim(),
                "Created",
                "TC_PING_001 failed: expected response body to be 'Created'."
        );
    }
}
