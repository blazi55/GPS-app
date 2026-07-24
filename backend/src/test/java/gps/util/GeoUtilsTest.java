package gps.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

	@Test
	void shouldCalculateZeroDistance_forSamePoint() {
		assertEquals(0.0, GeoUtils.distanceMeters(50.0, 20.0, 50.0, 20.0), 0.01);
	}

	@Test
	void shouldCalculateApproxDistance_betweenKnownPoints() {
		double meters = GeoUtils.distanceMeters(52.2297, 21.0122, 50.0647, 19.9450);
		assertTrue(meters > 240_000 && meters < 270_000);
	}

	@Test
	void shouldBeSymmetric() {
		double ab = GeoUtils.distanceMeters(52.0, 21.0, 50.0, 19.0);
		double ba = GeoUtils.distanceMeters(50.0, 19.0, 52.0, 21.0);
		assertEquals(ab, ba, 0.01);
	}

	@ParameterizedTest
	@ValueSource(doubles = {-90, -45, 0, 45, 90})
	void shouldAcceptValidLatitude(double latitude) {
		assertTrue(GeoUtils.isValidLatitude(latitude));
	}

	@ParameterizedTest
	@ValueSource(doubles = {-91, 91, 180})
	void shouldRejectInvalidLatitude(double latitude) {
		assertFalse(GeoUtils.isValidLatitude(latitude));
	}

	@ParameterizedTest
	@ValueSource(doubles = {-180, -90, 0, 90, 180})
	void shouldAcceptValidLongitude(double longitude) {
		assertTrue(GeoUtils.isValidLongitude(longitude));
	}

	@ParameterizedTest
	@ValueSource(doubles = {-181, 181, 200})
	void shouldRejectInvalidLongitude(double longitude) {
		assertFalse(GeoUtils.isValidLongitude(longitude));
	}

	@ParameterizedTest
	@CsvSource({
			"0, 0, 0, 1, 111000, 112000",
			"0, 0, 1, 0, 111000, 112000"
	})
	void shouldEstimateOneDegreeNearEquator(
			double lat1, double lon1, double lat2, double lon2,
			double minMeters, double maxMeters
	) {
		double meters = GeoUtils.distanceMeters(lat1, lon1, lat2, lon2);
		assertTrue(meters > minMeters && meters < maxMeters);
	}
}
