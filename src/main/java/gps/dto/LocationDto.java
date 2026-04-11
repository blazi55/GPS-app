package gps.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class LocationDto {
	private String deviceExternalId;
	private double latitude;
	private double longitude;
	private Instant timestamp;
}
