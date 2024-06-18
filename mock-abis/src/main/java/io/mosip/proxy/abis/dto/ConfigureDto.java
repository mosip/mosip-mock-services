package io.mosip.proxy.abis.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO class representing configuration details.
 * <p>
 * This class encapsulates configuration information for a specific functionality
 * or feature. It includes attributes such as:
 * <ul>
 * <li>{@code findDuplicate}: A boolean flag indicating whether to find duplicates.</li>
 * </ul>
 * Use this class to transfer configuration data between components or services,
 * facilitating customization and behavior control in ABIS (Automated Biometric
 * Identification System) or similar systems.
 * </p>
 * 
 * @author 
 * @since 1.0.0
 */

@Data
@NoArgsConstructor
public class ConfigureDto {
	private Boolean findDuplicate;
}