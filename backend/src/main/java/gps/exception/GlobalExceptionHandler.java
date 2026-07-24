package gps.exception;

import gps.dto.ErrorResponse;
import gps.i18n.MessageKeys;
import gps.i18n.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final MessageService messages;

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse(
						Instant.now(),
						HttpStatus.NOT_FOUND.value(),
						ex.getMessage()
				));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(
						Instant.now(),
						HttpStatus.BAD_REQUEST.value(),
						ex.getMessage()
				));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleState(IllegalStateException ex) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(new ErrorResponse(
						Instant.now(),
						HttpStatus.CONFLICT.value(),
						ex.getMessage()
				));
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ErrorResponse> handleGeneric(RuntimeException ex) {
		log.error("Unexpected error", ex);

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
						Instant.now(),
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						messages.get(MessageKeys.UNEXPECTED)
				));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
		log.error("Unhandled exception", ex);

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
						Instant.now(),
						HttpStatus.INTERNAL_SERVER_ERROR.value(),
						messages.get(MessageKeys.UNHANDLED)
				));
	}
}
