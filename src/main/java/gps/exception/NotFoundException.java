package gps.exception;

public class NotFoundException extends RuntimeException {
	public NotFoundException(String externalId) {
		super("No location found for device: " + externalId);
	}
}
