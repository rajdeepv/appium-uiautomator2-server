package io.appium.uiautomator2.http.impl;

import java.nio.charset.Charset;

import io.appium.uiautomator2.http.IHttpResponse;
import io.appium.uiautomator2.server.HttpStatusCode;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

public class NettyHttpResponse implements IHttpResponse {

    private final FullHttpResponse response;
    private final String CONTENT_TYPE = "Content-Type";
    private final String CONTENT_ENCODING = "Content-Encoding";
    private final String CONTENT_LENGTH = "Content-Length";
    private final String LOCATION = "location";
    private boolean closed;
    private Charset charset = CharsetUtil.UTF_8;


    public NettyHttpResponse(FullHttpResponse response) {
        this.response = response;
        response.headers().add(CONTENT_ENCODING, "identity");
    }

    public IHttpResponse setStatus(int status) {
        response.setStatus(HttpResponseStatus.valueOf(status));
        return this;
    }

    public IHttpResponse setContentType(String mimeType) {
        response.headers().add(CONTENT_TYPE, mimeType);
        return this;
    }

    public IHttpResponse setContent(byte[] data) {
        response.headers().add(CONTENT_LENGTH, data.length);
        response.content().writeBytes(data);
        return this;
    }

    public IHttpResponse setContent(String message) {
        setContent(message.getBytes(charset));
        return this;
    }

    public IHttpResponse sendRedirect(String to) {
        setStatus(HttpStatusCode.MOVED_PERMANENTLY.getStatusCode());
        response.headers().add(LOCATION, to);
        return this;
    }

    public IHttpResponse sendTemporaryRedirect(String to) {
        setStatus(HttpStatusCode.FOUND.getStatusCode());
        response.headers().add(LOCATION, to);
        return this;
    }

    @Override
    public void end() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public IHttpResponse setEncoding(Charset charset) {
        this.charset = charset;
        return this;
    }
}
