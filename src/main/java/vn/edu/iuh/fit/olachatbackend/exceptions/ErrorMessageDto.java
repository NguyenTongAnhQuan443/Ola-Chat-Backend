package vn.edu.iuh.fit.olachatbackend.exceptions;

import lombok.Builder;

@Builder
public class ErrorMessageDto {
    private int statusCode;
    private String message;
    private boolean success;

    public ErrorMessageDto(int statusCode, String message, boolean success) {
        this.statusCode = statusCode;
        this.message = message;
        this.success = success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ErrorMessageDto{"
                + "statusCode="
                + statusCode
                + ", message='"
                + message
                + '\''
                + ", success="
                + success
                + '}';
    }
}
