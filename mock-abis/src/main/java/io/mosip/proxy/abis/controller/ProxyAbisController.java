package io.mosip.proxy.abis.controller;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mosip.proxy.abis.Listener;
import io.mosip.proxy.abis.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.proxy.abis.exception.BindingException;
import io.mosip.proxy.abis.exception.FailureReasonsConstants;
import io.mosip.proxy.abis.exception.RequestException;
import io.mosip.proxy.abis.service.ProxyAbisInsertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;


@CrossOrigin
@RestController
@Api(value = "Abis", description = "Provides API's for proxy Abis", tags = "Proxy Abis API")
@RequestMapping("api/v0/proxyabis/")
public class ProxyAbisController {

	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisController.class);

	@Autowired
	ProxyAbisInsertService abisInsertService;

	@Autowired
	private Listener listener;

	private Timer timer = new Timer();

	@RequestMapping(value = "insertrequest", method = RequestMethod.POST)
	@ApiOperation(value = "Save Insert Request")
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
			processInsertRequest(ie, 1);
		} catch (RequestException exp) {
			logger.error("Exception while saving insert request");
			RequestMO re = new RequestMO(ie.getId(), ie.getVersion(), ie.getRequestId(), ie.getRequesttime(),
					ie.getReferenceId());
			exp.setEntity(re);
			if (null == exp.getReasonConstant())
				exp.setReasonConstant(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
			throw exp;
		}
		return new ResponseEntity<>("Success", HttpStatus.OK);
	}

	@RequestMapping(value = "deleterequest", method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete Request")
	public ResponseEntity<Object> deleteRequest(@RequestBody RequestMO ie) {
		try {
			return processDeleteRequest(ie, 1);
		} catch (RequestException exp) {
			logger.error("Exception while deleting reference id");
			exp.setEntity(ie);
			if (null == exp.getReasonConstant())
				exp.setReasonConstant(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
			throw exp;
		}

	}

	@RequestMapping(value = "upload", method = RequestMethod.POST)
	@ApiOperation(value = "Upload certificate Request")
	public ResponseEntity<String> uploadcertificate(@RequestParam("file") MultipartFile uploadfile,
			@RequestParam("password") String password, @RequestParam("alias") String alias,@RequestParam("keystore") String keystore) {
		if (uploadfile.isEmpty())
			return new ResponseEntity("Please select a file", HttpStatus.NO_CONTENT);

		if (null == alias || alias.isEmpty())
			return new ResponseEntity("Please enter alias", HttpStatus.NO_CONTENT);
		if (null == password || password.isEmpty())
			return new ResponseEntity("Please enter password", HttpStatus.NO_CONTENT);

		return new ResponseEntity<String>( abisInsertService.saveUploadedFileWithParameters(uploadfile, alias, password,keystore),HttpStatus.OK);


	}

	@RequestMapping(value = "identifyrequest", method = RequestMethod.POST)
	@ApiOperation(value = "Checks duplication")
	public ResponseEntity<Object> identityRequest(@RequestBody IdentityRequest ir) {
		try {
			return processIdentityRequest(ir, 1);
		} catch (RequestException exp) {
			logger.error("Error while finding duplicates for " + ir.getReferenceId());
			RequestMO re = new RequestMO(ir.getId(), ir.getVersion(), ir.getRequestId(), ir.getRequesttime(),
					ir.getReferenceId());
			exp.setEntity(re);
			if (null == exp.getReasonConstant())
				exp.setReasonConstant(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
			throw exp;
		}
	}

	public ResponseEntity<Object> deleteRequestThroughListner(RequestMO ie, int msgType) {
		try {
			return processDeleteRequest(ie, msgType);
		} catch (Exception ex) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 2,
					FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
			return new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private ResponseEntity<Object> processDeleteRequest(RequestMO ie, int msgType) {
		logger.info("Deleting request with reference id" + ie.getReferenceId());
		abisInsertService.deleteData(ie.getReferenceId());
		ResponseMO response = new ResponseMO(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 1);
		logger.info("Successfully deleted reference id" + ie.getReferenceId());
		ResponseEntity<Object> responseEntity = new ResponseEntity<Object>(response, HttpStatus.OK);
		executeAsync(responseEntity, 0, msgType);
		return responseEntity;
	}

	public ResponseEntity<Object> identityRequestThroughListner(IdentityRequest ir, int msgType) {
		try {
			return processIdentityRequest(ir, msgType);
		} catch (Exception ex) {
			FailureResponse fr = new FailureResponse(ir.getId(), ir.getRequestId(), ir.getRequesttime(), 2,
					FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
			return new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private ResponseEntity<Object> processIdentityRequest(IdentityRequest ir, int msgType) {
		logger.info("Finding duplication for reference ID " + ir.getReferenceId());
		int delayResponse = 0;
		ResponseEntity<Object> responseEntity;
		try {
			IdentifyDelayResponse idr = abisInsertService.findDuplication(ir);
			responseEntity = new ResponseEntity<Object>(idr.getIdentityResponse(), HttpStatus.OK);
			delayResponse = idr.getDelayResponse();
		} catch (RequestException exp) {
			FailureResponse fr = new FailureResponse(ir.getId(), ir.getRequestId(), ir.getRequesttime(), 2,
					null == exp.getReasonConstant() ? FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN
							: exp.getReasonConstant().toString());
			delayResponse = exp.getDelayResponse();
			responseEntity = new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
		executeAsync(responseEntity, delayResponse, msgType);
		return responseEntity;
	}

	public ResponseEntity<Object> saveInsertRequestThroughListner(InsertRequestMO ie, int msgType) {
		logger.info("Saving Insert Request");
		String validate = validateRequest(ie);
		if (null != validate) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 2, validate);
			return new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
		try {
			return processInsertRequest(ie, msgType);
		} catch (RequestException exp) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 2,
					null == exp.getReasonConstant() ? FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN
							: exp.getReasonConstant().toString());
			return new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	public ResponseEntity<Object> processInsertRequest(InsertRequestMO ie, int msgType) {
		int delayResponse = 0;
		ResponseEntity<Object> responseEntity;
		try {
			String validate = validateRequest(ie);
			if (null != validate) {
				FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 2, validate);
				responseEntity = new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
			} else {
				delayResponse = abisInsertService.insertData(ie);
				ResponseMO responseMO = new ResponseMO(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 1);
				responseEntity = new ResponseEntity<Object>(responseMO, HttpStatus.OK);
			}
		} catch (RequestException exp) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 2,
					null == exp.getReasonConstant() ? FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN
							: exp.getReasonConstant().toString());
			delayResponse = exp.getDelayResponse();
			responseEntity = new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
		executeAsync(responseEntity, delayResponse, msgType);
		return responseEntity;
	}

	private String validateRequest(InsertRequestMO ie) {

		if (null != ie.getId() && !ie.getId().isEmpty() && !ie.getId().equalsIgnoreCase("mosip.abis.insert"))
			return FailureReasonsConstants.INVALID_ID;
		if (null == ie.getRequestId() || ie.getRequestId().isEmpty())
			return FailureReasonsConstants.MISSING_REQUESTID;
		if (null == ie.getRequesttime())
			return FailureReasonsConstants.MISSING_REQUESTTIME;
		if (null == ie.getReferenceId() || ie.getReferenceId().isEmpty())
			return FailureReasonsConstants.MISSING_REFERENCEID;
		if (null != ie.getVersion() && !ie.getVersion().isEmpty()) {
			if (!ie.getVersion().matches("[0-9]+.[0-9]"))
				return FailureReasonsConstants.INVALID_VERSION;
		}
		return null;

	}

	public void executeAsync(ResponseEntity<Object> finalResponseEntity, int delayResponse, int msgType){
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					listener.sendToQueue(finalResponseEntity, msgType);
					logger.info("Scheduled job completed: MsgType "+msgType);
				} catch (JsonProcessingException | UnsupportedEncodingException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		};
		logger.info("Adding timed task with timer as "+delayResponse+" seconds");
		timer.schedule(task, delayResponse*1000);
	}
}
