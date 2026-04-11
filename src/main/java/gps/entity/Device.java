package gps.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Device {

	@Id
	@GeneratedValue
	private Long id;

	private String name;

	@Column(unique = true)
	private String externalId;

	@OneToMany(mappedBy = "device")
	private List<Location> locations;
}
