package vn.edu.iuh.fit.olachatbackend.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@Setter
@NoArgsConstructor
public class MessageResponse<T> implements java.io.Serializable {
    @Builder.Default
    private int statusCode = 200;
    private String message;
    @Builder.Default
    private boolean success = true;
    private T data;

    public MessageResponse(int statusCode, String message, boolean success) {
        this(statusCode, message, success, null);
    }

    public MessageResponse(int statusCode, String message, boolean success, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.success = success;
        this.data = data;
    }
}
