package gps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
	private String deviceExternalId;
	private double latitude;
	private double longitude;
	private Instant timestamp;
}
