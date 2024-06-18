package io.mosip.proxy.abis.exception;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.mosip.proxy.abis.dto.FailureResponse;

/**
 * Global exception handler for the application.
 * <p>
 * This class intercepts exceptions thrown by the application and converts them
 * into appropriate HTTP responses. It handles custom exceptions such as
 * {@link RequestException} and {@link BindingException}, converting them into
 * {@link FailureResponse} objects with detailed error information.
 * </p>
 * 
 * @since 1.0.0
 */
@ControllerAdvice
public class ExceptionInterceptor extends ResponseEntityExceptionHandler {
	/**
	 * Handles {@link RequestException} thrown by the application.
	 * <p>
	 * This method intercepts {@code RequestException} and constructs a
	 * {@link FailureResponse} object containing details about the error, such as
	 * the request ID, response time, and failure reason. It then returns an HTTP
	 * response with a status code of 500 (Internal Server Error).
	 * </p>
	 * 
	 * @param exp the exception being handled
	 * @return a {@code ResponseEntity} containing the {@code FailureResponse} and
	 *         HTTP status
	 */
	@ExceptionHandler(RequestException.class)
	public ResponseEntity<Object> handleInsertRequestException(RequestException exp) {
		FailureResponse fr = new FailureResponse();
		if (!Objects.isNull(exp.getEntity())) {
			fr.setId(exp.getEntity().getId());
			fr.setRequestId(exp.getEntity().getRequestId());
			fr.setResponsetime(exp.getEntity().getRequesttime());
			fr.setReturnValue("2");
			fr.setFailureReason(exp.getReasonConstant());
		}
		return new ResponseEntity<>(fr, HttpStatus.INTERNAL_SERVER_ERROR);

	}

	/**
	 * Handles {@link BindingException} thrown by the application.
	 * <p>
	 * This method intercepts {@code BindingException} and constructs a
	 * {@link FailureResponse} object containing details about the error, such as
	 * the request ID, response time, and failure reason. It then returns an HTTP
	 * response with a status code of 500 (Internal Server Error).
	 * </p>
	 * 
	 * @param bindingException the exception being handled
	 * @return a {@code ResponseEntity} containing the {@code FailureResponse} and
	 *         HTTP status
	 */
	@ExceptionHandler(BindingException.class)
	public ResponseEntity<Object> handleBindingErrors(BindingException bindingException) {
		FailureResponse fr = new FailureResponse();
		if (!Objects.isNull(bindingException.entity)) {
			fr.setId(bindingException.entity.getId());
			fr.setRequestId(bindingException.entity.getRequestId());
			fr.setResponsetime(bindingException.entity.getRequesttime());
			fr.setReturnValue("2");
			fr.setFailureReason(bindingException.bindingResult.getFieldErrors().get(0).getDefaultMessage());
		}
		return new ResponseEntity<>(fr, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}