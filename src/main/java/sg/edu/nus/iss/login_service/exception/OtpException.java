package sg.edu.nus.iss.login_service.exception;

import org.springframework.http.HttpStatus;

public class OtpException extends RuntimeException {
    private final HttpStatus httpStatus;

    public OtpException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
