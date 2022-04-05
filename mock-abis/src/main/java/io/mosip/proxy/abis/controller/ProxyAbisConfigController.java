package io.mosip.proxy.abis.controller;

import io.mosip.proxy.abis.dto.ConfigureDto;
import io.mosip.proxy.abis.dto.Expectation;
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

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@Tag(name = "Proxy Abis config API", description = "Provides API's for configuring proxy Abis")
@RequestMapping("config/")
public class ProxyAbisConfigController {

	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisConfigController.class);

	@Autowired
	ProxyAbisConfigService proxyAbisConfigService;

	@RequestMapping(value = "expectation", method = RequestMethod.POST)
	@Operation(summary = "Sets expectation", description = "Sets expectation", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<String> setExpectation(@Valid @RequestBody Expectation expectation) throws Exception {
		logger.info("Setting expectation" + expectation.getId());
		try {
			proxyAbisConfigService.setExpectation(expectation);
			return new ResponseEntity<>("Successfully inserted expectation " + expectation.getId(), HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception while getting expectation: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "expectation", method = RequestMethod.GET)
	@Operation(summary = "Gets expectation", description = "Gets expectation", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Map<String, Expectation>> getExpectation() throws Exception {
		logger.info("Getting expectation");
		try {
			return new ResponseEntity<>(proxyAbisConfigService.getExpectations(), HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception while getting expectation: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "expectation/{id}", method = RequestMethod.DELETE)
	@Operation(summary = "Delete expectation", description = "Delete expectation", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<String> deleteExpectation(@PathVariable String id) {
		logger.info("Delete expectation: " + id);
		try {
			proxyAbisConfigService.deleteExpectation(id);
			return new ResponseEntity<>("Successfully deleted expectation " + id, HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception while deleting expectation: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "expectation", method = RequestMethod.DELETE)
	@Operation(summary = "Delete expectations", description = "Delete expectations", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<String> deleteAllExpectations() {
		logger.info("Delete all expectations");
		try {
			proxyAbisConfigService.deleteExpectations();
			return new ResponseEntity<>("Successfully deleted expectations ", HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception while deleting expectations: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "configure", method = RequestMethod.GET)
	@Operation(summary = "Configure Request", description = "Configure Request", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<ConfigureDto> checkConfiguration() throws Exception {
		logger.info("Configure Request");
		try {
			ConfigureDto configureDto = new ConfigureDto();
			configureDto.setFindDuplicate(proxyAbisConfigService.getDuplicate());
			return new ResponseEntity<>(configureDto, HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception while getting configuration: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "configure", method = RequestMethod.POST)
	@Operation(summary = "Configure Request", description = "Configure Request", tags = { "Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<String> configure(@Valid @RequestBody ConfigureDto ie, BindingResult bd) throws Exception {
		logger.info("Configure Request");
		try {
			proxyAbisConfigService.setDuplicate(ie.getFindDuplicate());
			logger.info("[Configuration updated] overrideFindDuplicate: " + proxyAbisConfigService.getDuplicate());
			return new ResponseEntity<>("Successfully updated the configuration", HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception in configure request: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "cache", method = RequestMethod.GET)
	@Operation(summary = "Get cached biometrics", description = "Get cached biometrics", tags = {
			"Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<List<String>> getCache() throws Exception {
		logger.info("Get cached biometrics Request");
		try {
			return new ResponseEntity<>(proxyAbisConfigService.getCachedBiometrics(), HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception in cache request: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "cache/{hash}", method = RequestMethod.GET)
	@Operation(summary = "Get cached biometrics by hash", description = "Get cached biometrics by hash", tags = {
			"Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<List<String>> getCacheByHash(@PathVariable String hash) throws Exception {
		logger.info("Get cached biometrics by hash: " + hash);
		try {
			return new ResponseEntity<>(proxyAbisConfigService.getCachedBiometric(hash), HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception in cache request: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "cache", method = RequestMethod.DELETE)
	@Operation(summary = "Delete cached biometrics", description = "Delete cached biometrics", tags = {
			"Proxy Abis config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<String> deleteCache() throws Exception {
		logger.info("Delete cached biometrics Request");
		try {
			proxyAbisConfigService.deleteAllCachedBiometrics();
			logger.info("[Configuration updated] overrideFindDuplicate: " + proxyAbisConfigService.getDuplicate());
			return new ResponseEntity<>("Successfully deleted cached biometrics", HttpStatus.OK);
		} catch (RuntimeException exp) {
			logger.error("Exception in cache request: " + exp.getMessage());
			throw exp;
		}
	}

	@RequestMapping(value = "checkDuplicate", method = RequestMethod.GET)
	public ResponseEntity<String> setExpectation(@RequestParam("newduplicate") boolean newDuplicate) throws Exception {
		logger.info("Setting new duplicate " + newDuplicate);
		proxyAbisConfigService.setNewDuplicate(newDuplicate);
		logger.info("Get new duplicate " + proxyAbisConfigService.getNewDuplicate());
		return new ResponseEntity<>("Successfully updated " + proxyAbisConfigService.getNewDuplicate(), HttpStatus.OK);
	}

	@PostConstruct
	private void postConstruct() {
		proxyAbisConfigService.setNewDuplicate(proxyAbisConfigService.getDuplicate());
	}
}