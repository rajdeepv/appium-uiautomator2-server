package io.appium.uiautomator2.server.test.exceptions;

public class NoSuchContextException extends RuntimeException {

    public NoSuchContextException(String detailMessage) {
        super(detailMessage);
    }

    public NoSuchContextException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoSuchContextException(Throwable throwable) {
        super(throwable);
    }
}
