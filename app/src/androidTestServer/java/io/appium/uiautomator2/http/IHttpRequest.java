package io.appium.uiautomator2.http;

import java.util.Map;

public interface IHttpRequest {
    /**
     * Returns "GET", "POST", "PUT" or "DELETE".
     */
    String method();

    /**
     * Returns the request URI.
     */
    String uri();

    /**
     * Returns the full request body.
     */
    String body();

    /**
     * Gets the value of a given header.
     */
    String header(String name);

    /**
     * Returns additional data appended to the request.
     */
    Map<String, Object> data();
}
