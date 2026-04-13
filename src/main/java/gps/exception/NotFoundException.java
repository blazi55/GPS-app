package gps.exception;

public class NotFoundException extends RuntimeException {
	public NotFoundException(String externalId) {
		super("No object found for device: " + externalId);
	}
}
