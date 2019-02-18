package io.appium.uiautomator2.http;

import java.nio.charset.Charset;

public interface IHttpResponse {

    IHttpResponse setStatus(int status);

    IHttpResponse setContentType(String mimeType);

    IHttpResponse setContent(byte[] data);

    IHttpResponse setContent(String message);

    IHttpResponse setEncoding(Charset charset);

    IHttpResponse sendRedirect(String to);

    IHttpResponse sendTemporaryRedirect(String to);

    void end();

    boolean isClosed();
}
