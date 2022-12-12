package io.mosip.mock.authdata.dto;

import static io.mosip.mock.authdata.util.ErrorConstants.INVALID_REQUEST;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.mosip.mock.authdata.validator.RequestTime;
import lombok.Data;

@Data
public class RequestWrapper<T> {

    @RequestTime
    private String requestTime;

    @NotNull(message = INVALID_REQUEST)
    @Valid
    private T request;
}
