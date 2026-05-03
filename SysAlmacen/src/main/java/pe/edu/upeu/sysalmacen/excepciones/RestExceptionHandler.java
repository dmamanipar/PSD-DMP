package pe.edu.upeu.sysalmacen.excepciones;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    // ─── 404: ID no encontrado en base de datos ────────────────────────────────
    @ExceptionHandler(ModelNotFoundException.class)
    public ResponseEntity<CustomResponse> handleModelNotFoundException(
            ModelNotFoundException ex, WebRequest request) {
        CustomResponse err = new CustomResponse(
                HttpStatus.NOT_FOUND.value(), LocalDateTime.now(),
                ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    // ─── 400: Error aritmético ─────────────────────────────────────────────────
    @ExceptionHandler(ArithmeticException.class)
    public ResponseEntity<CustomResponse> handleArithmeticException(
            ArithmeticException ex, WebRequest request) {
        CustomResponse err = new CustomResponse(
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(),
                ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // ─── 400: Parámetro de ruta con tipo incorrecto (ej: /categorias/54A) ──────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String expectedType = (ex.getRequiredType() != null)
                ? ex.getRequiredType().getSimpleName()
                : "desconocido";
        String message = String.format(
                "El parámetro '%s' recibió el valor '%s', que no es un tipo válido. Se esperaba: %s",
                ex.getName(), ex.getValue(), expectedType);
        CustomResponse err = new CustomResponse(
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(),
                message, request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // ─── 400: @Valid falla en @RequestBody ─────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ":" + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        CustomResponse err = new CustomResponse(
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(),
                msg, request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // ─── 400: JSON malformado en el body ───────────────────────────────────────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        CustomResponse err = new CustomResponse(
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(),
                "El cuerpo de la solicitud es inválido o no puede ser leído. Verifique el formato JSON.",
                request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // ─── 405: Método HTTP incorrecto (ej: GET en lugar de POST) ───────────────
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String allowed = (ex.getSupportedHttpMethods() != null)
                ? ex.getSupportedHttpMethods().toString()
                : "N/A";
        String message = String.format(
                "El método HTTP '%s' no está soportado. Métodos permitidos: %s",
                ex.getMethod(), allowed);
        CustomResponse err = new CustomResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(), LocalDateTime.now(),
                message, request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.METHOD_NOT_ALLOWED);
    }

    // ─── 415: Content-Type no soportado ───────────────────────────────────────
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CustomResponse> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {
        String message = String.format(
                "El tipo de contenido '%s' no está soportado. Use: application/json",
                ex.getContentType());
        CustomResponse err = new CustomResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), LocalDateTime.now(),
                message, request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // ─── 400: Query param requerido ausente ────────────────────────────────────
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {
        String message = String.format(
                "El parámetro requerido '%s' de tipo '%s' está ausente.",
                ex.getParameterName(), ex.getParameterType());
        CustomResponse err = new CustomResponse(
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now(),
                message, request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    // ─── 404: Endpoint no existe ───────────────────────────────────────────────
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<CustomResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        String message = String.format(
                "No se encontró ningún endpoint para %s %s",
                ex.getHttpMethod(), ex.getRequestURL());
        CustomResponse err = new CustomResponse(
                HttpStatus.NOT_FOUND.value(), LocalDateTime.now(),
                message, request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.NOT_FOUND);
    }

    // ─── 500: Fallback general ─────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse> handleAllExceptions(
            Exception ex, WebRequest request) {
        CustomResponse err = new CustomResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now(),
                ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}