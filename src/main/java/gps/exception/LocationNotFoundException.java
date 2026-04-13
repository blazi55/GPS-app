package gps.exception;

public class LocationNotFoundException extends RuntimeException {
	public LocationNotFoundException(String externalId) {
		super("No location found for device: " + externalId);
	}
}
