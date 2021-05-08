package io.mosip.proxy.abis.controller;

import javax.validation.Valid;

import io.mosip.proxy.abis.entity.*;
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

@CrossOrigin
@RestController
@Api(value = "Abis", description = "Provides API's for proxy Abis", tags = "Proxy Abis API")
@RequestMapping("api/v0/proxyabis/")
public class ProxyAbisController {

	private static final Logger logger = LoggerFactory.getLogger(ProxyAbisController.class);

	@Autowired
	ProxyAbisInsertService abisInsertService;
	
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
			return processInsertRequest(ie);
		} catch (RequestException exp) {
			logger.error("Exception while saving insert request");
			RequestMO re = new RequestMO(ie.getId(), ie.getVersion(), ie.getRequestId(), ie.getRequesttime(),
					ie.getReferenceId());
			exp.setEntity(re);
			if (null == exp.getReasonConstant())
				exp.setReasonConstant(FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
			throw exp;
		}
	}

	@RequestMapping(value = "deleterequest", method = RequestMethod.DELETE)
	@ApiOperation(value = "Delete Request")
	public ResponseEntity<Object> deleteRequest(@RequestBody RequestMO ie) {
		try {
			return processDeleteRequest(ie);
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

	@RequestMapping(value = "identityrequest", method = RequestMethod.POST)
	@ApiOperation(value = "Checks duplication")
	public ResponseEntity<Object> identityRequest(@RequestBody IdentityRequest ir) {
		try {
			return processIdentityRequest(ir);
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

	public ResponseEntity<Object> deleteRequestThroughListner(RequestMO ie) {
		try {
			return processDeleteRequest(ie);
		} catch (Exception ex) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 2,
					FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN);
			return new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private ResponseEntity<Object> processDeleteRequest(RequestMO ie) {
		logger.info("Deleting request with reference id" + ie.getReferenceId());
		abisInsertService.deleteData(ie.getReferenceId());
		ResponseMO response = new ResponseMO(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 1);
		logger.info("Successfully deleted reference id" + ie.getReferenceId());
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	public ResponseEntity<Object> identityRequestThroughListner(IdentityRequest ir) {
		try {
			return processIdentityRequest(ir);
		} catch (Exception ex) {
			FailureResponse fr = new FailureResponse(ir.getId(), ir.getRequestId(), ir.getRequesttime(), 2,
					FailureReasonsConstants.UNABLE_TO_FETCH_BIOMETRIC_DETAILS);
			return new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}
	}

	private ResponseEntity<Object> processIdentityRequest(IdentityRequest ir) {
		logger.info("Finding duplication for reference ID " + ir.getReferenceId());
		IdentityResponse res = abisInsertService.findDuplication(ir);
		return new ResponseEntity<Object>(res, HttpStatus.OK);
	}

	public ResponseEntity<Object> saveInsertRequestThroughListner(InsertRequestMO ie) {
		logger.info("Saving Insert Request");
		String validate = validateRequest(ie);
		if (null != validate) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 2, validate);
			return new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}

		try {
			return processInsertRequest(ie);
		} catch (RequestException exp) {
			FailureResponse fr = new FailureResponse(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 2,
					null == exp.getReasonConstant() ? FailureReasonsConstants.INTERNAL_ERROR_UNKNOWN
							: exp.getReasonConstant().toString());
			return new ResponseEntity<Object>(fr, HttpStatus.NOT_ACCEPTABLE);
		}

	}

	private ResponseEntity<Object> processInsertRequest(InsertRequestMO ie) {
		abisInsertService.insertData(ie);
		ResponseMO response = new ResponseMO(ie.getId(), ie.getRequestId(), ie.getRequesttime(), 1);
		logger.info("Successfully inserted record ");
		return new ResponseEntity<Object>(response, HttpStatus.OK);
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

}
