package com.projectgoth;

/**
 * Captcha not found exception
 * Created by freddie on 15/5/15.
 */
public class CaptchaNotFoundException extends Exception {

    public CaptchaNotFoundException() {
        super();
    }

    public CaptchaNotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public CaptchaNotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CaptchaNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
