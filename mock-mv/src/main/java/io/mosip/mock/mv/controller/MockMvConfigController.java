package io.mosip.mock.mv.controller;

import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.mock.mv.dto.ConfigureDto;
import io.mosip.mock.mv.dto.Expectation;
import io.mosip.mock.mv.service.MockMvDecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin
@RestController
@Tag(name = "Proxy MockMv config API", description = "Provides API's for configuring proxy MockMv")
@RequestMapping("/config")
public class MockMvConfigController {
	
    private static final Logger logger = LoggerFactory.getLogger(MockMvConfigController.class);

	@Autowired
	MockMvDecisionService mockMvDecisionService;
    
	 @RequestMapping(value = "/configureMockMv", method = RequestMethod.POST)
	    @Operation(summary = "Configure Request", description = "Configure Request", tags = { "Proxy MockMv config API" })
	    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
	            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	    public ResponseEntity<String> configure(@Valid @RequestBody ConfigureDto configureDto, BindingResult bd)
	            throws Exception {
	        logger.info("Configure Request");
	        try {
	        	mockMvDecisionService.setMockMvDecision(configureDto.getMockMvDescision());
	            logger.info("Configuration updated overrideMockMvDecision: "+mockMvDecisionService.getMockMvDecision());
	            return new ResponseEntity<>("Successfully updated the configuration", HttpStatus.OK);
	        } catch (RuntimeException exp) {
	            logger.error("Exception in configure request: "+exp.getMessage());
	            throw exp;
	        }
	    }
	 
	 @RequestMapping(value = "/configureMockMv", method = RequestMethod.GET)
	    @Operation(summary = "Configure Request", description = "Configure Request", tags = { "Proxy MockMv config API" })
	    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
	            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	    public ResponseEntity<ConfigureDto> checkConfiguration()
	            throws Exception {
	        logger.info("Configure Request");
	        try {
	            ConfigureDto configureDto = new ConfigureDto();
	            configureDto.setMockMvDescision(mockMvDecisionService.getMockMvDecision());
	            return new ResponseEntity<>(configureDto, HttpStatus.OK);
	        } catch (RuntimeException exp) {
	            logger.error("Exception while getting configuration: "+exp.getMessage());
	            throw exp;
	        }
	    }
	 
	 @RequestMapping(value = "/expectationMockMv", method = RequestMethod.POST)
	    @Operation(summary = "Set expectation", description = "Set expectation", tags = { "Proxy mockMv config API" })
	    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
	            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	    public ResponseEntity<String> setExpectation(@Valid @RequestBody Expectation expectation) throws Exception {
	        logger.info("Setting expectation" + expectation.getRId());
	        try {
	        	mockMvDecisionService.setExpectation(expectation);
	            return new ResponseEntity<>("Successfully inserted expectation "+expectation.getRId(), HttpStatus.OK);
	        } catch (RuntimeException exp) {
	            logger.error("Exception while getting expectation: "+exp.getMessage());
	            throw exp;
	        }
	    }
	 
	 @RequestMapping(value = "/expectationMockMv", method = RequestMethod.GET)
	    @Operation(summary = "Gets expectation", description = "Gets expectation", tags = { "Proxy mockMv config API" })
	    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
	            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	    public ResponseEntity<Map<String, Expectation>> getExpectation() throws Exception {
	        logger.info("Getting expectation");
	        try {
	            return new ResponseEntity<>(mockMvDecisionService.getExpectations(), HttpStatus.OK);
	        } catch (RuntimeException exp) {
	            logger.error("Exception while getting expectation: "+exp.getMessage());
	            throw exp;
	        }
	    }
	 
	 @RequestMapping(value = "/expectationMockMv/{rid}", method = RequestMethod.GET)
	    @Operation(summary = "Get expectation", description = "Get expectation", tags = { "Proxy MockMv config API" })
	    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
	            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	    public ResponseEntity<?> getExpectation(@PathVariable String rid) {
	        logger.info("Getting expectation: "+rid);
	        try {
	        	Expectation expectation = mockMvDecisionService.getExpectation(rid);
	        	if(expectation.getRId()!=null)
	            return new ResponseEntity<>(mockMvDecisionService.getExpectation(rid), HttpStatus.OK);
	        	else {
		            return new ResponseEntity<>("No expectation set for given rid:"+rid, HttpStatus.OK);
	        	}
	        } catch (RuntimeException exp) {
	            logger.error("Exception while getting expectation: "+exp.getMessage());
	            throw exp;
	        }
	    }
	 
	 @RequestMapping(value = "/expectationMockMv/{rid}", method = RequestMethod.DELETE)
	    @Operation(summary = "Delete expectation", description = "Delete expectation", tags = { "Proxy MockMv config API" })
	    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
	            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	    public ResponseEntity<String> deleteExpectation(@PathVariable String rid) {
	        logger.info("Delete expectation: "+rid);
	        try {
	        	mockMvDecisionService.deleteExpectation(rid);
	            return new ResponseEntity<>("Successfully deleted expectation "+rid, HttpStatus.OK);
	        } catch (RuntimeException exp) {
	            logger.error("Exception while deleting expectation: "+exp.getMessage());
	            throw exp;
	        }
	    }
	 
	 @RequestMapping(value = "/expectationMockMv", method = RequestMethod.DELETE)
	    @Operation(summary = "/Delete expectations", description = "Delete expectations", tags = { "Proxy MockMv config API" })
	    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
	            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
	            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	    public ResponseEntity<String> deleteAllExpectations() {
	        logger.info("Delete all expectations");
	        try {
	        	mockMvDecisionService.deleteExpectations();
	            return new ResponseEntity<>("Successfully deleted expectations ", HttpStatus.OK);
	        } catch (RuntimeException exp) {
	            logger.error("Exception while deleting expectations: "+exp.getMessage());
	            throw exp;
	        }
	    }

}
