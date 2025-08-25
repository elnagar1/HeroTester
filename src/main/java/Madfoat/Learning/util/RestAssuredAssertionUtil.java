package Madfoat.Learning.util;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.lessThan;

/**
 * Lightweight helpers to add assertions on Rest Assured responses in a concise, reusable way.
 *
 * Usage examples:
 *
 * Response resp = given().get(url);
 * RestAssuredAssertionUtil.assertStatus(resp, 200)
 *     .body("data.id", equalTo(123));
 *
 * // or using JSONPath helper
 * RestAssuredAssertionUtil.assertJsonPathEquals(resp, "data.id", 123);
 */
public final class RestAssuredAssertionUtil {

    private RestAssuredAssertionUtil() { }

    public static ValidatableResponse assertStatus(Response response, int expectedStatus) {
        return response.then().statusCode(expectedStatus);
    }

    public static ValidatableResponse assertContains(Response response, String expectedSubstring) {
        return response.then().body(containsString(expectedSubstring));
    }

    public static ValidatableResponse assertHeaderEquals(Response response, String headerName, String expectedValue) {
        return response.then().header(headerName, expectedValue);
    }

    public static ValidatableResponse assertJsonPathEquals(Response response, String jsonPath, Object expectedValue) {
        return response.then().body(jsonPath, equalTo(expectedValue));
    }

    public static ValidatableResponse assertContentType(Response response, String expectedContentType) {
        return response.then().contentType(expectedContentType);
    }

    public static ValidatableResponse assertTimeLessThan(Response response, long maxMillis) {
        return response.then().time(lessThan(maxMillis));
    }

    public static ValidatableResponse assertJsonPathExists(Response response, String jsonPath) {
        return response.then().body(jsonPath, notNullValue());
    }

    // Overloads that continue a chain on an existing ValidatableResponse
    public static ValidatableResponse assertContains(ValidatableResponse thenStage, String expectedSubstring) {
        return thenStage.body(containsString(expectedSubstring));
    }

    public static ValidatableResponse assertJsonPathEquals(ValidatableResponse thenStage, String jsonPath, Object expectedValue) {
        return thenStage.body(jsonPath, equalTo(expectedValue));
    }

    public static ValidatableResponse assertContentType(ValidatableResponse thenStage, String expectedContentType) {
        return thenStage.contentType(expectedContentType);
    }

    public static ValidatableResponse assertTimeLessThan(ValidatableResponse thenStage, long maxMillis) {
        return thenStage.time(lessThan(maxMillis));
    }

    public static ValidatableResponse assertJsonPathExists(ValidatableResponse thenStage, String jsonPath) {
        return thenStage.body(jsonPath, notNullValue());
    }
}

