package io.mosip.proxy.abis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.mosip.proxy.abis.dto.FailureResponse;

@ControllerAdvice
public class ExceptionInterceptor extends ResponseEntityExceptionHandler {

	@ExceptionHandler(RequestException.class)
	public ResponseEntity<Object> handleInsertRequestException(RequestException exp) {
		FailureResponse fr = new FailureResponse();
		if (null != exp.entity) {
			fr.setId(exp.entity.getId());
			fr.setRequestId(exp.entity.getRequestId());
			fr.setResponsetime(exp.entity.getRequesttime());
			fr.setReturnValue("2");
			fr.setFailureReason(exp.reasonConstant);
		}
		return new ResponseEntity<Object>(fr, HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@ExceptionHandler(BindingException.class)
	public ResponseEntity<Object> handleBindingErrors(BindingException bindingException) {
		FailureResponse fr = new FailureResponse();
		if (null != bindingException.entity) {
			fr.setId(bindingException.entity.getId());
			fr.setRequestId(bindingException.entity.getRequestId());
			fr.setResponsetime(bindingException.entity.getRequesttime());
			fr.setReturnValue("2");
			fr.setFailureReason(bindingException.bindingResult.getFieldErrors().get(0).getDefaultMessage());
		}
		return new ResponseEntity<Object>(fr, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
