package gps.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

	@Test
	void shouldCalculateZeroDistance_forSamePoint() {
		assertEquals(0.0, GeoUtils.distanceMeters(50.0, 20.0, 50.0, 20.0), 0.01);
	}

	@Test
	void shouldCalculateApproxDistance_betweenKnownPoints() {
		// Warsaw approx vs Krakow approx ~250 km
		double meters = GeoUtils.distanceMeters(52.2297, 21.0122, 50.0647, 19.9450);
		assertTrue(meters > 240_000 && meters < 270_000);
	}

	@Test
	void shouldValidateCoordinates() {
		assertTrue(GeoUtils.isValidLatitude(0));
		assertTrue(GeoUtils.isValidLatitude(90));
		assertTrue(GeoUtils.isValidLatitude(-90));
		assertFalse(GeoUtils.isValidLatitude(91));
		assertFalse(GeoUtils.isValidLatitude(-91));

		assertTrue(GeoUtils.isValidLongitude(180));
		assertTrue(GeoUtils.isValidLongitude(-180));
		assertFalse(GeoUtils.isValidLongitude(181));
		assertFalse(GeoUtils.isValidLongitude(-181));
	}
}
