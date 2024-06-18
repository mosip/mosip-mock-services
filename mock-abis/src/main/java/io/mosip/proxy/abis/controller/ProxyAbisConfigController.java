package io.mosip.proxy.abis.controller;

import io.mosip.proxy.abis.constant.AbisErrorCode;
import io.mosip.proxy.abis.dto.ConfigureDto;
import io.mosip.proxy.abis.dto.Expectation;
import io.mosip.proxy.abis.exception.AbisException;
import io.mosip.proxy.abis.service.ProxyAbisConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller class that handles HTTP requests related to configuring proxy
 * Abis. This controller provides endpoints for setting, getting, and deleting
 * expectations, configuring requests, and managing cached biometrics.
 */
@SuppressWarnings({ "java:S5122" })
@CrossOrigin
@RestController
@Tag(name = "Proxy Abis config API", description = "Provides API's for configuring proxy Abis")
@RequestMapping("config/")
public class ProxyAbisConfigController {
	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisConfigController.class);

	private ProxyAbisConfigService proxyAbisConfigService;

	/**
	 * Constructs the controller with the provided ProxyAbisConfigService instance.
	 *
	 * @param proxyAbisConfigService The service instance to be used by this
	 *                               controller.
	 */
	@Autowired
	public ProxyAbisConfigController(ProxyAbisConfigService proxyAbisConfigService) {
		this.proxyAbisConfigService = proxyAbisConfigService;
	}

	/**
	 * Sets the expectation using the provided Expectation object.
	 *
	 * @param expectation The Expectation object containing the expectation details.
	 * @return ResponseEntity indicating the success or failure of the operation.
	 * @throws AbisException If an error occurs during setting the expectation.
	 */
	@PostMapping(value = "expectation")
	@Operation(summary = "Sets expectation", description = "Sets expectation", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> setExpectation(@Valid @RequestBody Expectation expectation) {
		logger.info("Setting expectation {}", expectation.getId());
		try {
			proxyAbisConfigService.setExpectation(expectation);
			return new ResponseEntity<>("Successfully inserted expectation " + expectation.getId(), HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while getting expectation: ", exp);
			throw new AbisException(AbisErrorCode.SET_EXPECTATION_EXCEPTION.getErrorCode(),
					AbisErrorCode.SET_EXPECTATION_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}

	/**
	 * Retrieves the expectations currently set.
	 *
	 * @return ResponseEntity containing a map of expectation IDs to Expectation
	 *         objects.
	 * @throws AbisException If an error occurs during retrieving the expectations.
	 */
	@GetMapping(value = "expectation")
	@Operation(summary = "Gets expectation", description = "Gets expectation", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<Map<String, Expectation>> getExpectation() {
		logger.info("Getting expectation");
		try {
			return new ResponseEntity<>(proxyAbisConfigService.getExpectations(), HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while getting expectation: ", exp);
			throw new AbisException(AbisErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(),
					AbisErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}

	/**
	 * Deletes the expectation identified by the provided ID.
	 *
	 * @param id The ID of the expectation to be deleted.
	 * @return ResponseEntity indicating the success or failure of the operation.
	 * @throws AbisException If an error occurs during deleting the expectation.
	 */
	@DeleteMapping(value = "expectation/{id}")
	@Operation(summary = "Delete expectation", description = "Delete expectation", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> deleteExpectation(@PathVariable String id) {
		logger.info("Delete expectation: {}", id);
		try {
			proxyAbisConfigService.deleteExpectation(id);
			return new ResponseEntity<>("Successfully deleted expectation " + id, HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while deleting expectation: ", exp);
			throw new AbisException(AbisErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(),
					AbisErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}

	/**
	 * Deletes all expectations currently set.
	 *
	 * @return ResponseEntity indicating the success or failure of the operation.
	 * @throws AbisException If an error occurs during deleting the expectations.
	 */
	@DeleteMapping(value = "expectation")
	@Operation(summary = "Delete expectations", description = "Delete expectations", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> deleteAllExpectations() {
		logger.info("Delete all expectations");
		try {
			proxyAbisConfigService.deleteExpectations();
			return new ResponseEntity<>("Successfully deleted expectations ", HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while deleting expectations: ", exp);
			throw new AbisException(AbisErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(),
					AbisErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}

	/**
	 * Retrieves the current configuration details.
	 *
	 * @return ResponseEntity containing the configuration details.
	 * @throws AbisException If an error occurs during retrieving the configuration.
	 */
	@GetMapping(value = "configure")
	@Operation(summary = "Configure Request", description = "Configure Request", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<ConfigureDto> checkConfiguration() {
		logger.info("Check Configure Request");
		try {
			ConfigureDto configureDto = new ConfigureDto();
			configureDto.setFindDuplicate(proxyAbisConfigService.getDuplicate());
			return new ResponseEntity<>(configureDto, HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while getting configuration: ", exp);
			throw new AbisException(AbisErrorCode.INVALID_CONFIGURATION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CONFIGURATION_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}

	/**
	 * Updates the configuration based on the provided ConfigureDto.
	 *
	 * @param ie The ConfigureDto object containing the updated configuration.
	 * @param bd The BindingResult object for validation.
	 * @return ResponseEntity indicating the success or failure of the operation.
	 * @throws AbisException If an error occurs during configuring the request.
	 */
	@PostMapping(value = "configure")
	@Operation(summary = "Configure Request", description = "Configure Request", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> configure(@Valid @RequestBody ConfigureDto ie, BindingResult bd) {
		logger.info("Configure Request");
		try {
			proxyAbisConfigService.setDuplicate(ie.getFindDuplicate());
			logger.info("[Configuration updated] overrideFindDuplicate: {}", proxyAbisConfigService.getDuplicate());
			return new ResponseEntity<>("Successfully updated the configuration", HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception in configure request: ", exp);
			throw new AbisException(AbisErrorCode.INVALID_CONFIGURATION_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CONFIGURATION_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}

	/**
	 * Retrieves the cached biometrics.
	 *
	 * @return ResponseEntity containing the list of cached biometrics.
	 * @throws AbisException If an error occurs during retrieving the cached
	 *                       biometrics.
	 */
	@GetMapping(value = "cache")
	@Operation(summary = "Get cached biometrics", description = "Get cached biometrics", tags = {
			"Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<List<String>> getCache() {
		logger.info("Get cached biometrics Request");
		try {
			return new ResponseEntity<>(proxyAbisConfigService.getCachedBiometrics(), HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception in getCache request: ", exp);
			throw new AbisException(AbisErrorCode.INVALID_CACHE_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CACHE_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}

	/**
	 * Retrieves the cached biometrics associated with the specified hash.
	 *
	 * @param hash The hash value used to retrieve the cached biometrics.
	 * @return ResponseEntity containing the list of cached biometrics matching the
	 *         hash.
	 * @throws AbisException If an error occurs during retrieving the cached
	 *                       biometrics by hash.
	 */
	@GetMapping(value = "cache/{hash}")
	@Operation(summary = "Get cached biometrics by hash", description = "Get cached biometrics by hash", tags = {
			"Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<List<String>> getCacheByHash(@PathVariable String hash) {
		logger.info("Get cached biometrics by hash: {}", hash);
		try {
			return new ResponseEntity<>(proxyAbisConfigService.getCachedBiometric(hash), HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception in getCacheByHash request: ", exp);
			throw new AbisException(AbisErrorCode.INVALID_CACHE_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CACHE_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}

	/**
	 * Deletes all cached biometric data.
	 *
	 * @return ResponseEntity indicating success or failure of the operation.
	 * @throws AbisException If there's an error while deleting cached biometric
	 *                       data.
	 */
	@DeleteMapping(value = "cache")
	@Operation(summary = "Delete cached biometrics", description = "Delete cached biometrics", tags = {
			"Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> deleteCache() {
		logger.info("Delete cached biometrics Request");
		try {
			proxyAbisConfigService.deleteAllCachedBiometrics();
			logger.info("[Configuration updated] overrideFindDuplicate: {}", proxyAbisConfigService.getDuplicate());
			return new ResponseEntity<>("Successfully deleted cached biometrics", HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception in deleteCache request: ", exp);
			throw new AbisException(AbisErrorCode.INVALID_CACHE_EXCEPTION.getErrorCode(),
					AbisErrorCode.INVALID_CACHE_EXCEPTION.getErrorMessage() + " " + exp.getLocalizedMessage());
		}
	}
}