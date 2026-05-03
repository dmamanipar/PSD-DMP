package pe.edu.upeu.sysalmacen.excepciones;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomResponse {
    private int statusCode;
    private LocalDateTime datetime;
    private String message;
    private String details;

    public static CustomResponse created(String message, String uri) {
        return new CustomResponse(201, LocalDateTime.now(), message, "uri=" + uri);
    }

    public static CustomResponse ok(String message, String uri) {
        return new CustomResponse(200, LocalDateTime.now(), message, "uri=" + uri);
    }

    public static CustomResponse noContent(String message, String uri) {
        return new CustomResponse(204, LocalDateTime.now(), message, "uri=" + uri);
    }

}
