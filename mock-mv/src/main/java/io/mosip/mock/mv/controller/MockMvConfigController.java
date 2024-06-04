package io.mosip.mock.mv.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mock.mv.constant.MVErrorCode;
import io.mosip.mock.mv.dto.ConfigureDto;
import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.exception.MVException;
import io.mosip.mock.mv.queue.Listener;
import io.mosip.mock.mv.service.MockMvDecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@SuppressWarnings({ "java:S5122" })
@CrossOrigin
@RestController
@Tag(name = "Proxy MockMv config API", description = "Provides API's for configuring proxy MockMv")
@RequestMapping("/config")
public class MockMvConfigController {
	private Logger logger = LoggerFactory.getLogger(MockMvConfigController.class);

	private MockMvDecisionService mockMvDecisionService;

	@Autowired
	public MockMvConfigController(MockMvDecisionService mockMvDecisionService) {
		this.mockMvDecisionService = mockMvDecisionService;
	}

	@PostMapping(value = "/configureMockMv")
	@Operation(summary = "Configure Request", description = "Configure Request", tags = { "Proxy MockMv config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> configure(@Valid @RequestBody ConfigureDto configureDto, BindingResult bd) {
		logger.info("Configure Request");
		try {
			mockMvDecisionService.setMockMvDecision(configureDto.getMockMvDescision());
			logger.info("Configuration updated overrideMockMvDecision: {}", mockMvDecisionService.getMockMvDecision());
			return new ResponseEntity<>("Successfully updated the configuration", HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception in configure request: ", exp);
			throw new MVException(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(),
					MVErrorCode.CONFIGURE_EXCEPTION.getErrorMessage() + exp.getLocalizedMessage());
		}
	}

	@GetMapping(value = "/configureMockMv")
	@Operation(summary = "Configure Request", description = "Configure Request", tags = { "Proxy MockMv config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<ConfigureDto> checkConfiguration() {
		logger.info("Configure Request");
		try {
			ConfigureDto configureDto = new ConfigureDto();
			configureDto.setMockMvDescision(mockMvDecisionService.getMockMvDecision());
			return new ResponseEntity<>(configureDto, HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while getting configuration: ", exp);
			throw new MVException(MVErrorCode.CONFIGURE_EXCEPTION.getErrorCode(),
					MVErrorCode.CONFIGURE_EXCEPTION.getErrorMessage() + exp.getLocalizedMessage());
		}
	}

	@PostMapping(value = "/expectationMockMv")
	@Operation(summary = "Set expectation", description = "Set expectation", tags = { "Proxy mockMv config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> setExpectation(@Valid @RequestBody Expectation expectation) {
		String request = "";
		try {
			request = Listener.javaObjectToJsonString(expectation);
			logger.info("Setting expectation {}", request);

			mockMvDecisionService.setExpectation(expectation);
			return new ResponseEntity<>("Successfully inserted expectation " + expectation.getRId(), HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while getting expectation:", exp);
			throw new MVException(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(),
					MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage() + exp.getLocalizedMessage());
		}
	}

	@GetMapping(value = "/expectationMockMv")
	@Operation(summary = "Gets expectation", description = "Gets expectation", tags = { "Proxy mockMv config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<Map<String, Expectation>> getExpectation() {
		logger.info("Getting expectation");
		try {
			return new ResponseEntity<>(mockMvDecisionService.getExpectations(), HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while getting expectation: ", exp);
			throw new MVException(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(),
					MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage() + exp.getLocalizedMessage());
		}
	}

	@GetMapping(value = "/expectationMockMv/{rid}")
	@Operation(summary = "Get expectation", description = "Get expectation", tags = { "Proxy MockMv config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> getExpectation(@PathVariable String rid) {
		logger.info("Getting expectation: {}", rid);
		try {
			Expectation expectation = mockMvDecisionService.getExpectation(rid);
			if (expectation.getRId() != null)
				return new ResponseEntity<>(mockMvDecisionService.getExpectation(rid).toString(), HttpStatus.OK);
			else {
				return new ResponseEntity<>("No expectation set for given rid:" + rid, HttpStatus.OK);
			}
		} catch (Exception exp) {
			logger.error("Exception while getting expectation: ", exp);
			throw new MVException(MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorCode(),
					MVErrorCode.GET_EXPECTATION_EXCEPTION.getErrorMessage() + exp.getLocalizedMessage());
		}
	}

	@DeleteMapping(value = "/expectationMockMv/{rid}")
	@Operation(summary = "Delete expectation", description = "Delete expectation", tags = { "Proxy MockMv config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> deleteExpectation(@PathVariable String rid) {
		logger.info("Delete expectation: {}", rid);
		try {
			mockMvDecisionService.deleteExpectation(rid);
			return new ResponseEntity<>("Successfully deleted expectation " + rid, HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while deleting expectation: ", exp);
			throw new MVException(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(),
					MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage() + exp.getLocalizedMessage());
		}
	}

	@DeleteMapping(value = "/expectationMockMv")
	@Operation(summary = "/Delete expectations", description = "Delete expectations", tags = {
			"Proxy MockMv config API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S2139" })
	public ResponseEntity<String> deleteAllExpectations() {
		logger.info("Delete all expectations");
		try {
			mockMvDecisionService.deleteExpectations();
			return new ResponseEntity<>("Successfully deleted expectations ", HttpStatus.OK);
		} catch (Exception exp) {
			logger.error("Exception while deleting expectations: ", exp);
			throw new MVException(MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorCode(),
					MVErrorCode.DELETE_EXPECTATION_EXCEPTION.getErrorMessage() + exp.getLocalizedMessage());
		}
	}
}