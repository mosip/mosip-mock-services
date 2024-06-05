package io.mosip.proxy.abis.controller;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.mosip.proxy.abis.dto.FailureResponse;
import io.mosip.proxy.abis.dto.IdentifyDelayResponse;
import io.mosip.proxy.abis.dto.IdentityRequest;
import io.mosip.proxy.abis.dto.InsertRequestMO;
import io.mosip.proxy.abis.dto.RequestMO;
import io.mosip.proxy.abis.dto.ResponseMO;
import io.mosip.proxy.abis.exception.BindingException;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.listener.Listener;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;
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
@Tag(name = "Proxy Abis API", description = "Provides API's for proxy Abis")
@RequestMapping("abis/")
public class ProxyAbisController {
	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisController.class);

	private ProxyAbisInsertService abisInsertService;
	private Listener listener;
	private Timer timer = new Timer();

	@Autowired
	public ProxyAbisController(ProxyAbisInsertService abisInsertService) {
		this.abisInsertService = abisInsertService;
		this.timer = new Timer();
	}
	
	@PostMapping(value = "insertrequest")
	@Operation(summary = "Save Insert Request", description = "Save Insert Request", tags = { "Proxy Abis API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@SuppressWarnings({ "java:S112" })
	public ResponseEntity<Object> saveInsertRequest(@Valid @RequestBody InsertRequestMO ie, BindingResult bd)
			throws Exception {
		logger.info("Saving Insert Request");
		if (bd.hasErrors()) {
			logger.info("Some fields are missing in the insert request");
			RequestMO re = new RequestMO(ie.getId(), ie.getVersion(), ie.getRequestId(), ie.getRequesttime(),
					ie.getReferenceId());
			throw new BindingException(re, bd);
		}
		try {
			return processInsertRequest(ie, 1);
		} catch (RequestException exp) {
			logger.error("Exception while saving insert request");
			RequestMO re = new RequestMO(ie.getId(), ie.getVersion(), ie.getRequestId(), ie.getRequesttime(),
					ie.getReferenceId());
			
			String reason = null;
			if (Objects.isNull(exp.getReasonConstant()))
				reason = FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN;

			RequestException expInfo = new RequestException(re, reason, 0);
			return new ResponseEntity<>(expInfo.getReasonConstant(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping(value = "deleterequest")
	@Operation(summary = "Delete Request", description = "Delete Request", tags = { "Proxy Abis API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> deleteRequest(@RequestBody RequestMO ie) {
		try {
			return processDeleteRequest(ie, 1);
		} catch (RequestException exp) {
			logger.error("Exception while deleting reference id", exp);
			
			String reason = null;
			if (Objects.isNull(exp.getReasonConstant()))
				reason = FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN;

			RequestException expInfo = new RequestException(ie, reason, 0);
			return new ResponseEntity<>(expInfo.getReasonConstant(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Upload certificate Request", description = "Upload certificate Request", tags = {
			"Proxy Abis API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<String> uploadcertificate(@RequestBody MultipartFile uploadfile,
			@RequestParam("password") String password, @RequestParam("alias") String alias,
			@RequestParam("keystore") String keystore) {
		if (uploadfile.isEmpty())
			return new ResponseEntity<>("Please select a file", HttpStatus.NO_CONTENT);

		if (null == alias || alias.isEmpty())
			return new ResponseEntity<>("Please enter alias", HttpStatus.NO_CONTENT);
		if (null == password || password.isEmpty())
			return new ResponseEntity<>("Please enter password", HttpStatus.NO_CONTENT);

		return new ResponseEntity<>(
				abisInsertService.saveUploadedFileWithParameters(uploadfile, alias, password, keystore), HttpStatus.OK);
	}

	@PostMapping(value = "identifyrequest")
	@Operation(summary = "Checks duplication", description = "Checks duplication", tags = { "Proxy Abis API" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> identityRequest(@RequestBody IdentityRequest ir) {
		try {
			return processIdentityRequest(ir, 1);
		} catch (RequestException exp) {
			logger.info("Error while finding duplicates for {}", ir.getReferenceId());
			logger.error("Error while finding duplicates", exp);
			RequestMO re = new RequestMO(ir.getId(), ir.getVersion(), ir.getRequestId(), ir.getRequesttime(),
					ir.getReferenceId());
			String reason = null;
			if (Objects.isNull(exp.getReasonConstant()))
				reason = FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN;

			RequestException expInfo = new RequestException(re, reason, 0);
			return new ResponseEntity<>(expInfo.getReasonConstant(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<Object> deleteRequestThroughListner(RequestMO ie, int msgType) {
		try {
			return processDeleteRequest(ie, msgType);
		} catch (Exception ex) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), "2",
					FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
			return new ResponseEntity<>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private ResponseEntity<Object> processDeleteRequest(RequestMO ie, int msgType) {
		logger.info("Deleting request with reference id {}", ie.getReferenceId());
		abisInsertService.deleteData(ie.getReferenceId());
		ResponseMO response = new ResponseMO(ie.getId(), ie.getRequestId(), ie.getRequesttime(), "1");
		logger.info("Successfully deleted reference id {}", ie.getReferenceId());
		ResponseEntity<Object> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);
		executeAsync(responseEntity, 0, msgType);
		return responseEntity;
	}

	public ResponseEntity<Object> identityRequestThroughListner(IdentityRequest ir, int msgType) {
		try {
			return processIdentityRequest(ir, msgType);
		} catch (Exception ex) {
			FailureResponse fr = new FailureResponse(ir.getId(), ir.getRequestId(), ir.getRequesttime(), "2",
					FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
			return new ResponseEntity<>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private ResponseEntity<Object> processIdentityRequest(IdentityRequest ir, int msgType) {
		logger.info("Finding duplication for reference ID {}", ir.getReferenceId());
		int delayResponse = 0;
		ResponseEntity<Object> responseEntity;
		try {
			IdentifyDelayResponse idr = abisInsertService.findDuplication(ir);
			responseEntity = new ResponseEntity<>(idr.getIdentityResponse(), HttpStatus.OK);
			delayResponse = idr.getDelayResponse();
		} catch (RequestException exp) {
			FailureResponse fr = new FailureResponse(ir.getId(), ir.getRequestId(), ir.getRequesttime(), "2",
					null == exp.getReasonConstant() ? FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN
							: exp.getReasonConstant());
			delayResponse = exp.getDelayResponse();
			responseEntity = new ResponseEntity<>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
		executeAsync(responseEntity, delayResponse, msgType);
		return responseEntity;
	}

	public ResponseEntity<Object> saveInsertRequestThroughListner(InsertRequestMO ie, int msgType) {
		logger.info("Saving Insert Request");
		String validate = validateRequest(ie);
		if (null != validate) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), "2", validate);
			return new ResponseEntity<>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
		try {
			return processInsertRequest(ie, msgType);
		} catch (RequestException exp) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), "2",
					null == exp.getReasonConstant() ? FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN
							: exp.getReasonConstant());
			return new ResponseEntity<>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	public ResponseEntity<Object> processInsertRequest(InsertRequestMO ie, int msgType) {
		int delayResponse = 0;
		ResponseEntity<Object> responseEntity;
		try {
			String validate = validateRequest(ie);
			if (null != validate) {
				FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), "2",
						validate);
				responseEntity = new ResponseEntity<>(fr, HttpStatus.NOT_ACCEPTABLE);
			} else {
				delayResponse = abisInsertService.insertData(ie);
				ResponseMO responseMO = new ResponseMO(ie.getId(), ie.getRequestId(), ie.getRequesttime(), "1");
				responseEntity = new ResponseEntity<>(responseMO, HttpStatus.OK);
			}
		} catch (RequestException exp) {
			logger.error("processInsertRequest::failureReason:: ", exp);
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), "2",
					null == exp.getReasonConstant() ? FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN
							: exp.getReasonConstant());
			delayResponse = exp.getDelayResponse();
			responseEntity = new ResponseEntity<>(fr, HttpStatus.OK);
		}
		executeAsync(responseEntity, delayResponse, msgType);
		return responseEntity;
	}

	@SuppressWarnings({ "java:S6353" })
	private String validateRequest(InsertRequestMO ie) {
		if (!Objects.isNull(ie.getId()) && !ie.getId().isEmpty() && !ie.getId().equalsIgnoreCase("mosip.abis.insert"))
			return FailureReasonsConstants.INVALID_ID;
		if (Objects.isNull(ie.getRequestId()) || ie.getRequestId().isEmpty())
			return FailureReasonsConstants.MISSING_REQUESTID;
		if (Objects.isNull(ie.getRequesttime()))
			return FailureReasonsConstants.MISSING_REQUESTTIME;
		if (Objects.isNull(ie.getReferenceId()) || ie.getReferenceId().isEmpty())
			return FailureReasonsConstants.MISSING_REFERENCEID;
		if (!Objects.isNull(ie.getVersion()) && !ie.getVersion().isEmpty() && !ie.getVersion().matches("[0-9]+.[0-9]"))
			return FailureReasonsConstants.INVALID_VERSION;
		return null;
	}

	public void executeAsync(ResponseEntity<Object> finalResponseEntity, int delayResponse, int msgType) {
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					listener.sendToQueue(finalResponseEntity, msgType);
					logger.info("Scheduled job completed: MsgType {}", msgType);
				} catch (JsonProcessingException e) {
					logger.error("executeAsync::error ", e);
				}
			}
		};
		logger.info("Adding timed task with timer as {} in seconds", delayResponse);
		timer.schedule(task, (long)delayResponse * 1000);
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}
}