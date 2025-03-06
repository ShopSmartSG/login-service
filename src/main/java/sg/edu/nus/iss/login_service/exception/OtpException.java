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

    public static class UnprocessableEntityException extends RuntimeException {
        public UnprocessableEntityException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class AccountLockedException extends RuntimeException {
        public AccountLockedException(String message) {
            super(message);
        }
    }

    public static class EmailNotFoundException extends RuntimeException {
        public EmailNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidOtpException extends RuntimeException {
        public InvalidOtpException(String message) {
            super(message);
        }
    }

    public static class OldPasswordIncorrectException extends RuntimeException {
        public OldPasswordIncorrectException(String message) {
            super(message);
        }
    }
}
