package io.mosip.mock.mv.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing configuration details for MockMv decision.
 * <p>
 * This class encapsulates the decision configuration for MockMv, providing a structured
 * format to manage and retrieve the decision setting.
 * <p>
 * This DTO is typically used in APIs related to configuring and retrieving MockMv decisions.
 * 
 */
@Data
public class ConfigureDto {
	private String mockMvDescision;
}