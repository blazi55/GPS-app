package gps.support;

import gps.i18n.MessageKeys;
import gps.i18n.MessageService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

public final class MessageServiceTestSupport {

	private MessageServiceTestSupport() {
	}

	public static void stubEnglish(MessageService messages) {
		lenient().when(messages.get(anyString())).thenAnswer(invocation ->
				translate(invocation.getArgument(0))
		);
		lenient().when(messages.get(anyString(), any())).thenAnswer(invocation -> {
			String code = invocation.getArgument(0);
			Object firstArg = invocation.getArgument(1);
			return translate(code, firstArg);
		});
	}

	private static String translate(String code) {
		return switch (code) {
			case MessageKeys.UNEXPECTED -> "Unexpected error";
			case MessageKeys.UNHANDLED -> "Something went wrong";
			case MessageKeys.LOCATION_FROM_AFTER_TO -> "'from' must be before or equal to 'to'";
			case MessageKeys.DEVICE_EXTERNAL_ID_REQUIRED -> "ExternalId is required";
			case MessageKeys.DEVICE_NAME_REQUIRED -> "Device name cannot be empty";
			case MessageKeys.LOCATION_LATITUDE_INVALID -> "Invalid latitude";
			case MessageKeys.LOCATION_LONGITUDE_INVALID -> "Invalid longitude";
			case MessageKeys.LOCATION_DEVICE_EXTERNAL_ID_REQUIRED -> "DeviceExternalId is required";
			default -> code;
		};
	}

	private static String translate(String code, Object arg) {
		return switch (code) {
			case MessageKeys.DEVICE_NOT_FOUND_ID -> "Device not found for id: " + arg;
			case MessageKeys.DEVICE_NOT_FOUND_EXTERNAL_ID -> "Device not found for externalId: " + arg;
			case MessageKeys.LOCATION_NOT_FOUND -> "No location found for device: " + arg;
			case MessageKeys.LOCATION_DEVICE_NOT_FOUND -> "Device not found for externalId: " + arg;
			default -> translate(code);
		};
	}
}
