
package exception;
import java.util.Map;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 400: Validación de campos (@Valid) ──────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status",    400,
                    "error",     "Bad Request",
                    "errors",    fieldErrors,
                    "timestamp", OffsetDateTime.now().toString()
                ));
    }

    // ── 404: Recurso no encontrado ──────────────────────────────────────
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "status",    404,
                    "error",     "Not Found",
                    "message",   ex.getMessage(),
                    "timestamp", OffsetDateTime.now().toString()
                ));
    }

    // ── 403: Sin permiso ────────────────────────────────────────────────
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                    "status",    403,
                    "error",     "Forbidden",
                    "message",   ex.getMessage(),
                    "timestamp", OffsetDateTime.now().toString()
                ));
    }

    // ── 400: Cualquier otro error de negocio ────────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "status",    400,
                    "error",     "Bad Request",
                    "message",   ex.getMessage(),
                    "timestamp", OffsetDateTime.now().toString()
                ));
    }
}