package vn.edu.iuh.fit.olachatbackend.exceptions;

public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String message) {
        super(message);
    }

    public InternalServerErrorException(RuntimeException message) {
        super(message.getMessage());
    }
}
