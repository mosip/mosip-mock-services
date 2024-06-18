package io.mosip.proxy.abis.exception;

import java.io.Serializable;

import org.springframework.validation.BindingResult;

import io.mosip.proxy.abis.dto.RequestMO;
import lombok.Getter;

/**
 * Represents an exception that occurs during the binding of request parameters
 * to a {@link RequestMO} entity.
 * <p>
 * This exception is typically thrown when there are validation errors while
 * binding incoming request data to a {@code RequestMO} object. It encapsulates
 * the entity being bound and the binding result containing validation errors.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {@code
 * if (bindingResult.hasErrors()) {
 * 	throw new BindingException(requestMO, bindingResult);
 * }
 * }
 * </pre>
 * 
 * @since 1.0.0
 */
@Getter
public class BindingException extends Exception implements Serializable {
	/** Serial version UID for serialization. */
	private static final long serialVersionUID = 4870396284624577010L;

	/** The entity that was being bound during the exception. */
	public final RequestMO entity;

	/** The binding result containing validation errors. */
	public final transient BindingResult bindingResult;

	/**
	 * Default constructor for {@code BindingException}.
	 * <p>
	 * Initializes the exception with a new {@link RequestMO} entity and a
	 * {@code null} binding result.
	 * </p>
	 */
	public BindingException() {
		super();
		this.entity = new RequestMO();
		this.bindingResult = null;
	}

	/**
	 * Constructs a {@code BindingException} with the specified entity and binding
	 * result.
	 * 
	 * @param entity        the entity that was being bound
	 * @param bindingResult the binding result containing validation errors
	 */
	public BindingException(RequestMO entity, BindingResult bindingResult) {
		super();
		this.bindingResult = bindingResult;
		this.entity = entity;
	}
}