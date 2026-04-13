package gps.exception;


import gps.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

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
						"Unexpected error"
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
						"Something went wrong"
				));
	}

	@ExceptionHandler(LocationNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(LocationNotFoundException ex) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(new ErrorResponse(
						Instant.now(),
						HttpStatus.NOT_FOUND.value(),
						ex.getMessage()
				));
	}
}