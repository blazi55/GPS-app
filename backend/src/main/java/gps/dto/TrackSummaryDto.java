package gps.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackSummaryDto {
	private String deviceExternalId;
	private int pointCount;
	private double totalDistanceMeters;
	private Instant from;
	private Instant to;
	private List<LocationDto> points;
}
