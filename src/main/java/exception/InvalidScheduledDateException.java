package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * SCRUM-386 – Se lanza cuando la fecha programada no está
 * al menos 5 minutos en el futuro.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidScheduledDateException extends RuntimeException {
    public InvalidScheduledDateException(String message) {
        super(message);
    }
}