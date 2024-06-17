package io.mosip.mock.mv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object (DTO) representing an error message.
 * <p>
 * This class encapsulates an error code and its corresponding message.
 * It is used for conveying error details in a structured format.
 * <p>
 * Implements Serializable to support serialization and deserialization.
 * 
 * @author Rishabh Keshari
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDTO implements Serializable {

    private static final long serialVersionUID = 2452990684776944908L;

    /**
     * The error code associated with the error.
     */
    private String errorCode;

    /**
     * The error message describing the error.
     */
    private String message;
}