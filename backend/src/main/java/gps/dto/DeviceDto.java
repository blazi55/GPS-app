package gps.dto;

import gps.enums.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDto {
	private Long id;
	private String name;
	private DeviceType deviceType;
	private String externalId;
}
