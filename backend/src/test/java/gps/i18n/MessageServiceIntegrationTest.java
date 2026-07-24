package gps.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class MessageServiceIntegrationTest {

	@Autowired
	private MessageService messageService;

	@AfterEach
	void clearLocale() {
		LocaleContextHolder.resetLocaleContext();
	}

	@Test
	void shouldResolveEnglishByDefault() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		assertEquals(
				"Device not found for id: 5",
				messageService.get(MessageKeys.DEVICE_NOT_FOUND_ID, 5L)
		);
	}

	@Test
	void shouldResolvePolishMessages() {
		LocaleContextHolder.setLocale(Locale.forLanguageTag("pl"));
		assertEquals(
				"Nie znaleziono urządzenia o id: 5",
				messageService.get(MessageKeys.DEVICE_NOT_FOUND_ID, 5L)
		);
		assertEquals(
				"Nieprawidłowa szerokość geograficzna",
				messageService.get(MessageKeys.LOCATION_LATITUDE_INVALID)
		);
	}
}
